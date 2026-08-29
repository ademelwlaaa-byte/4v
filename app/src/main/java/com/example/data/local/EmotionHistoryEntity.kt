package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "emotion_history",
    indices = [Index(value = ["botId"]), Index(value = ["timestamp"])]
)
data class EmotionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val botId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val messageIndex: Int = 0,
    val affectionScore: Int = 50,
    val respectScore: Int = 50,
    val comfortScore: Int = 50,
    val resentmentScore: Int = 0,
    val trustScore: Int = 50,
    val physicalComfortScore: Int = 30,
    val obsessionScore: Int = 0,
    val dominantEmotion: String = "neutral",
    val fullVectorJson: String
)

@Entity(
    tableName = "self_check_failure_logs",
    indices = [Index(value = ["botId"])]
)
data class SelfCheckFailureLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val botId: String,
    val messageIndex: Int = 0,
    val failedChecksJson: String,
    val userMessageText: String = "",
    val modelResponseText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
