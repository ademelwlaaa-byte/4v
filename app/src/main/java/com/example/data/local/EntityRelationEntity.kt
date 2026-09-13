package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "entity_relations")
data class EntityRelationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val sourceEntityId: Long = 0L,
    val sourceEntityName: String = "",
    val relationType: String,         // e.g. "kardeşi", "arkadaşı", "annesi", "patronu"
    val targetEntityId: Long = 0L,
    val targetEntityName: String = "",
    val confidence: String = "certain", // "certain", "inferred", "disputed"
    val createdAtMessageIndex: Int = 0
)

@Dao
interface EntityRelationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelation(relation: EntityRelationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelations(relations: List<EntityRelationEntity>)

    @Update
    suspend fun updateRelation(relation: EntityRelationEntity)

    @Query("SELECT * FROM entity_relations WHERE botId = :botId AND confidence != 'disputed'")
    suspend fun getRelationsForBot(botId: String): List<EntityRelationEntity>

    @Query("SELECT * FROM entity_relations WHERE botId = :botId AND (LOWER(sourceEntityName) = LOWER(:name) OR LOWER(targetEntityName) = LOWER(:name))")
    suspend fun findRelationsByEntityName(botId: String, name: String): List<EntityRelationEntity>

    @Query("DELETE FROM entity_relations WHERE id = :id")
    suspend fun deleteRelation(id: Long)
}
