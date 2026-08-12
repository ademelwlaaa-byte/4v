package com.example.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BotDao {
    @Query("SELECT * FROM bots ORDER BY updatedAt DESC")
    fun getAllBots(): Flow<List<BotEntity>>

    @Query("SELECT * FROM bots")
    suspend fun getAllBotsList(): List<BotEntity>

    @Query("SELECT * FROM bots WHERE id = :id")
    suspend fun getBotById(id: String): BotEntity?

    @Query("SELECT * FROM bots WHERE id = :id")
    fun getBotByIdFlow(id: String): Flow<BotEntity?>

    @Query("SELECT * FROM bots WHERE needsSummarization = 1")
    suspend fun getBotsNeedingSummarization(): List<BotEntity>

    @Query("UPDATE bots SET needsSummarization = :needs WHERE id = :id")
    suspend fun setNeedsSummarization(id: String, needs: Boolean)

    @Upsert
    suspend fun insertOrUpdate(bot: BotEntity)

    @Query("DELETE FROM bots WHERE id = :id")
    suspend fun deleteBotById(id: String)

    @Query("DELETE FROM bots")
    suspend fun deleteAllBots()
}
