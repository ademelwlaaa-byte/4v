package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AffectionEventDao {
    @Query("SELECT * FROM affection_events WHERE botId = :botId ORDER BY timestamp DESC")
    fun getEventsForBot(botId: String): Flow<List<AffectionEventEntity>>

    @Query("SELECT * FROM affection_events WHERE botId = :botId ORDER BY timestamp DESC")
    suspend fun getEventsForBotList(botId: String): List<AffectionEventEntity>

    @Insert
    suspend fun insertEvent(event: AffectionEventEntity)

    @Query("DELETE FROM affection_events WHERE botId = :botId")
    suspend fun deleteEventsForBot(botId: String)
}
