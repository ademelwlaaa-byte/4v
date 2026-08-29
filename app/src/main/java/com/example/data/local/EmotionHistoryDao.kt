package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EmotionHistoryDao {
    @Insert
    suspend fun insertHistory(entity: EmotionHistoryEntity): Long

    @Query("SELECT * FROM emotion_history WHERE botId = :botId ORDER BY timestamp ASC")
    suspend fun getHistoryForBot(botId: String): List<EmotionHistoryEntity>

    @Query("SELECT * FROM emotion_history WHERE botId = :botId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentHistoryForBot(botId: String, limit: Int = 20): List<EmotionHistoryEntity>
}

@Dao
interface SelfCheckFailureLogDao {
    @Insert
    suspend fun insertLog(entity: SelfCheckFailureLogEntity): Long

    @Query("SELECT * FROM self_check_failure_logs WHERE botId = :botId ORDER BY timestamp DESC")
    suspend fun getLogsForBot(botId: String): List<SelfCheckFailureLogEntity>
}
