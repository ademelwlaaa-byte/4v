package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Entity(tableName = "sensitive_triggers")
data class SensitiveTriggerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val triggerTopic: String,
    val sourceDescription: String,
    val intensityMultiplier: Double = 2.5,
    val recentTriggerCount: Int = 0
)

@Dao
interface SensitiveTriggerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: SensitiveTriggerEntity): Long

    @Query("SELECT * FROM sensitive_triggers WHERE botId = :botId")
    suspend fun getTriggersForBot(botId: String): List<SensitiveTriggerEntity>

    @Query("SELECT * FROM sensitive_triggers WHERE botId = :botId AND triggerTopic = :topic LIMIT 1")
    suspend fun getTriggerByTopic(botId: String, topic: String): SensitiveTriggerEntity?

    @Query("DELETE FROM sensitive_triggers WHERE botId = :botId")
    suspend fun deleteTriggersForBot(botId: String)
}

@Entity(tableName = "pending_reappraisals")
data class PendingReappraisalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val botId: String,
    val originalEventDescription: String,
    val originalEmotionState: String,
    val triggeredAtMessageIndex: Int,
    val triggeredAtTimestamp: Long = System.currentTimeMillis(),
    val reappraisalEligibleAtMessageIndex: Int,
    val reappraisalEligibleAtTimestamp: Long,
    val resolved: Boolean = false,
    val expired: Boolean = false
)

@Dao
interface PendingReappraisalDao {
    @Insert
    suspend fun insert(entity: PendingReappraisalEntity): Long

    @Update
    suspend fun update(entity: PendingReappraisalEntity)

    @Query("SELECT * FROM pending_reappraisals WHERE botId = :botId AND resolved = 0 AND expired = 0 ORDER BY triggeredAtTimestamp ASC")
    suspend fun getActivePendingReappraisals(botId: String): List<PendingReappraisalEntity>

    @Query("SELECT * FROM pending_reappraisals WHERE botId = :botId ORDER BY triggeredAtTimestamp ASC")
    suspend fun getAllPendingReappraisals(botId: String): List<PendingReappraisalEntity>

    @Query("SELECT * FROM pending_reappraisals WHERE botId = :botId")
    suspend fun getAllForBot(botId: String): List<PendingReappraisalEntity>

    @Query("DELETE FROM pending_reappraisals WHERE botId = :botId")
    suspend fun deleteForBot(botId: String)
}
