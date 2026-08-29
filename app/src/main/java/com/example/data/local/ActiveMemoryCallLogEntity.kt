package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "active_memory_call_logs")
data class ActiveMemoryCallLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val action: String,                 // "save_memory", "update_memory", "delete_memory"
    val content: String,
    val category: String = "fact",     // "fact" or "event"
    val importance: Int = 5,           // 1 - 10
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ActiveMemoryCallLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActiveMemoryCallLogEntity): Long

    @Query("SELECT * FROM active_memory_call_logs WHERE botId = :botId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLogsForBot(botId: String, limit: Int = 20): List<ActiveMemoryCallLogEntity>
}
