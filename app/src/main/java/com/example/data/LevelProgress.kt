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
