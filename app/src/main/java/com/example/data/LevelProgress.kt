package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity representing the player's performance and unlock status on each maze level.
 */
@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val level: Int,
    val stars: Int = 0,
    val bestTimeMs: Long = 0L,
    val isUnlocked: Boolean = false,
    val highScore: Int = 0,
    val bestSteps: Int = 0
)

/**
 * Room Entity representing a single completed level run to populate a leaderboard.
 */
@Entity(tableName = "level_runs")
data class LevelRun(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val level: Int,
    val timeMs: Long,
    val steps: Int,
    val score: Int,
    val playerName: String,
    val timestamp: Long
)

/**
 * Room Entity representing a custom randomly generated stage in the Special Section.
 */
@Entity(tableName = "special_stages")
data class SpecialStage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val seed: Long,
    val gridSize: Int,
    val timestamp: Long = System.currentTimeMillis()
)


