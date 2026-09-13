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

val MIGRATION_25_26 = object : Migration(25, 26) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_settings ADD COLUMN enableLlm7 INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE messages ADD COLUMN provider TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_26_27 = object : Migration(26, 27) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `empty_response_logs` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`botId` TEXT NOT NULL, " +
                "`provider` TEXT NOT NULL, " +
                "`model` TEXT NOT NULL, " +
                "`finishReason` TEXT NOT NULL, " +
                "`rawLength` INTEGER NOT NULL, " +
                "`errorMessage` TEXT NOT NULL, " +
                "`timestamp` INTEGER NOT NULL" +
                ")"
        )
    }
}

val MIGRATION_27_28 = object : Migration(27, 28) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE user_settings ADD COLUMN enablePollinations INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE user_settings ADD COLUMN enableOpencodeZen INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE user_settings ADD COLUMN enableOvh INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE user_settings ADD COLUMN pollinationsModel TEXT NOT NULL DEFAULT 'openai'")
        db.execSQL("ALTER TABLE user_settings ADD COLUMN opencodeZenModel TEXT NOT NULL DEFAULT 'deepseek-v4-flash-free'")
        db.execSQL("ALTER TABLE user_settings ADD COLUMN ovhModel TEXT NOT NULL DEFAULT 'meta-llama/Meta-Llama-3-70B-Instruct'")
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
        CustomProviderEntity::class,
        EmptyResponseLogEntity::class,
        MalformedOutputLogEntity::class,
        EntityRelationEntity::class,
        ImplicitCorrectionLogEntity::class,
        ArchivedMemoryEntity::class
    ],
    version = 35,
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
    abstract fun emptyResponseLogDao(): EmptyResponseLogDao
    abstract fun malformedOutputLogDao(): MalformedOutputLogDao
    abstract fun entityRelationDao(): EntityRelationDao
    abstract fun implicitCorrectionLogDao(): ImplicitCorrectionLogDao
    abstract fun archivedMemoryDao(): ArchivedMemoryDao

    companion object {
        val MIGRATION_28_29 = object : Migration(28, 29) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_settings ADD COLUMN fallbackChainOrder TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_29_30 = object : Migration(29, 30) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE user_settings ADD COLUMN openRouterApiKey TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN openRouterModel TEXT NOT NULL DEFAULT 'deepseek/deepseek-chat'")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN nvidiaApiKey TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN nvidiaModel TEXT NOT NULL DEFAULT 'deepseek-ai/deepseek-v4-flash'")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN githubPatToken TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN githubModel TEXT NOT NULL DEFAULT 'openai/gpt-4o'")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN mistralApiKey TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN mistralModel TEXT NOT NULL DEFAULT 'mistral-large-latest'")
            }
        }

        val MIGRATION_30_31 = object : Migration(30, 31) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `malformed_output_logs` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`botId` TEXT NOT NULL, " +
                        "`provider` TEXT NOT NULL, " +
                        "`model` TEXT NOT NULL, " +
                        "`rawOutput` TEXT NOT NULL, " +
                        "`errorMessage` TEXT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL" +
                        ")"
                )
                db.execSQL("ALTER TABLE user_settings ADD COLUMN opencodeZenApiKey TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN geminiModel TEXT NOT NULL DEFAULT 'gemini-2.5-flash'")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN claudeModel TEXT NOT NULL DEFAULT 'claude-3-5-sonnet-20241022'")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN groqModel TEXT NOT NULL DEFAULT 'llama-3.3-70b-versatile'")
                db.execSQL("ALTER TABLE user_settings ADD COLUMN openaiModel TEXT NOT NULL DEFAULT 'gpt-4o'")
            }
        }

        val MIGRATION_31_32 = object : Migration(31, 32) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_settings_new` (" +
                        "`id` INTEGER PRIMARY KEY NOT NULL, " +
                        "`customApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`groqApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`claudeApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`openaiApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`backupApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`selectedProvider` TEXT NOT NULL DEFAULT 'gemini', " +
                        "`selectedModel` TEXT NOT NULL DEFAULT 'gemini-2.5-flash', " +
                        "`fallbackModel` TEXT NOT NULL DEFAULT 'gemini-2.5-flash', " +
                        "`responseLength` TEXT NOT NULL DEFAULT 'standard', " +
                        "`enableNsfw` INTEGER NOT NULL DEFAULT 1, " +
                        "`enableOoc` INTEGER NOT NULL DEFAULT 1, " +
                        "`enableFlirty` INTEGER NOT NULL DEFAULT 1, " +
                        "`enableHardcore` INTEGER NOT NULL DEFAULT 1, " +
                        "`enableFetish` INTEGER NOT NULL DEFAULT 0, " +
                        "`enableDarkRp` INTEGER NOT NULL DEFAULT 0, " +
                        "`enableSweet` INTEGER NOT NULL DEFAULT 0, " +
                        "`enablePrimal` INTEGER NOT NULL DEFAULT 0, " +
                        "`enableAutoFallback` INTEGER NOT NULL DEFAULT 1, " +
                        "`enableTts` INTEGER NOT NULL DEFAULT 1, " +
                        "`ttsSpeed` REAL NOT NULL DEFAULT 1.0, " +
                        "`ttsPitch` REAL NOT NULL DEFAULT 1.0, " +
                        "`selectedVoiceName` TEXT NOT NULL DEFAULT '', " +
                        "`appLanguage` TEXT NOT NULL DEFAULT 'tr', " +
                        "`totalPromptTokens` INTEGER NOT NULL DEFAULT 0, " +
                        "`totalCandidateTokens` INTEGER NOT NULL DEFAULT 0, " +
                        "`enableLlm7` INTEGER NOT NULL DEFAULT 0, " +
                        "`enablePollinations` INTEGER NOT NULL DEFAULT 1, " +
                        "`enableOpencodeZen` INTEGER NOT NULL DEFAULT 1, " +
                        "`pollinationsModel` TEXT NOT NULL DEFAULT 'openai', " +
                        "`opencodeZenApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`opencodeZenModel` TEXT NOT NULL DEFAULT 'deepseek-v4-flash-free', " +
                        "`fallbackChainOrder` TEXT NOT NULL DEFAULT '', " +
                        "`openRouterApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`openRouterModel` TEXT NOT NULL DEFAULT 'deepseek/deepseek-chat', " +
                        "`nvidiaApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`nvidiaModel` TEXT NOT NULL DEFAULT 'deepseek-ai/deepseek-v4-flash', " +
                        "`mistralApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`mistralModel` TEXT NOT NULL DEFAULT 'mistral-large-latest', " +
                        "`geminiModel` TEXT NOT NULL DEFAULT 'gemini-2.5-flash', " +
                        "`claudeModel` TEXT NOT NULL DEFAULT 'claude-3-5-sonnet-20241022', " +
                        "`groqModel` TEXT NOT NULL DEFAULT 'llama-3.3-70b-versatile', " +
                        "`openaiModel` TEXT NOT NULL DEFAULT 'gpt-4o'" +
                        ")"
                )

                db.execSQL(
                    "INSERT INTO `user_settings_new` (" +
                        "id, customApiKey, groqApiKey, claudeApiKey, openaiApiKey, backupApiKey, " +
                        "selectedProvider, selectedModel, fallbackModel, responseLength, " +
                        "enableNsfw, enableOoc, enableFlirty, enableHardcore, enableFetish, " +
                        "enableDarkRp, enableSweet, enablePrimal, enableAutoFallback, " +
                        "enableTts, ttsSpeed, ttsPitch, selectedVoiceName, appLanguage, " +
                        "totalPromptTokens, totalCandidateTokens, enableLlm7, enablePollinations, " +
                        "enableOpencodeZen, pollinationsModel, opencodeZenApiKey, opencodeZenModel, " +
                        "fallbackChainOrder, openRouterApiKey, openRouterModel, nvidiaApiKey, " +
                        "nvidiaModel, mistralApiKey, mistralModel, geminiModel, claudeModel, " +
                        "groqModel, openaiModel" +
                        ") SELECT " +
                        "id, customApiKey, groqApiKey, claudeApiKey, openaiApiKey, backupApiKey, " +
                        "selectedProvider, selectedModel, fallbackModel, responseLength, " +
                        "enableNsfw, enableOoc, enableFlirty, enableHardcore, enableFetish, " +
                        "enableDarkRp, enableSweet, enablePrimal, enableAutoFallback, " +
                        "enableTts, ttsSpeed, ttsPitch, selectedVoiceName, appLanguage, " +
                        "totalPromptTokens, totalCandidateTokens, enableLlm7, enablePollinations, " +
                        "enableOpencodeZen, pollinationsModel, " +
                        "COALESCE(opencodeZenApiKey, ''), COALESCE(opencodeZenModel, 'deepseek-v4-flash-free'), " +
                        "COALESCE(fallbackChainOrder, ''), COALESCE(openRouterApiKey, ''), COALESCE(openRouterModel, 'deepseek/deepseek-chat'), " +
                        "COALESCE(nvidiaApiKey, ''), COALESCE(nvidiaModel, 'deepseek-ai/deepseek-v4-flash'), " +
                        "COALESCE(mistralApiKey, ''), COALESCE(mistralModel, 'mistral-large-latest'), " +
                        "COALESCE(geminiModel, 'gemini-2.5-flash'), COALESCE(claudeModel, 'claude-3-5-sonnet-20241022'), " +
                        "COALESCE(groqModel, 'llama-3.3-70b-versatile'), COALESCE(openaiModel, 'gpt-4o') " +
                        "FROM `user_settings`"
                )

                db.execSQL("DROP TABLE `user_settings`")
                db.execSQL("ALTER TABLE `user_settings_new` RENAME TO `user_settings`")
            }
        }

        val MIGRATION_32_33 = object : Migration(32, 33) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE user_settings SET selectedProvider = 'gemini' WHERE selectedProvider = 'opencode_zen'")
                db.execSQL("UPDATE user_settings SET fallbackChainOrder = REPLACE(fallbackChainOrder, 'opencode_zen', '') WHERE fallbackChainOrder LIKE '%opencode_zen%'")
            }
        }

        val MIGRATION_33_34 = object : Migration(33, 34) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val existingColumns = mutableSetOf<String>()
                db.query("PRAGMA table_info(`user_settings`)").use { cursor ->
                    val nameIdx = cursor.getColumnIndex("name")
                    if (nameIdx != -1) {
                        while (cursor.moveToNext()) {
                            existingColumns.add(cursor.getString(nameIdx))
                        }
                    }
                }

                fun colExpr(col: String, defaultSql: String): String {
                    return if (existingColumns.contains(col)) "`$col`" else "$defaultSql AS `$col`"
                }

                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `user_settings_v34` (" +
                        "`id` INTEGER PRIMARY KEY NOT NULL, " +
                        "`customApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`groqApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`claudeApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`openaiApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`backupApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`selectedProvider` TEXT NOT NULL DEFAULT 'gemini', " +
                        "`selectedModel` TEXT NOT NULL DEFAULT 'gemini-2.5-flash', " +
                        "`fallbackModel` TEXT NOT NULL DEFAULT 'gemini-2.5-flash', " +
                        "`responseLength` TEXT NOT NULL DEFAULT 'standard', " +
                        "`enableNsfw` INTEGER NOT NULL DEFAULT 1, " +
                        "`enableOoc` INTEGER NOT NULL DEFAULT 1, " +
                        "`enableFlirty` INTEGER NOT NULL DEFAULT 1, " +
                        "`enableHardcore` INTEGER NOT NULL DEFAULT 1, " +
                        "`enableFetish` INTEGER NOT NULL DEFAULT 0, " +
                        "`enableDarkRp` INTEGER NOT NULL DEFAULT 0, " +
                        "`enableSweet` INTEGER NOT NULL DEFAULT 0, " +
                        "`enablePrimal` INTEGER NOT NULL DEFAULT 0, " +
                        "`enableAutoFallback` INTEGER NOT NULL DEFAULT 1, " +
                        "`enableTts` INTEGER NOT NULL DEFAULT 1, " +
                        "`ttsSpeed` REAL NOT NULL DEFAULT 1.0, " +
                        "`ttsPitch` REAL NOT NULL DEFAULT 1.0, " +
                        "`selectedVoiceName` TEXT NOT NULL DEFAULT '', " +
                        "`appLanguage` TEXT NOT NULL DEFAULT 'tr', " +
                        "`totalPromptTokens` INTEGER NOT NULL DEFAULT 0, " +
                        "`totalCandidateTokens` INTEGER NOT NULL DEFAULT 0, " +
                        "`enableLlm7` INTEGER NOT NULL DEFAULT 0, " +
                        "`enablePollinations` INTEGER NOT NULL DEFAULT 1, " +
                        "`enableOvh` INTEGER NOT NULL DEFAULT 0, " +
                        "`pollinationsModel` TEXT NOT NULL DEFAULT 'openai', " +
                        "`ovhModel` TEXT NOT NULL DEFAULT 'meta-llama/Meta-Llama-3-70B-Instruct', " +
                        "`fallbackChainOrder` TEXT NOT NULL DEFAULT '', " +
                        "`openRouterApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`openRouterModel` TEXT NOT NULL DEFAULT 'deepseek/deepseek-chat', " +
                        "`nvidiaApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`nvidiaModel` TEXT NOT NULL DEFAULT 'deepseek-ai/deepseek-v4-flash', " +
                        "`mistralApiKey` TEXT NOT NULL DEFAULT '', " +
                        "`mistralModel` TEXT NOT NULL DEFAULT 'mistral-large-latest', " +
                        "`geminiModel` TEXT NOT NULL DEFAULT 'gemini-2.5-flash', " +
                        "`claudeModel` TEXT NOT NULL DEFAULT 'claude-3-5-sonnet-20241022', " +
                        "`groqModel` TEXT NOT NULL DEFAULT 'llama-3.3-70b-versatile', " +
                        "`openaiModel` TEXT NOT NULL DEFAULT 'gpt-4o'" +
                        ")"
                )

                val idSel = colExpr("id", "1")
                val customApiKeySel = colExpr("customApiKey", "''")
                val groqApiKeySel = colExpr("groqApiKey", "''")
                val claudeApiKeySel = colExpr("claudeApiKey", "''")
                val openaiApiKeySel = colExpr("openaiApiKey", "''")
                val backupApiKeySel = colExpr("backupApiKey", "''")
                val selectedProviderSel = colExpr("selectedProvider", "'gemini'")
                val selectedModelSel = colExpr("selectedModel", "'gemini-2.5-flash'")
                val fallbackModelSel = colExpr("fallbackModel", "'gemini-2.5-flash'")
                val responseLengthSel = colExpr("responseLength", "'standard'")
                val enableNsfwSel = colExpr("enableNsfw", "1")
                val enableOocSel = colExpr("enableOoc", "1")
                val enableFlirtySel = colExpr("enableFlirty", "1")
                val enableHardcoreSel = colExpr("enableHardcore", "1")
                val enableFetishSel = colExpr("enableFetish", "0")
                val enableDarkRpSel = colExpr("enableDarkRp", "0")
                val enableSweetSel = colExpr("enableSweet", "0")
                val enablePrimalSel = colExpr("enablePrimal", "0")
                val enableAutoFallbackSel = colExpr("enableAutoFallback", "1")
                val enableTtsSel = colExpr("enableTts", "1")
                val ttsSpeedSel = colExpr("ttsSpeed", "1.0")
                val ttsPitchSel = colExpr("ttsPitch", "1.0")
                val selectedVoiceNameSel = colExpr("selectedVoiceName", "''")
                val appLanguageSel = colExpr("appLanguage", "'tr'")
                val totalPromptTokensSel = colExpr("totalPromptTokens", "0")
                val totalCandidateTokensSel = colExpr("totalCandidateTokens", "0")
                val enableLlm7Sel = colExpr("enableLlm7", "0")
                val enablePollinationsSel = colExpr("enablePollinations", "1")
                val enableOvhSel = colExpr("enableOvh", "0")
                val pollinationsModelSel = colExpr("pollinationsModel", "'openai'")
                val ovhModelSel = colExpr("ovhModel", "'meta-llama/Meta-Llama-3-70B-Instruct'")
                val fallbackChainOrderSel = colExpr("fallbackChainOrder", "''")
                val openRouterApiKeySel = colExpr("openRouterApiKey", "''")
                val openRouterModelSel = colExpr("openRouterModel", "'deepseek/deepseek-chat'")
                val nvidiaApiKeySel = colExpr("nvidiaApiKey", "''")
                val nvidiaModelSel = colExpr("nvidiaModel", "'deepseek-ai/deepseek-v4-flash'")
                val mistralApiKeySel = colExpr("mistralApiKey", "''")
                val mistralModelSel = colExpr("mistralModel", "'mistral-large-latest'")
                val geminiModelSel = colExpr("geminiModel", "'gemini-2.5-flash'")
                val claudeModelSel = colExpr("claudeModel", "'claude-3-5-sonnet-20241022'")
                val groqModelSel = colExpr("groqModel", "'llama-3.3-70b-versatile'")
                val openaiModelSel = colExpr("openaiModel", "'gpt-4o'")

                db.execSQL(
                    "INSERT INTO `user_settings_v34` (" +
                        "id, customApiKey, groqApiKey, claudeApiKey, openaiApiKey, backupApiKey, " +
                        "selectedProvider, selectedModel, fallbackModel, responseLength, " +
                        "enableNsfw, enableOoc, enableFlirty, enableHardcore, enableFetish, " +
                        "enableDarkRp, enableSweet, enablePrimal, enableAutoFallback, " +
                        "enableTts, ttsSpeed, ttsPitch, selectedVoiceName, appLanguage, " +
                        "totalPromptTokens, totalCandidateTokens, enableLlm7, enablePollinations, " +
                        "enableOvh, pollinationsModel, ovhModel, " +
                        "fallbackChainOrder, openRouterApiKey, openRouterModel, nvidiaApiKey, " +
                        "nvidiaModel, mistralApiKey, mistralModel, geminiModel, claudeModel, " +
                        "groqModel, openaiModel" +
                        ") SELECT " +
                        "$idSel, $customApiKeySel, $groqApiKeySel, $claudeApiKeySel, $openaiApiKeySel, $backupApiKeySel, " +
                        "$selectedProviderSel, $selectedModelSel, $fallbackModelSel, $responseLengthSel, " +
                        "$enableNsfwSel, $enableOocSel, $enableFlirtySel, $enableHardcoreSel, $enableFetishSel, " +
                        "$enableDarkRpSel, $enableSweetSel, $enablePrimalSel, $enableAutoFallbackSel, " +
                        "$enableTtsSel, $ttsSpeedSel, $ttsPitchSel, $selectedVoiceNameSel, $appLanguageSel, " +
                        "$totalPromptTokensSel, $totalCandidateTokensSel, $enableLlm7Sel, $enablePollinationsSel, " +
                        "$enableOvhSel, $pollinationsModelSel, $ovhModelSel, " +
                        "$fallbackChainOrderSel, $openRouterApiKeySel, $openRouterModelSel, $nvidiaApiKeySel, " +
                        "$nvidiaModelSel, $mistralApiKeySel, $mistralModelSel, $geminiModelSel, $claudeModelSel, " +
                        "$groqModelSel, $openaiModelSel " +
                        "FROM `user_settings`"
                )

                db.execSQL("DROP TABLE `user_settings`")
                db.execSQL("ALTER TABLE `user_settings_v34` RENAME TO `user_settings`")
                db.execSQL("UPDATE user_settings SET selectedProvider = 'gemini' WHERE selectedProvider = 'opencode_zen'")
                db.execSQL("UPDATE user_settings SET fallbackChainOrder = REPLACE(fallbackChainOrder, 'opencode_zen', '') WHERE fallbackChainOrder LIKE '%opencode_zen%'")
            }
        }

        val MIGRATION_34_35 = object : Migration(34, 35) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `entity_relations` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`botId` TEXT NOT NULL, " +
                        "`sourceEntityId` INTEGER NOT NULL DEFAULT 0, " +
                        "`sourceEntityName` TEXT NOT NULL DEFAULT '', " +
                        "`relationType` TEXT NOT NULL, " +
                        "`targetEntityId` INTEGER NOT NULL DEFAULT 0, " +
                        "`targetEntityName` TEXT NOT NULL DEFAULT '', " +
                        "`confidence` TEXT NOT NULL DEFAULT 'certain', " +
                        "`createdAtMessageIndex` INTEGER NOT NULL DEFAULT 0" +
                        ")"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `implicit_correction_logs` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`botId` TEXT NOT NULL, " +
                        "`memoryId` INTEGER NOT NULL, " +
                        "`memoryType` TEXT NOT NULL, " +
                        "`userFeedbackText` TEXT NOT NULL, " +
                        "`previousConfidence` TEXT NOT NULL, " +
                        "`newConfidence` TEXT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL" +
                        ")"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `archived_memories` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`botId` TEXT NOT NULL, " +
                        "`originalMemoryId` INTEGER NOT NULL, " +
                        "`memoryType` TEXT NOT NULL, " +
                        "`content` TEXT NOT NULL, " +
                        "`archivedReason` TEXT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL" +
                        ")"
                )
            }
        }

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
                        MIGRATION_24_25,
                        MIGRATION_25_26,
                        MIGRATION_26_27,
                        MIGRATION_27_28,
                        MIGRATION_28_29,
                        MIGRATION_29_30,
                        MIGRATION_30_31,
                        MIGRATION_31_32,
                        MIGRATION_32_33,
                        MIGRATION_33_34,
                        MIGRATION_34_35
                    )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
