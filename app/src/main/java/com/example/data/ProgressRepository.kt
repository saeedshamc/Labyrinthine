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
        initializeProgress()
    }
}
