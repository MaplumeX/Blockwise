package com.maplume.blockwise.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.maplume.blockwise.core.data.entity.TimeEntryEntity
import com.maplume.blockwise.core.data.entity.TimeEntryWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: TimeEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<TimeEntryEntity>): List<Long>

    @Update
    suspend fun update(entry: TimeEntryEntity)

    @Delete
    suspend fun delete(entry: TimeEntryEntity)

    @Query("DELETE FROM time_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM time_entries WHERE id = :id")
    suspend fun getById(id: Long): TimeEntryEntity?

    @Transaction
    @Query("SELECT * FROM time_entries WHERE id = :id")
    suspend fun getByIdWithDetails(id: Long): TimeEntryWithDetails?

    @Transaction
    @Query("SELECT * FROM time_entries WHERE id = :id")
    fun getByIdWithDetailsFlow(id: Long): Flow<TimeEntryWithDetails?>

    @Query(
        """
        SELECT * FROM time_entries
        WHERE start_time >= :startTime AND start_time < :endTime
        ORDER BY start_time DESC
        """
    )
    fun getByTimeRange(startTime: Long, endTime: Long): Flow<List<TimeEntryEntity>>

    @Transaction
    @Query(
        """
        SELECT * FROM time_entries
        WHERE start_time >= :startTime AND start_time < :endTime
        ORDER BY start_time DESC
        """
    )
    fun getByTimeRangeWithDetails(startTime: Long, endTime: Long): Flow<List<TimeEntryWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM time_entries
        WHERE start_time < :endTime AND end_time > :startTime
        ORDER BY start_time ASC
        """
    )
    fun getOverlappingWithDetails(startTime: Long, endTime: Long): Flow<List<TimeEntryWithDetails>>

    @Transaction
    @Query(
        """
        SELECT * FROM time_entries
        WHERE start_time >= :dayStart AND start_time < :dayEnd
        ORDER BY start_time ASC
        """
    )
    fun getByDay(dayStart: Long, dayEnd: Long): Flow<List<TimeEntryWithDetails>>

    @Query(
        """
        SELECT * FROM time_entries
        WHERE activity_id = :activityId
        ORDER BY start_time DESC
        """
    )
    fun getByActivityType(activityId: Long): Flow<List<TimeEntryEntity>>

    @Transaction
    @Query(
        """
        SELECT * FROM time_entries
        ORDER BY start_time DESC
        LIMIT :limit OFFSET :offset
        """
    )
    fun getRecentWithDetails(limit: Int, offset: Int): Flow<List<TimeEntryWithDetails>>

    @Query(
        """
        SELECT * FROM time_entries
        WHERE id != :excludeId
          AND ((start_time < :endTime AND end_time > :startTime))
        LIMIT 1
        """
    )
    suspend fun findOverlapping(startTime: Long, endTime: Long, excludeId: Long = 0): TimeEntryEntity?

    @Query(
        """
        SELECT COUNT(*) FROM time_entries
        WHERE start_time >= :startTime AND start_time < :endTime
        """
    )
    suspend fun countByTimeRange(startTime: Long, endTime: Long): Int

    @Query(
        """
        SELECT COALESCE(SUM(duration_minutes), 0) FROM time_entries
        WHERE start_time >= :startTime AND start_time < :endTime
        """
    )
    suspend fun getTotalDurationByTimeRange(startTime: Long, endTime: Long): Int

    @Transaction
    @Query("SELECT * FROM time_entries ORDER BY end_time DESC LIMIT 1")
    suspend fun getLatest(): TimeEntryWithDetails?

    @Transaction
    @Query("SELECT * FROM time_entries ORDER BY start_time ASC")
    fun getAllWithDetails(): Flow<List<TimeEntryWithDetails>>
}

