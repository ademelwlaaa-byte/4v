package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "memory_facts")
data class MemoryFactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val subject: String,          // e.g. "kullanıcı", "karakter", "Mehmet"
    val key: String,              // e.g. "meslek", "yaş", "fobi"
    val value: String,            // e.g. "öğretmen"
    val confidence: String = "certain", // "certain" or "inferred"
    val lastConfirmedAt: Long = System.currentTimeMillis(),
    val userCorrected: Boolean = false,
    val supersededBy: Long? = null,
    val isBotSaved: Boolean = true
)

@Dao
interface MemoryFactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: MemoryFactEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacts(facts: List<MemoryFactEntity>)

    @Update
    suspend fun updateFact(fact: MemoryFactEntity)

    @Query("SELECT * FROM memory_facts WHERE botId = :botId AND supersededBy IS NULL ORDER BY lastConfirmedAt DESC")
    suspend fun getActiveFacts(botId: String): List<MemoryFactEntity>

    @Query("SELECT * FROM memory_facts WHERE botId = :botId AND supersededBy IS NULL AND (subject LIKE '%' || :query || '%' OR key LIKE '%' || :query || '%' OR value LIKE '%' || :query || '%') ORDER BY lastConfirmedAt DESC")
    suspend fun searchFacts(botId: String, query: String): List<MemoryFactEntity>

    @Query("SELECT * FROM memory_facts WHERE id = :id")
    suspend fun getFactById(id: Long): MemoryFactEntity?

    @Query("DELETE FROM memory_facts WHERE id = :id")
    suspend fun deleteFact(id: Long)

    @Query("DELETE FROM memory_facts WHERE botId = :botId AND userCorrected = 0")
    suspend fun deleteAutoFacts(botId: String)
}
