package com.maplume.blockwise.core.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.maplume.blockwise.core.data.dao.ActivityTypeDao
import com.maplume.blockwise.core.data.dao.GoalDao
import com.maplume.blockwise.core.data.dao.StatisticsDao
import com.maplume.blockwise.core.data.dao.TagDao
import com.maplume.blockwise.core.data.dao.TimeEntryDao
import com.maplume.blockwise.core.data.dao.TimeEntryTagDao
import com.maplume.blockwise.core.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            val now = System.currentTimeMillis()

            database.execSQL("ALTER TABLE activity_types ADD COLUMN created_at INTEGER NOT NULL DEFAULT $now")
            database.execSQL("ALTER TABLE activity_types ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $now")

            database.execSQL("ALTER TABLE tags ADD COLUMN created_at INTEGER NOT NULL DEFAULT $now")
            database.execSQL("ALTER TABLE tags ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $now")

            database.execSQL("ALTER TABLE goals ADD COLUMN created_at INTEGER NOT NULL DEFAULT $now")
            database.execSQL("ALTER TABLE goals ADD COLUMN updated_at INTEGER NOT NULL DEFAULT $now")

            database.execSQL("CREATE INDEX IF NOT EXISTS `index_activity_types_display_order` ON `activity_types` (`display_order`)")
            database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_tags_name` ON `tags` (`name`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_goals_is_active` ON `goals` (`is_active`)")

            database.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `time_entry_tags_new` (
                    `entry_id` INTEGER NOT NULL,
                    `tag_id` INTEGER NOT NULL,
                    PRIMARY KEY(`entry_id`, `tag_id`),
                    FOREIGN KEY(`entry_id`) REFERENCES `time_entries`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`tag_id`) REFERENCES `tags`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            database.execSQL(
                """
                INSERT INTO time_entry_tags_new(entry_id, tag_id)
                SELECT entry_id, tag_id FROM time_entry_tags
                """.trimIndent()
            )
            database.execSQL("DROP TABLE time_entry_tags")
            database.execSQL("ALTER TABLE time_entry_tags_new RENAME TO time_entry_tags")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_entry_tags_entry_id` ON `time_entry_tags` (`entry_id`)")
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_time_entry_tags_tag_id` ON `time_entry_tags` (`tag_id`)")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideTimeEntryDao(database: AppDatabase): TimeEntryDao {
        return database.timeEntryDao()
    }

    @Provides
    @Singleton
    fun provideActivityTypeDao(database: AppDatabase): ActivityTypeDao {
        return database.activityTypeDao()
    }

    @Provides
    @Singleton
    fun provideTagDao(database: AppDatabase): TagDao {
        return database.tagDao()
    }

    @Provides
    @Singleton
    fun provideTimeEntryTagDao(database: AppDatabase): TimeEntryTagDao {
        return database.timeEntryTagDao()
    }

    @Provides
    @Singleton
    fun provideGoalDao(database: AppDatabase): GoalDao {
        return database.goalDao()
    }

    @Provides
    @Singleton
    fun provideStatisticsDao(database: AppDatabase): StatisticsDao {
        return database.statisticsDao()
    }
}
