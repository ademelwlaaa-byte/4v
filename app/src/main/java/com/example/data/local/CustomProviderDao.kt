package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomProviderDao {
    @Query("SELECT * FROM custom_providers ORDER BY createdAt DESC")
    fun getAllProvidersFlow(): Flow<List<CustomProviderEntity>>

    @Query("SELECT * FROM custom_providers ORDER BY createdAt DESC")
    suspend fun getAllProviders(): List<CustomProviderEntity>

    @Query("SELECT * FROM custom_providers WHERE id = :id LIMIT 1")
    suspend fun getProviderById(id: Long): CustomProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: CustomProviderEntity): Long

    @Update
    suspend fun updateProvider(provider: CustomProviderEntity)

    @Query("DELETE FROM custom_providers WHERE id = :id")
    suspend fun deleteProvider(id: Long)
}
