package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "malformed_output_logs")
data class MalformedOutputLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val provider: String,
    val model: String,
    val rawOutput: String,
    val errorMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)
