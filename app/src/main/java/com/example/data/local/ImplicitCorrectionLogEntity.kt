package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "implicit_correction_logs")
data class ImplicitCorrectionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val memoryId: Long,
    val memoryType: String, // "fact", "event", "fragment"
    val userFeedbackText: String,
    val previousConfidence: String,
    val newConfidence: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ImplicitCorrectionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ImplicitCorrectionLogEntity): Long

    @Query("SELECT * FROM implicit_correction_logs WHERE botId = :botId ORDER BY timestamp DESC")
    suspend fun getLogsForBot(botId: String): List<ImplicitCorrectionLogEntity>
}
