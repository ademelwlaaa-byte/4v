package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MalformedOutputLogDao {
    @Insert
    suspend fun insertLog(log: MalformedOutputLogEntity)

    @Query("SELECT COUNT(*) FROM malformed_output_logs WHERE provider = :provider")
    suspend fun getLogCountForProvider(provider: String): Int

    @Query("SELECT COUNT(*) FROM malformed_output_logs WHERE provider = :provider AND timestamp >= :sinceTimestamp")
    suspend fun getRecentLogCountForProvider(provider: String, sinceTimestamp: Long): Int
}
