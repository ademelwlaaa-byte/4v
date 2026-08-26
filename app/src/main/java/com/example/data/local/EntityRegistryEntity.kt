package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "entity_registry")
data class EntityRegistryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val entityName: String,       // e.g. "Ayşe", "İstanbul"
    val entityType: String = "person", // "person", "location", "object"
    val description: String,      // e.g. "kullanıcının kız kardeşi, 24 yaşında"
    val firstMentionedAt: Long = System.currentTimeMillis(),
    val lastMentionedAt: Long = System.currentTimeMillis()
)

@Dao
interface EntityRegistryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateEntity(entity: EntityRegistryEntity): Long

    @Update
    suspend fun updateEntity(entity: EntityRegistryEntity)

    @Query("SELECT * FROM entity_registry WHERE botId = :botId ORDER BY lastMentionedAt DESC")
    suspend fun getEntitiesForBot(botId: String): List<EntityRegistryEntity>

    @Query("SELECT * FROM entity_registry WHERE botId = :botId AND LOWER(entityName) = LOWER(:name) LIMIT 1")
    suspend fun findByName(botId: String, name: String): EntityRegistryEntity?

    @Query("DELETE FROM entity_registry WHERE id = :id")
    suspend fun deleteEntity(id: Long)
}
