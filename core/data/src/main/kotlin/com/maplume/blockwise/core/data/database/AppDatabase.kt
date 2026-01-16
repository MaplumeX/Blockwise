package com.maplume.blockwise.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.maplume.blockwise.core.data.database.converter.Converters
import com.maplume.blockwise.core.data.database.converter.EnumConverters
import com.maplume.blockwise.core.data.dao.ActivityTypeDao
import com.maplume.blockwise.core.data.dao.GoalDao
import com.maplume.blockwise.core.data.dao.StatisticsDao
import com.maplume.blockwise.core.data.dao.TagDao
import com.maplume.blockwise.core.data.dao.TimeEntryDao
import com.maplume.blockwise.core.data.dao.TimeEntryTagDao
import com.maplume.blockwise.core.data.entity.ActivityTypeEntity
import com.maplume.blockwise.core.data.entity.GoalEntity
import com.maplume.blockwise.core.data.entity.TagEntity
import com.maplume.blockwise.core.data.entity.TimeEntryEntity
import com.maplume.blockwise.core.data.entity.TimeEntryTagEntity

/**
 * Main Room database for Blockwise application.
 */
@Database(
    entities = [
        TimeEntryEntity::class,
        ActivityTypeEntity::class,
        TagEntity::class,
        TimeEntryTagEntity::class,
        GoalEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class, EnumConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timeEntryDao(): TimeEntryDao
    abstract fun activityTypeDao(): ActivityTypeDao
    abstract fun tagDao(): TagDao
    abstract fun timeEntryTagDao(): TimeEntryTagDao
    abstract fun goalDao(): GoalDao
    abstract fun statisticsDao(): StatisticsDao

    companion object {
        const val DATABASE_NAME = "blockwise_database"
    }
}
