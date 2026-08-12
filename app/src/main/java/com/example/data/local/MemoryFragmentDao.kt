package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MemoryFragmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFragment(fragment: MemoryFragmentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFragments(fragments: List<MemoryFragmentEntity>)

    @Query("SELECT * FROM memory_fragments WHERE botId = :botId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentFragments(botId: String, limit: Int = 10): List<MemoryFragmentEntity>

    @Query("""
        SELECT * FROM memory_fragments 
        WHERE botId = :botId AND id IN (
            SELECT docid FROM memory_fragments_fts WHERE memory_fragments_fts MATCH :ftsQuery
        )
        ORDER BY createdAt DESC LIMIT :limit
    """)
    suspend fun searchFragmentsFts(botId: String, ftsQuery: String, limit: Int = 10): List<MemoryFragmentEntity>

    @Query("""
        SELECT * FROM memory_fragments 
        WHERE botId = :botId AND content LIKE :keywordPattern
        ORDER BY createdAt DESC LIMIT :limit
    """)
    suspend fun searchFragmentsLike(botId: String, keywordPattern: String, limit: Int = 10): List<MemoryFragmentEntity>

    @Query("SELECT COUNT(*) FROM memory_fragments WHERE botId = :botId")
    suspend fun getFragmentCount(botId: String): Int

    @Query("""
        DELETE FROM memory_fragments 
        WHERE id IN (
            SELECT id FROM memory_fragments 
            WHERE botId = :botId 
            ORDER BY createdAt ASC 
            LIMIT :deleteCount
        )
    """)
    suspend fun deleteOldestFragments(botId: String, deleteCount: Int)

    @Query("DELETE FROM memory_fragments WHERE botId = :botId")
    suspend fun deleteFragmentsForBot(botId: String)
}
