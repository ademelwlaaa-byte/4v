package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "memory_checkpoints")
data class MemoryCheckpointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val checkpointNumber: Int,
    val messageRangeStart: Int,
    val messageRangeEnd: Int,
    val summaryText: String,
    val embedding: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface MemoryCheckpointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckpoint(checkpoint: MemoryCheckpointEntity): Long

    @Query("SELECT * FROM memory_checkpoints WHERE botId = :botId ORDER BY checkpointNumber DESC")
    suspend fun getCheckpointsForBot(botId: String): List<MemoryCheckpointEntity>

    @Query("SELECT * FROM memory_checkpoints WHERE botId = :botId ORDER BY checkpointNumber DESC LIMIT :limit")
    suspend fun getRecentCheckpoints(botId: String, limit: Int = 3): List<MemoryCheckpointEntity>

    @Query("SELECT MAX(checkpointNumber) FROM memory_checkpoints WHERE botId = :botId")
    suspend fun getMaxCheckpointNumber(botId: String): Int?
}
