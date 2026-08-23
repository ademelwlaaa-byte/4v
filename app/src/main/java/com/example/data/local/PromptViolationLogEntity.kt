package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompt_violation_logs")
data class PromptViolationLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val messageText: String,
    val violationType: String,
    val timestamp: Long = System.currentTimeMillis()
)
