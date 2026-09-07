package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE bots ADD COLUMN totalPromptTokens INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE bots ADD COLUMN totalCandidateTokens INTEGER NOT NULL DEFAULT 0")
    }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE bots ADD COLUMN needsSummarization INTEGER NOT NULL DEFAULT 0")
        db.execSQL("CREATE TABLE IF NOT EXISTS `memory_fragments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `content` TEXT NOT NULL, `category` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
        db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS `memory_fragments_fts` USING FTS4(`content` TEXT, content=`memory_fragments`)")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE bots ADD COLUMN emotionState TEXT NOT NULL DEFAULT '{\"mood\":\"nötr\",\"intensity\":5,\"affection\":50,\"trust\":50,\"tension\":10}'")
        db.execSQL("ALTER TABLE bots ADD COLUMN worldAtmosphere TEXT NOT NULL DEFAULT '{\"mood\":\"sakin\",\"intensity\":5,\"currentEvent\":\"\"}'")
        db.execSQL("CREATE TABLE IF NOT EXISTS `character_emotions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `characterName` TEXT NOT NULL, `emotionState` TEXT NOT NULL)")
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE bots ADD COLUMN previousEmotionState TEXT NOT NULL DEFAULT '{\"mood\":\"nötr\",\"intensity\":5,\"affection\":50,\"trust\":50,\"tension\":10}'")
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE messages ADD COLUMN status TEXT NOT NULL DEFAULT 'success'")
    }
}

val MIGRATION_14_15 = object : Migration(14, 15) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `story_progress` (" +
                "`botId` TEXT NOT NULL, " +
                "`chapterNumber` INTEGER NOT NULL DEFAULT 1, " +
                "`currentBranch` TEXT NOT NULL DEFAULT 'main', " +
                "`fatigue` INTEGER NOT NULL DEFAULT 0, " +
                "`firstImpressionAvengers` TEXT NOT NULL DEFAULT 'none', " +
                "`tonyAffinity` INTEGER NOT NULL DEFAULT 0, " +
                "`steveTrust` INTEGER NOT NULL DEFAULT 0, " +
                "`mysteryFactor` INTEGER NOT NULL DEFAULT 0, " +
                "`earlyInterest` TEXT NOT NULL DEFAULT 'none', " +
                "`powerDisclosure` TEXT NOT NULL DEFAULT 'moderate', " +
                "`natashaBond` INTEGER NOT NULL DEFAULT 0, " +
                "`wandaBond` INTEGER NOT NULL DEFAULT 0, " +
                "`trustAvengers` TEXT NOT NULL DEFAULT 'conflicted', " +
                "`commitmentLevel` TEXT NOT NULL DEFAULT 'cautious', " +
                "`flightInstinct` INTEGER NOT NULL DEFAULT 0, " +
                "`steveBond` INTEGER NOT NULL DEFAULT 0, " +
                "`samRapport` INTEGER NOT NULL DEFAULT 0, " +
                "PRIMARY KEY(`botId`), " +
                "FOREIGN KEY(`botId`) REFERENCES `bots`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE " +
                ")"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_story_progress_botId` ON `story_progress` (`botId`)")
    }
}

val MIGRATION_15_16 = object : Migration(15, 16) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `affection_events` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`botId` TEXT NOT NULL, " +
                "`timestamp` INTEGER NOT NULL, " +
                "`scoreDelta` INTEGER NOT NULL, " +
                "`shortDescription` TEXT NOT NULL" +
                ")"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_affection_events_botId` ON `affection_events` (`botId`)")
    }
}

val MIGRATION_16_17 = object : Migration(16, 17) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `prompt_violation_logs` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`botId` TEXT NOT NULL, " +
                "`messageText` TEXT NOT NULL, " +
                "`violationType` TEXT NOT NULL, " +
                "`timestamp` INTEGER NOT NULL" +
                ")"
        )
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `bots` ADD COLUMN `baseAffectionDifficulty` REAL NOT NULL DEFAULT 1.0")
    }
}

val MIGRATION_18_19 = object : Migration(18, 19) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `scene_templates` (" +
                "`id` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`setting` TEXT NOT NULL, " +
                "`mode` TEXT NOT NULL, " +
                "`tension` TEXT NOT NULL, " +
                "`bonusMultiplier` REAL NOT NULL DEFAULT 1.0, " +
                "`description` TEXT NOT NULL, " +
                "PRIMARY KEY(`id`)" +
                ")"
        )
    }
}

val MIGRATION_19_20 = object : Migration(19, 20) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `memory_facts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `subject` TEXT NOT NULL, `key` TEXT NOT NULL, `value` TEXT NOT NULL, `confidence` TEXT NOT NULL DEFAULT 'certain', `lastConfirmedAt` INTEGER NOT NULL, `userCorrected` INTEGER NOT NULL DEFAULT 0, `supersededBy` INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `memory_events` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `description` TEXT NOT NULL, `importanceScore` INTEGER NOT NULL DEFAULT 50, `embedding` TEXT NOT NULL DEFAULT '', `supersededBy` INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `entity_registry` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `entityName` TEXT NOT NULL, `entityType` TEXT NOT NULL DEFAULT 'person', `description` TEXT NOT NULL, `firstMentionedAt` INTEGER NOT NULL, `lastMentionedAt` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `memory_checkpoints` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `checkpointNumber` INTEGER NOT NULL, `messageRangeStart` INTEGER NOT NULL, `messageRangeEnd` INTEGER NOT NULL, `summaryText` TEXT NOT NULL, `embedding` TEXT NOT NULL DEFAULT '', `timestamp` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `cast_members` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `name` TEXT NOT NULL, `description` TEXT NOT NULL, `role` TEXT NOT NULL DEFAULT 'Yan Karakter', `affectionScore` INTEGER NOT NULL DEFAULT 50, `relationshipState` TEXT NOT NULL DEFAULT 'Tanıdık', `firstAppearedAt` INTEGER NOT NULL, `importanceScore` INTEGER NOT NULL DEFAULT 50, `isAutoAdded` INTEGER NOT NULL DEFAULT 1, `isBlacklisted` INTEGER NOT NULL DEFAULT 0)")
    }
}

val MIGRATION_20_21 = object : Migration(20, 21) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `emotion_history` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `messageIndex` INTEGER NOT NULL DEFAULT 0, `affectionScore` INTEGER NOT NULL DEFAULT 50, `respectScore` INTEGER NOT NULL DEFAULT 50, `comfortScore` INTEGER NOT NULL DEFAULT 50, `resentmentScore` INTEGER NOT NULL DEFAULT 0, `trustScore` INTEGER NOT NULL DEFAULT 50, `physicalComfortScore` INTEGER NOT NULL DEFAULT 30, `obsessionScore` INTEGER NOT NULL DEFAULT 0, `dominantEmotion` TEXT NOT NULL DEFAULT 'neutral', `fullVectorJson` TEXT NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_emotion_history_botId` ON `emotion_history` (`botId`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_emotion_history_timestamp` ON `emotion_history` (`timestamp`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `self_check_failure_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `messageIndex` INTEGER NOT NULL DEFAULT 0, `failedChecksJson` TEXT NOT NULL, `userMessageText` TEXT NOT NULL DEFAULT '', `modelResponseText` TEXT NOT NULL DEFAULT '', `timestamp` INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_self_check_failure_logs_botId` ON `self_check_failure_logs` (`botId`)")
    }
}

val MIGRATION_21_22 = object : Migration(21, 22) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `bots` ADD COLUMN `emotionalRegulationCapacity` INTEGER NOT NULL DEFAULT 50")
        db.execSQL("CREATE TABLE IF NOT EXISTS `sensitive_triggers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `triggerTopic` TEXT NOT NULL, `sourceDescription` TEXT NOT NULL, `intensityMultiplier` REAL NOT NULL DEFAULT 2.5, `recentTriggerCount` INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_sensitive_triggers_botId` ON `sensitive_triggers` (`botId`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `pending_reappraisals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `originalEventDescription` TEXT NOT NULL, `originalEmotionState` TEXT NOT NULL, `triggeredAtMessageIndex` INTEGER NOT NULL, `triggeredAtTimestamp` INTEGER NOT NULL, `reappraisalEligibleAtMessageIndex` INTEGER NOT NULL, `reappraisalEligibleAtTimestamp` INTEGER NOT NULL, `resolved` INTEGER NOT NULL DEFAULT 0, `expired` INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_pending_reappraisals_botId` ON `pending_reappraisals` (`botId`)")
    }
}

val MIGRATION_22_23 = object : Migration(22, 23) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `memory_events` ADD COLUMN `isBotSaved` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE `memory_facts` ADD COLUMN `isBotSaved` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("CREATE TABLE IF NOT EXISTS `active_memory_call_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `action` TEXT NOT NULL, `content` TEXT NOT NULL, `category` TEXT NOT NULL DEFAULT 'fact', `importance` INTEGER NOT NULL DEFAULT 5, `timestamp` INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_active_memory_call_logs_botId` ON `active_memory_call_logs` (`botId`)")
    }
}

val MIGRATION_23_24 = object : Migration(23, 24) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE `bots` ADD COLUMN `storyCalendarDate` TEXT NOT NULL DEFAULT '2026-08-29'")
        db.execSQL("ALTER TABLE `bots` ADD COLUMN `storyDayCounter` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE `bots` ADD COLUMN `birthDate` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `bots` ADD COLUMN `currentAge` INTEGER NOT NULL DEFAULT 20")
        db.execSQL("ALTER TABLE `bots` ADD COLUMN `initialAge` INTEGER NOT NULL DEFAULT 20")
        db.execSQL("ALTER TABLE `bots` ADD COLUMN `lastMessageTimestamp` INTEGER NOT NULL DEFAULT 0")

        db.execSQL("ALTER TABLE `cast_members` ADD COLUMN `birthDate` TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE `cast_members` ADD COLUMN `currentAge` INTEGER NOT NULL DEFAULT 20")
        db.execSQL("ALTER TABLE `cast_members` ADD COLUMN `initialAge` INTEGER NOT NULL DEFAULT 20")

        db.execSQL("ALTER TABLE `memory_events` ADD COLUMN `realWorldTimestamp` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `memory_events` ADD COLUMN `storyDayIndex` INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE `memory_events` ADD COLUMN `storyCalendarDateStr` TEXT NOT NULL DEFAULT ''")

        db.execSQL("CREATE TABLE IF NOT EXISTS `time_perception_mismatch_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `botId` TEXT NOT NULL, `userMessageText` TEXT NOT NULL, `modelResponseText` TEXT NOT NULL, `detectedTimeExpression` TEXT NOT NULL, `codeCalculatedDays` INTEGER NOT NULL, `mismatchReason` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_time_mismatch_botId` ON `time_perception_mismatch_logs` (`botId`)")
    }
}

val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `custom_providers` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`label` TEXT NOT NULL, " +
                "`baseUrl` TEXT NOT NULL, " +
                "`apiKeyEncrypted` TEXT NOT NULL, " +
                "`modelName` TEXT NOT NULL, " +
                "`apiFormat` TEXT NOT NULL DEFAULT 'openai', " +
                "`supportsFunctionCalling` INTEGER NOT NULL DEFAULT 1, " +
                "`createdAt` INTEGER NOT NULL" +
                ")"
        )
    }
}

@Database(
    entities = [
        BotEntity::class,
        MessageEntity::class,
        UserSettingsEntity::class,
        MemoryFragmentEntity::class,
        MemoryFragmentFtsEntity::class,
        CharacterEmotionEntity::class,
        StoryProgressEntity::class,
        AffectionEventEntity::class,
        PromptViolationLogEntity::class,
        SceneTemplateEntity::class,
        MemoryFactEntity::class,
        MemoryEventEntity::class,
        EntityRegistryEntity::class,
        MemoryCheckpointEntity::class,
        CastMemberEntity::class,
        EmotionHistoryEntity::class,
        SelfCheckFailureLogEntity::class,
        SensitiveTriggerEntity::class,
        PendingReappraisalEntity::class,
        ActiveMemoryCallLogEntity::class,
        TimePerceptionMismatchLogEntity::class,
        CustomProviderEntity::class
    ],
    version = 25,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun botDao(): BotDao
    abstract fun messageDao(): MessageDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun memoryFragmentDao(): MemoryFragmentDao
    abstract fun characterEmotionDao(): CharacterEmotionDao
    abstract fun storyProgressDao(): StoryProgressDao
    abstract fun affectionEventDao(): AffectionEventDao
    abstract fun promptViolationLogDao(): PromptViolationLogDao
    abstract fun sceneTemplateDao(): SceneTemplateDao
    abstract fun memoryFactDao(): MemoryFactDao
    abstract fun memoryEventDao(): MemoryEventDao
    abstract fun entityRegistryDao(): EntityRegistryDao
    abstract fun memoryCheckpointDao(): MemoryCheckpointDao
    abstract fun castMemberDao(): CastMemberDao
    abstract fun emotionHistoryDao(): EmotionHistoryDao
    abstract fun selfCheckFailureLogDao(): SelfCheckFailureLogDao
    abstract fun sensitiveTriggerDao(): SensitiveTriggerDao
    abstract fun pendingReappraisalDao(): PendingReappraisalDao
    abstract fun activeMemoryCallLogDao(): ActiveMemoryCallLogDao
    abstract fun timePerceptionMismatchLogDao(): TimePerceptionMismatchLogDao
    abstract fun customProviderDao(): CustomProviderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "emochi_database"
                )
                    .addMigrations(
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12,
                        MIGRATION_12_13,
                        MIGRATION_13_14,
                        MIGRATION_14_15,
                        MIGRATION_15_16,
                        MIGRATION_16_17,
                        MIGRATION_17_18,
                        MIGRATION_18_19,
                        MIGRATION_19_20,
                        MIGRATION_20_21,
                        MIGRATION_21_22,
                        MIGRATION_22_23,
                        MIGRATION_23_24,
                        MIGRATION_24_25
                    )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
