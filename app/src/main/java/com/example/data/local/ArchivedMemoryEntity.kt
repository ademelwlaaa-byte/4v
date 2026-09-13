package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "archived_memories")
data class ArchivedMemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val originalMemoryId: Long,
    val memoryType: String,
    val content: String,
    val archivedReason: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ArchivedMemoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArchive(archive: ArchivedMemoryEntity): Long

    @Query("SELECT * FROM archived_memories WHERE botId = :botId ORDER BY timestamp DESC")
    suspend fun getArchivesForBot(botId: String): List<ArchivedMemoryEntity>
}
