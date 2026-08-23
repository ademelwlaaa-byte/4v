package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "affection_events",
    indices = [Index(value = ["botId"])]
)
data class AffectionEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val scoreDelta: Int,
    val shortDescription: String
)
