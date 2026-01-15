package com.maplume.blockwise.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maplume.blockwise.core.data.entity.TimeEntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * Data Access Object for time entries.
 */
@Dao
interface TimeEntryDao {

    @Query("SELECT * FROM time_entries ORDER BY start_time DESC")
    fun getAllTimeEntries(): Flow<List<TimeEntryEntity>>

    @Query("SELECT * FROM time_entries WHERE id = :id")
    suspend fun getTimeEntryById(id: Long): TimeEntryEntity?

    @Query("SELECT * FROM time_entries WHERE start_time >= :startTime AND end_time <= :endTime ORDER BY start_time")
    fun getTimeEntriesInRange(startTime: Instant, endTime: Instant): Flow<List<TimeEntryEntity>>

    @Query("SELECT * FROM time_entries WHERE activity_id = :activityId ORDER BY start_time DESC")
    fun getTimeEntriesByActivityId(activityId: Long): Flow<List<TimeEntryEntity>>

    @Query("""
        SELECT * FROM time_entries
        WHERE start_time >= :startTime AND end_time <= :endTime
        ORDER BY start_time DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getTimeEntriesPaged(
        startTime: Instant,
        endTime: Instant,
        limit: Int,
        offset: Int
    ): List<TimeEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntry(entry: TimeEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntries(entries: List<TimeEntryEntity>)

    @Update
    suspend fun updateTimeEntry(entry: TimeEntryEntity)

    @Delete
    suspend fun deleteTimeEntry(entry: TimeEntryEntity)

    @Query("DELETE FROM time_entries WHERE id = :id")
    suspend fun deleteTimeEntryById(id: Long)

    @Query("SELECT COUNT(*) FROM time_entries")
    suspend fun getTimeEntryCount(): Int

    @Query("""
        SELECT SUM(duration_minutes) FROM time_entries
        WHERE start_time >= :startTime AND end_time <= :endTime
    """)
    suspend fun getTotalDurationInRange(startTime: Instant, endTime: Instant): Int?
}
