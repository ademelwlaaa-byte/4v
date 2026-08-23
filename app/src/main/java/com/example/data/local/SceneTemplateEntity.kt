package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scene_templates")
data class SceneTemplateEntity(
    @PrimaryKey val id: String,
    val name: String,
    val setting: String,        // "public", "private", "mixed"
    val mode: String,           // "formal", "casual", "mixed"
    val tension: String,        // "none", "conflict", "crisis"
    val bonusMultiplier: Double = 1.0,
    val description: String
)
