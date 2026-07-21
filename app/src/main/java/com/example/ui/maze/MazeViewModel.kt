package com.example.ui.maze

import android.app.Application
import android.content.Context
import android.os.Vibrator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.LevelProgress
import com.example.data.MazeCell
import com.example.data.MazeGenerator
import com.example.data.ProgressRepository
import com.example.util.DifficultyCurve
import com.example.util.Localization
import com.example.util.SoundManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Screen navigation state indicators.
 */
enum class GameScreen {
    WELCOME,
    LEVEL_SELECT,
    GAME_PLAY,
    SETTINGS
}

/**
 * Game control inputs.
 */
enum class ControlScheme {
    SWIPE,
    JOYSTICK
}

/**
 * Leaderboard record representing competitor ranking in Time Trial.
 */
data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val timeMs: Long,
    val isUser: Boolean = false,
    val isPersonalBest: Boolean = false
)

private data class RawEntry(
    val name: String,
    val timeMs: Long,
    val isUser: Boolean,
    val isPB: Boolean
)

/**
 * Visual particle modeling for the celebratory complete explosion.
 */
data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    var alpha: Float,
    var life: Int,
    val maxLife: Int,
    val colorType: Int = 0
)

class MazeViewModel(
    application: Application,
    private val repository: ProgressRepository
) : AndroidViewModel(application) {

    // Dynamic localisation & theme settings states
    var currentLanguage by mutableStateOf(Localization.Language.EN)
    var isDarkTheme by mutableStateOf(true) // default: dark theme
    
    private var _soundEnabled by mutableStateOf(true)
    var soundEnabled: Boolean
        get() = _soundEnabled
        set(value) {
            _soundEnabled = value
            SoundManager.isSoundEnabled = value
        }

    var hapticEnabled by mutableStateOf(true)
    var controlScheme by mutableStateOf(ControlScheme.JOYSTICK)
    var minimapEnabled by mutableStateOf(true)
    var isTimeTrialMode by mutableStateOf(false)

    // Current navigation state
    var currentScreen by mutableStateOf(GameScreen.WELCOME)

    // Persistent Level Progression state Flow
    val levelProgressList: StateFlow<List<LevelProgress>> = repository.allProgress
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Generates a fully dynamic, real-time leaderboard list of competitors
     * relative to the user's active timer or personal best record.
     */
    fun getLeaderboardEntries(activeTimeMs: Long): List<LeaderboardEntry> {
        val (threeStarSec, twoStarSec) = DifficultyCurve.getStarThresholds(currentLevel)
        val baseSec = threeStarSec.toDouble()

        // 4 preset global competitors with times proportional to the star thresholds
        val comp1Time = (baseSec * 0.70 * 1000).toLong() // Top Master
        val comp2Time = (baseSec * 0.90 * 1000).toLong() // Pro Speedrunner
        val comp3Time = (baseSec * 1.15 * 1000).toLong() // Regular Gamer
        val comp4Time = (baseSec * 1.40 * 1000).toLong() // Beginner/Ghost

        val rawList = mutableListOf<RawEntry>()
        rawList.add(RawEntry("LabyrinthLegend", comp1Time, isUser = false, isPB = false))
        rawList.add(RawEntry("CyberGlider", comp2Time, isUser = false, isPB = false))
        rawList.add(RawEntry("NeonRacer", comp3Time, isUser = false, isPB = false))
        rawList.add(RawEntry("ShadowRunner", comp4Time, isUser = false, isPB = false))

        // Include user's personal best if they have completed this level before
        val pbTimeMs = levelProgressList.value.find { it.level == currentLevel }?.bestTimeMs ?: 0L
        if (pbTimeMs > 0L) {
            rawList.add(RawEntry("Personal Best", pbTimeMs, isUser = false, isPB = true))
        }

        // Add current active run position
        rawList.add(RawEntry("You", activeTimeMs, isUser = true, isPB = false))

        // Sort ascending by time
        val sortedList = rawList.sortedBy { it.timeMs }

        // Map to rank and output LeaderboardEntry elements
        return sortedList.mapIndexed { index, entry ->
            LeaderboardEntry(
                rank = index + 1,
                name = entry.name,
                timeMs = entry.timeMs,
                isUser = entry.isUser,
                isPersonalBest = entry.isPB
            )
        }
    }

    // Active gameplay variables
    var currentLevel by mutableStateOf(1)
    var mazeGrid by mutableStateOf<Array<Array<MazeCell>>?>(null)
    var playerX by mutableStateOf(0)
    var playerY by mutableStateOf(0)
    var exitX by mutableStateOf(0)
    var exitY by mutableStateOf(0)

    // Trail breadcrumbs: list of visited cell coordinates to render background glows
    var playerTrail by mutableStateOf<List<Pair<Int, Int>>>(emptyList())

    // Game stats
    var isTimerRunning by mutableStateOf(false)
    var gameTimeTicks by mutableStateOf(0L) // in tenths of a second (100ms units)
    var levelCompleted by mutableStateOf(false)
    var starsEarned by mutableStateOf(0)
    var completionTimeMs by mutableStateOf(0L)
    var stepsTaken by mutableStateOf(0)
    var currentScore by mutableStateOf(0)

    // Particle explosions on level completion
    var activeParticles by mutableStateOf<List<Particle>>(emptyList())

    // Coroutine Job for the running game timer and particles
    private var timerJob: Job? = null
    private var particlesJob: Job? = null

    init {
        viewModelScope.launch {
            repository.initializeProgress()
        }
    }

    /**
     * Prepares and starts a specific level.
     */
    fun startLevel(level: Int) {
        currentLevel = level
        val size = DifficultyCurve.getGridSize(level)
        
        // Generate procedural maze
        val generatedGrid = MazeGenerator.generate(size, size, seed = level.toLong() * 9997L)
        mazeGrid = generatedGrid
        
        // Initialize player position (entrance: top-left corner)
        playerX = 0
        playerY = 0
        
        // Exit is placed randomly and deterministically on one of the outer borders, far from (0,0)
        val random = java.util.Random(level.toLong() * 12345L)
        val edgeChoices = if (size >= 11) 4 else 2
        val edge = random.nextInt(edgeChoices)
        when (edge) {
            0 -> { // Bottom edge
                exitX = size / 2 + random.nextInt(size - size / 2)
                exitY = size - 1
                generatedGrid[exitX][exitY].bottomWall = false
            }
            1 -> { // Right edge
                exitX = size - 1
                exitY = size / 2 + random.nextInt(size - size / 2)
                generatedGrid[exitX][exitY].rightWall = false
            }
            2 -> { // Top edge
                exitX = size / 2 + random.nextInt(size - size / 2)
                exitY = 0
                generatedGrid[exitX][exitY].topWall = false
            }
            else -> { // Left edge
                exitX = 0
                exitY = size / 2 + random.nextInt(size - size / 2)
                generatedGrid[exitX][exitY].leftWall = false
            }
        }
        
        playerTrail = listOf(Pair(0, 0))
        levelCompleted = false
        starsEarned = 0
        gameTimeTicks = 0L
        stepsTaken = 0
        currentScore = 0
        activeParticles = emptyList()
        currentScreen = GameScreen.GAME_PLAY

        startTimer()
    }

    /**
     * Start the countdown timer.
     */
    private fun startTimer() {
        timerJob?.cancel()
        isTimerRunning = true
        timerJob = viewModelScope.launch {
            while (isTimerRunning && !levelCompleted) {
                delay(100L)
                gameTimeTicks++
            }
        }
    }

    /**
     * Stops the current gameplay timer.
     */
    private fun stopTimer() {
        isTimerRunning = false
        timerJob?.cancel()
    }

    /**
     * Direct user instruction to move in one of the 4 card directions.
     */
    fun movePlayer(dx: Int, dy: Int) {
        if (levelCompleted) return
        val grid = mazeGrid ?: return
        val size = DifficultyCurve.getGridSize(currentLevel)

        val targetX = playerX + dx
        val targetY = playerY + dy

        // Special case: check if player is physically exiting the maze boundaries from the exit cell
        if (playerX == exitX && playerY == exitY) {
            val cell = grid[playerX][playerY]
            val isExiting = when {
                dx == 0 && dy == -1 && !cell.topWall && targetY == -1 -> true
                dx == 0 && dy == 1 && !cell.bottomWall && targetY == size -> true
                dx == -1 && dy == 0 && !cell.leftWall && targetX == -1 -> true
                dx == 1 && dy == 0 && !cell.rightWall && targetX == size -> true
                else -> false
            }
            if (isExiting) {
                playerX = targetX
                playerY = targetY
                completeLevel()
                return
            }
        }

        // Verify boundaries
        if (targetX !in 0 until size || targetY !in 0 until size) {
            SoundManager.playCollision()
            return
        }

        val currentCell = grid[playerX][playerY]

        // Check if movement is blocked by walls
        val moveBlocked = when {
            dx == 1 && dy == 0 -> currentCell.rightWall
            dx == -1 && dy == 0 -> currentCell.leftWall
            dx == 0 && dy == 1 -> currentCell.bottomWall
            dx == 0 && dy == -1 -> currentCell.topWall
            else -> true
        }

        if (!moveBlocked) {
            playerX = targetX
            playerY = targetY
            stepsTaken++
            
            // Add to breadcrumb trail
            if (Pair(playerX, playerY) !in playerTrail) {
                playerTrail = playerTrail + Pair(playerX, playerY)
            }
            
            triggerHapticClick()
            SoundManager.playMove()
        } else {
            SoundManager.playCollision()
        }
    }

    /**
     * Trigger completion effects and saves results to Room database.
     */
    private fun completeLevel() {
        levelCompleted = true
        stopTimer()
        
        completionTimeMs = gameTimeTicks * 100L
        val secondsElapsed = (completionTimeMs / 1000f).toInt()
        
        // Calculate stars based on difficulty thresholds
        val (threeStarSec, twoStarSec) = DifficultyCurve.getStarThresholds(currentLevel)
        starsEarned = when {
            secondsElapsed <= threeStarSec -> 3
            secondsElapsed <= twoStarSec -> 2
            else -> 1
        }

        // Calculate dynamic game score
        val baseScore = 1000 + currentLevel * 100
        val timePenalty = (completionTimeMs / 100).toInt() // 10 points per second (100ms units)
        val stepsPenalty = stepsTaken * 20
        currentScore = maxOf(100, baseScore - timePenalty - stepsPenalty)

        // Trigger celebratory visual particles
        triggerCompletionExplosion()
        triggerHapticCompletion()
        SoundManager.playWin()

        // Persist level progress and unlock next level in Room database
        viewModelScope.launch {
            repository.saveProgress(currentLevel, starsEarned, completionTimeMs, currentScore, stepsTaken)
        }
    }

    /**
     * Procedural explosion at the centre exit cell.
     */
    private fun triggerCompletionExplosion() {
        val particles = mutableListOf<Particle>()
        val random = Random(System.currentTimeMillis())
        
        // Spawn 60 dynamic particles of different colors
        for (i in 0 until 60) {
            val angle = random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = 2f + random.nextFloat() * 10f
            particles.add(
                Particle(
                    x = exitX.toFloat() + 0.5f,
                    y = exitY.toFloat() + 0.5f,
                    vx = Math.cos(angle.toDouble()).toFloat() * speed * 0.05f,
                    vy = Math.sin(angle.toDouble()).toFloat() * speed * 0.05f,
                    size = 6f + random.nextFloat() * 14f,
                    alpha = 1.0f,
                    life = 0,
                    maxLife = 20 + random.nextInt(35),
                    colorType = random.nextInt(3)
                )
            )
        }
        activeParticles = particles

        // Animating loop
        particlesJob?.cancel()
        particlesJob = viewModelScope.launch {
            while (activeParticles.isNotEmpty() && levelCompleted) {
                delay(16L) // ~60fps
                val currentList = activeParticles.map { p ->
                    p.life++
                    p.alpha = 1f - (p.life.toFloat() / p.maxLife.toFloat())
                    p.copy(
                        x = p.x + p.vx,
                        y = p.y + p.vy,
                        alpha = p.alpha,
                        life = p.life
                    )
                }.filter { it.life < it.maxLife }
                activeParticles = currentList
            }
        }
    }

    private fun triggerHapticClick() {
        if (!hapticEnabled) return
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    it.vibrate(android.os.VibrationEffect.createOneShot(10, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(10)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun triggerHapticCompletion() {
        if (!hapticEnabled) return
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.let {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val pattern = longArrayOf(0, 50, 40, 100)
                    it.vibrate(android.os.VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(150)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Erases saved Room database progress.
     */
    fun resetSavedProgress() {
        viewModelScope.launch {
            repository.resetProgress()
            currentScreen = GameScreen.WELCOME
        }
    }
}

/**
 * Factory class for ViewModel instantiation.
 */
class MazeViewModelFactory(
    private val application: Application,
    private val repository: ProgressRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MazeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MazeViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
