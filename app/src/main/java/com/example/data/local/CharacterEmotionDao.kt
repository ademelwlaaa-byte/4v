package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterEmotionDao {
    @Query("SELECT * FROM character_emotions WHERE botId = :botId")
    suspend fun getEmotionsForBot(botId: String): List<CharacterEmotionEntity>

    @Query("SELECT * FROM character_emotions WHERE botId = :botId")
    fun getEmotionsForBotFlow(botId: String): Flow<List<CharacterEmotionEntity>>

    @Query("SELECT * FROM character_emotions WHERE botId = :botId AND LOWER(characterName) = LOWER(:characterName) LIMIT 1")
    suspend fun getEmotionForCharacter(botId: String, characterName: String): CharacterEmotionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(emotion: CharacterEmotionEntity)

    @Query("DELETE FROM character_emotions WHERE botId = :botId")
    suspend fun deleteEmotionsForBot(botId: String)
}
