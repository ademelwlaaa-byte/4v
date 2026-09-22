package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "memory_events")
data class MemoryEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val description: String,
    val importanceScore: Int = 50,     // 0 - 100
    val embedding: String = "",         // JSON serialized List<Float>
    val supersededBy: Long? = null,
    val isBotSaved: Boolean = true,
    val realWorldTimestamp: Long = System.currentTimeMillis(),
    val storyDayIndex: Long = 1L,
    val storyCalendarDateStr: String = ""
)

@Dao
interface MemoryEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: MemoryEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<MemoryEventEntity>)

    @Update
    suspend fun updateEvent(event: MemoryEventEntity)

    @Query("SELECT * FROM memory_events WHERE botId = :botId AND supersededBy IS NULL ORDER BY timestamp DESC")
    suspend fun getActiveEvents(botId: String): List<MemoryEventEntity>

    @Query("SELECT * FROM memory_events WHERE botId = :botId AND supersededBy IS NULL ORDER BY timestamp DESC")
    fun getActiveEventsFlow(botId: String): kotlinx.coroutines.flow.Flow<List<MemoryEventEntity>>

    @Query("SELECT COUNT(*) FROM memory_events WHERE botId = :botId AND supersededBy IS NULL")
    suspend fun getEventCount(botId: String): Int

    @Query("DELETE FROM memory_events WHERE id IN (SELECT id FROM memory_events WHERE botId = :botId AND supersededBy IS NULL AND importanceScore < 80 ORDER BY timestamp ASC LIMIT :limit)")
    suspend fun deleteOldestLowImportanceEvents(botId: String, limit: Int)

    @Query("DELETE FROM memory_events WHERE id = :id")
    suspend fun deleteEvent(id: Long)

    @Query("DELETE FROM memory_events WHERE botId = :botId")
    suspend fun deleteAllEventsForBot(botId: String)
}
