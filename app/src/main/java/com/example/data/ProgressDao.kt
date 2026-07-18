package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for the level_progress table.
 * Implements reactive Flow queries and asynchronous suspending transactions.
 */
@Dao
interface ProgressDao {

    @Query("SELECT * FROM level_progress ORDER BY level ASC")
    fun getAllProgress(): Flow<List<LevelProgress>>

    @Query("SELECT * FROM level_progress WHERE level = :level LIMIT 1")
    suspend fun getProgressForLevel(level: Int): LevelProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: LevelProgress)

    @Query("DELETE FROM level_progress")
    suspend fun clearAll()
}
