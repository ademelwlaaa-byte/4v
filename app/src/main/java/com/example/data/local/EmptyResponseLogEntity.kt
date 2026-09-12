package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empty_response_logs")
data class EmptyResponseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val botId: String,
    val provider: String,
    val model: String,
    val finishReason: String,
    val rawLength: Int,
    val errorMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)
