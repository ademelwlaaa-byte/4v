package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SceneTemplateDao {
    @Query("SELECT * FROM scene_templates")
    fun getAllTemplatesFlow(): Flow<List<SceneTemplateEntity>>

    @Query("SELECT * FROM scene_templates")
    suspend fun getAllTemplates(): List<SceneTemplateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<SceneTemplateEntity>)

    @Query("DELETE FROM scene_templates")
    suspend fun deleteAll()
}
