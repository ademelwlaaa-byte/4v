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
        SceneTemplateEntity::class
    ],
    version = 19,
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
                        MIGRATION_18_19
                    )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
