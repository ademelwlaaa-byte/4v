package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "cast_members")
data class CastMemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val name: String,
    val description: String,
    val role: String = "Yan Karakter",
    val affectionScore: Int = 50,
    val relationshipState: String = "Tanıdık",
    val firstAppearedAt: Long = System.currentTimeMillis(),
    val importanceScore: Int = 50,
    val isAutoAdded: Boolean = true,
    val isBlacklisted: Boolean = false,
    val birthDate: String = "",
    val currentAge: Int = 20,
    val initialAge: Int = 20
)

@Dao
interface CastMemberDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCastMember(member: CastMemberEntity): Long

    @Update
    suspend fun updateCastMember(member: CastMemberEntity)

    @Query("SELECT * FROM cast_members WHERE botId = :botId AND isBlacklisted = 0 ORDER BY importanceScore DESC, firstAppearedAt DESC")
    suspend fun getCastMembersForBot(botId: String): List<CastMemberEntity>

    @Query("SELECT * FROM cast_members WHERE botId = :botId AND LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findByName(botId: String, name: String): CastMemberEntity?

    @Query("UPDATE cast_members SET isBlacklisted = 1 WHERE id = :id")
    suspend fun blacklistCastMember(id: Long)

    @Query("DELETE FROM cast_members WHERE id = :id")
    suspend fun deleteCastMember(id: Long)
}
