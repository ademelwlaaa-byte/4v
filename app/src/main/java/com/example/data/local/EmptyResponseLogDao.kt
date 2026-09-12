package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EmptyResponseLogDao {
    @Insert
    suspend fun insertLog(log: EmptyResponseLogEntity)

    @Query("SELECT * FROM empty_response_logs WHERE botId = :botId ORDER BY timestamp DESC LIMIT 50")
    suspend fun getLogsForBot(botId: String): List<EmptyResponseLogEntity>

    @Query("SELECT * FROM empty_response_logs ORDER BY timestamp DESC LIMIT 100")
    suspend fun getAllLogs(): List<EmptyResponseLogEntity>

    @Query("SELECT COUNT(*) FROM empty_response_logs WHERE botId = :botId AND finishReason LIKE '%filter%'")
    suspend fun getContentFilterCountForBot(botId: String): Int

    @Query("DELETE FROM empty_response_logs WHERE botId = :botId")
    suspend fun clearLogsForBot(botId: String)
}
