package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository pattern implementation to abstract the local Room database operations.
 */
class ProgressRepository(private val progressDao: ProgressDao) {

    val allProgress: Flow<List<LevelProgress>> = progressDao.getAllProgress()

    suspend fun getProgressForLevel(level: Int): LevelProgress? {
        return progressDao.getProgressForLevel(level)
    }

    /**
     * Saves progress for a completed level and automatically unlocks the next level.
     */
    suspend fun saveProgress(level: Int, stars: Int, timeMs: Long, score: Int, steps: Int) {
        val existing = progressDao.getProgressForLevel(level)
        val bestTime = if (existing == null || existing.bestTimeMs == 0L || timeMs < existing.bestTimeMs) {
            timeMs
        } else {
            existing.bestTimeMs
        }
        val maxStars = if (existing == null || stars > existing.stars) stars else existing.stars
        val finalScore = if (existing == null || score > existing.highScore) score else existing.highScore
        val minSteps = if (existing == null || existing.bestSteps == 0 || steps < existing.bestSteps) {
            steps
        } else {
            existing.bestSteps
        }
        
        // Save current level progress
        progressDao.insertOrUpdate(
            LevelProgress(
                level = level,
                stars = maxStars,
                bestTimeMs = bestTime,
                isUnlocked = true,
                highScore = finalScore,
                bestSteps = minSteps
            )
        )

        // Insert this run into history
        progressDao.insertRun(
            LevelRun(
                level = level,
                timeMs = timeMs,
                steps = steps,
                score = score,
                playerName = "You",
                timestamp = System.currentTimeMillis()
            )
        )

        // Automatically unlock the next level
        val nextLevel = level + 1
        val nextExisting = progressDao.getProgressForLevel(nextLevel)
        if (nextExisting == null || !nextExisting.isUnlocked) {
            progressDao.insertOrUpdate(
                LevelProgress(
                    level = nextLevel,
                    stars = nextExisting?.stars ?: 0,
                    bestTimeMs = nextExisting?.bestTimeMs ?: 0L,
                    isUnlocked = true,
                    highScore = nextExisting?.highScore ?: 0,
                    bestSteps = nextExisting?.bestSteps ?: 0
                )
            )
        }
    }

    /**
     * Retrieves the top 5 runs for a specific level.
     */
    fun getTopRunsForLevel(level: Int, limit: Int = 5): Flow<List<LevelRun>> {
        return progressDao.getTopRunsForLevel(level, limit)
    }

    /**
     * Seeds the initial progress with Level 1 unlocked, if empty.
     */
    suspend fun initializeProgress() {
        val level1 = progressDao.getProgressForLevel(1)
        if (level1 == null) {
            progressDao.insertOrUpdate(
                LevelProgress(
                    level = 1,
                    stars = 0,
                    bestTimeMs = 0L,
                    isUnlocked = true
                )
            )
        }
    }

    /**
     * Deletes all local records and re-initializes Level 1 progress.
     */
    suspend fun resetProgress() {
        progressDao.clearAll()
        progressDao.clearAllRuns()
        progressDao.clearAllSpecialStages()
        initializeProgress()
    }

    // Special custom random stage repository methods
    val allSpecialStages: Flow<List<SpecialStage>> = progressDao.getAllSpecialStages()

    suspend fun insertSpecialStage(stage: SpecialStage): Long {
        return progressDao.insertSpecialStage(stage)
    }

    suspend fun deleteSpecialStage(id: Long) {
        progressDao.deleteSpecialStage(id)
    }
}
