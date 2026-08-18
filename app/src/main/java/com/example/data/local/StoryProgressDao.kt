package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryProgressDao {
    @Query("SELECT * FROM story_progress WHERE botId = :botId")
    fun getProgress(botId: String): Flow<StoryProgressEntity?>

    @Query("SELECT * FROM story_progress WHERE botId = :botId")
    suspend fun getProgressOnce(botId: String): StoryProgressEntity?

    @Query("SELECT * FROM story_progress WHERE botId = :botId LIMIT 1")
    suspend fun getProgressForBot(botId: String): StoryProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: StoryProgressEntity)

    @Query("DELETE FROM story_progress WHERE botId = :botId")
    suspend fun deleteForBot(botId: String)
}
