package com.maplume.blockwise.core.data.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class ActivityStatistics(
    @ColumnInfo(name = "activity_id")
    val activityId: Long,
    @ColumnInfo(name = "activity_name")
    val activityName: String,
    @ColumnInfo(name = "color_hex")
    val colorHex: String,
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Int,
    @ColumnInfo(name = "entry_count")
    val entryCount: Int
)

data class TagStatistics(
    @ColumnInfo(name = "tag_id")
    val tagId: Long,
    @ColumnInfo(name = "tag_name")
    val tagName: String,
    @ColumnInfo(name = "color_hex")
    val colorHex: String,
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Int,
    @ColumnInfo(name = "entry_count")
    val entryCount: Int
)

data class DailyStatistics(
    @ColumnInfo(name = "date_millis")
    val dateMillis: Long,
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Int,
    @ColumnInfo(name = "entry_count")
    val entryCount: Int
)

data class HourlyDistribution(
    @ColumnInfo(name = "hour")
    val hour: Int,
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Int
)

data class TotalStatistics(
    @ColumnInfo(name = "total_minutes")
    val totalMinutes: Int,
    @ColumnInfo(name = "entry_count")
    val entryCount: Int
)

@Dao
interface StatisticsDao {

    @Query(
        """
        SELECT
            te.activity_id,
            at.name AS activity_name,
            at.color_hex,
            COALESCE(SUM(te.duration_minutes), 0) AS total_minutes,
            COUNT(te.id) AS entry_count
        FROM time_entries te
        JOIN activity_types at ON te.activity_id = at.id
        WHERE te.start_time >= :startTime AND te.start_time < :endTime
        GROUP BY te.activity_id
        ORDER BY total_minutes DESC
        """
    )
    fun getStatsByActivityType(startTime: Long, endTime: Long): Flow<List<ActivityStatistics>>

    @Query(
        """
        SELECT
            t.id AS tag_id,
            t.name AS tag_name,
            t.color_hex,
            COALESCE(SUM(te.duration_minutes), 0) AS total_minutes,
            COUNT(DISTINCT te.id) AS entry_count
        FROM time_entries te
        JOIN time_entry_tags tet ON te.id = tet.entry_id
        JOIN tags t ON tet.tag_id = t.id
        WHERE te.start_time >= :startTime AND te.start_time < :endTime
        GROUP BY t.id
        ORDER BY total_minutes DESC
        """
    )
    fun getStatsByTag(startTime: Long, endTime: Long): Flow<List<TagStatistics>>

    @Query(
        """
        SELECT
            CAST((te.start_time / 3600000) % 24 AS INTEGER) AS hour,
            COALESCE(SUM(te.duration_minutes), 0) AS total_minutes
        FROM time_entries te
        WHERE te.start_time >= :startTime AND te.start_time < :endTime
        GROUP BY hour
        ORDER BY hour ASC
        """
    )
    fun getHourlyDistribution(startTime: Long, endTime: Long): Flow<List<HourlyDistribution>>

    @Query(
        """
        SELECT
            COALESCE(SUM(duration_minutes), 0) AS total_minutes,
            COUNT(*) AS entry_count
        FROM time_entries
        WHERE start_time >= :startTime AND start_time < :endTime
        """
    )
    suspend fun getTotalStats(startTime: Long, endTime: Long): TotalStatistics

    @Query(
        """
        SELECT
            COALESCE(SUM(te.duration_minutes), 0)
        FROM time_entries te
        JOIN time_entry_tags tet ON te.id = tet.entry_id
        WHERE tet.tag_id = :tagId
          AND te.start_time >= :startTime
          AND te.start_time < :endTime
        """
    )
    suspend fun getTotalMinutesForTag(tagId: Long, startTime: Long, endTime: Long): Int

    @Query(
        """
        SELECT
            (te.start_time / 86400000) * 86400000 AS date_millis,
            COALESCE(SUM(te.duration_minutes), 0) AS total_minutes,
            COUNT(DISTINCT te.id) AS entry_count
        FROM time_entries te
        JOIN time_entry_tags tet ON te.id = tet.entry_id
        WHERE tet.tag_id = :tagId
          AND te.start_time >= :startTime
          AND te.start_time < :endTime
        GROUP BY date_millis
        ORDER BY date_millis ASC
        """
    )
    fun getDailyStatsForTag(tagId: Long, startTime: Long, endTime: Long): Flow<List<DailyStatistics>>
}

