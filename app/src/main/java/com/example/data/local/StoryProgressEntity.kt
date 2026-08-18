package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "story_progress",
    foreignKeys = [
        ForeignKey(
            entity = BotEntity::class,
            parentColumns = ["id"],
            childColumns = ["botId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StoryProgressEntity(
    @PrimaryKey
    val botId: String,
    val chapterNumber: Int = 1,
    val currentBranch: String = "main", // "main", "kisa_kacis", "kacis_thor"
    val fatigue: Int = 0,
    val firstImpressionAvengers: String = "none", // "impulsive", "calculated", "none"
    val tonyAffinity: Int = 0,
    val steveTrust: Int = 0,
    val mysteryFactor: Int = 0,
    val earlyInterest: String = "none", // "natasha", "wanda", "none"
    val powerDisclosure: String = "moderate", // "minimal", "moderate", "generous"
    val natashaBond: Int = 0,
    val wandaBond: Int = 0,
    val trustAvengers: String = "conflicted", // "leaning_positive", "leaning_negative", "conflicted"
    val commitmentLevel: String = "cautious", // "cautious", "open", "guarded"
    val flightInstinct: Int = 0,
    val steveBond: Int = 0,
    val samRapport: Int = 0
)
