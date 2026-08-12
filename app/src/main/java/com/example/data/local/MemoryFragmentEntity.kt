package com.example.data.local

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey

@Entity(tableName = "memory_fragments")
data class MemoryFragmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val content: String,
    val category: String, // "DURUM" or "HAFIZA"
    val createdAt: Long = System.currentTimeMillis()
)

@Fts4(contentEntity = MemoryFragmentEntity::class)
@Entity(tableName = "memory_fragments_fts")
data class MemoryFragmentFtsEntity(
    val content: String
)
