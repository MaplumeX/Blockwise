package com.maplume.blockwise.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.maplume.blockwise.core.data.database.converter.DateTimeConverters
import com.maplume.blockwise.core.data.entity.ActivityTypeEntity
import com.maplume.blockwise.core.data.entity.GoalEntity
import com.maplume.blockwise.core.data.entity.TagEntity
import com.maplume.blockwise.core.data.entity.TimeEntryEntity
import com.maplume.blockwise.core.data.entity.TimeEntryTagCrossRef
import com.maplume.blockwise.core.data.dao.ActivityTypeDao
import com.maplume.blockwise.core.data.dao.GoalDao
import com.maplume.blockwise.core.data.dao.TagDao
import com.maplume.blockwise.core.data.dao.TimeEntryDao

/**
 * Main Room database for Blockwise application.
 */
@Database(
    entities = [
        TimeEntryEntity::class,
        ActivityTypeEntity::class,
        TagEntity::class,
        TimeEntryTagCrossRef::class,
        GoalEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timeEntryDao(): TimeEntryDao
    abstract fun activityTypeDao(): ActivityTypeDao
    abstract fun tagDao(): TagDao
    abstract fun goalDao(): GoalDao

    companion object {
        const val DATABASE_NAME = "blockwise_database"
    }
}
