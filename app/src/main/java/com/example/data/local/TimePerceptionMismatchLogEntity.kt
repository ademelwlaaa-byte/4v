package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "time_perception_mismatch_logs")
data class TimePerceptionMismatchLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val userMessageText: String,
    val modelResponseText: String,
    val detectedTimeExpression: String,
    val codeCalculatedDays: Long,
    val mismatchReason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface TimePerceptionMismatchLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: TimePerceptionMismatchLogEntity): Long

    @Query("SELECT * FROM time_perception_mismatch_logs WHERE botId = :botId ORDER BY timestamp DESC")
    suspend fun getLogsForBot(botId: String): List<TimePerceptionMismatchLogEntity>

    @Query("SELECT COUNT(*) FROM time_perception_mismatch_logs WHERE botId = :botId")
    suspend fun getMismatchCountForBot(botId: String): Int
}
