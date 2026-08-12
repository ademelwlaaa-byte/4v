package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "character_emotions")
data class CharacterEmotionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val characterName: String,
    val emotionState: String = """{"mood":"nötr","intensity":5,"affection":50,"trust":50,"tension":10}"""
)
