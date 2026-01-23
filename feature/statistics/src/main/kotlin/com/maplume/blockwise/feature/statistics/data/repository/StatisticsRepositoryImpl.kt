package com.maplume.blockwise.feature.statistics.data.repository

import com.maplume.blockwise.core.data.calculator.OverlapStatisticsCalculator
import com.maplume.blockwise.core.data.dao.StatisticsDao
import com.maplume.blockwise.core.data.dao.TimeEntryDao
import com.maplume.blockwise.core.data.mapper.toDomain
import com.maplume.blockwise.core.domain.model.CategoryStatistics
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.maplume.blockwise.core.domain.model.HourlyPattern
import com.maplume.blockwise.core.domain.model.StatisticsSummary
import com.maplume.blockwise.core.domain.time.TimeRange
import com.maplume.blockwise.core.domain.repository.StatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import javax.inject.Inject

class StatisticsRepositoryImpl @Inject constructor(
    private val dao: StatisticsDao,
    private val timeEntryDao: com.maplume.blockwise.core.data.dao.TimeEntryDao
) : StatisticsRepository {

    override fun getStatsByActivityType(
        startTime: Instant,
        endTime: Instant
    ): Flow<List<CategoryStatistics>> {
        val window = TimeRange(startTime = startTime, endTime = endTime)
        return timeEntryDao.getOverlappingWithDetails(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).map { list ->
            val entries = list.map { it.toDomain() }
            val calc = OverlapStatisticsCalculator()

            val totals = calc.activityTotals(entries, window)
            val totalMinutes = totals.values.sumOf { it.first }

            totals.entries
                .mapNotNull { (activityId, pair) ->
                    val (minutes, count) = pair
                    val activity = entries.firstOrNull { it.activityId == activityId }?.activity ?: return@mapNotNull null
                    CategoryStatistics(
                        id = activityId,
                        name = activity.name,
                        colorHex = activity.colorHex,
                        totalMinutes = minutes,
                        entryCount = count,
                        percentage = if (totalMinutes > 0) (minutes * 100f / totalMinutes) else 0f
                    )
                }
                .sortedByDescending { it.totalMinutes }
        }
    }

    override fun getStatsByTag(startTime: Instant, endTime: Instant): Flow<List<CategoryStatistics>> {
        val window = TimeRange(startTime = startTime, endTime = endTime)
        return timeEntryDao.getOverlappingWithDetails(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).map { list ->
            val entries = list.map { it.toDomain() }
            val calc = OverlapStatisticsCalculator()

            val totals = calc.tagTotals(entries, window)
            val totalMinutes = totals.values.sumOf { it.first }

            totals.entries
                .mapNotNull { (tagId, pair) ->
                    val (minutes, count) = pair
                    val tag = entries.asSequence().flatMap { it.tags.asSequence() }.firstOrNull { it.id == tagId }
                        ?: return@mapNotNull null
                    CategoryStatistics(
                        id = tagId,
                        name = tag.name,
                        colorHex = tag.colorHex,
                        totalMinutes = minutes,
                        entryCount = count,
                        percentage = if (totalMinutes > 0) (minutes * 100f / totalMinutes) else 0f
                    )
                }
                .sortedByDescending { it.totalMinutes }
        }
    }

    override fun getDailyTrends(startTime: Instant, endTime: Instant): Flow<List<DailyTrend>> {
        val window = TimeRange(startTime = startTime, endTime = endTime)
        return timeEntryDao.getOverlappingWithDetails(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).map { list ->
            val entries = list.map { it.toDomain() }
            val calc = OverlapStatisticsCalculator()

            calc.dailyTotals(entries, window)
                .map { (date, pair) ->
                    val (minutes, count) = pair
                    DailyTrend(date = date, totalMinutes = minutes, entryCount = count)
                }
        }
    }

    override fun getHourlyDistribution(startTime: Instant, endTime: Instant): Flow<List<HourlyPattern>> {
        val window = TimeRange(startTime = startTime, endTime = endTime)
        return timeEntryDao.getOverlappingWithDetails(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).map { list ->
            val entries = list.map { it.toDomain() }
            val calc = OverlapStatisticsCalculator()

            val byHour = calc.hourlyTotals(entries, window)
            (0..23).map { hour ->
                HourlyPattern(hour = hour, totalMinutes = byHour[hour] ?: 0)
            }
        }
    }

    override suspend fun getTotalStats(startTime: Instant, endTime: Instant): StatisticsSummary {
        val window = TimeRange(startTime = startTime, endTime = endTime)
        return withContext(Dispatchers.IO) {
            val entries = timeEntryDao.getOverlappingWithDetails(
                startTime.toEpochMilliseconds(),
                endTime.toEpochMilliseconds()
            ).first().map { it.toDomain() }

            val calc = OverlapStatisticsCalculator()
            StatisticsSummary(
                totalMinutes = calc.totalMinutes(entries, window),
                entryCount = calc.entryCount(entries, window)
            )
        }
    }

    override suspend fun getTotalMinutesForTag(tagId: Long, startTime: Instant, endTime: Instant): Int {
        val window = TimeRange(startTime = startTime, endTime = endTime)
        return withContext(Dispatchers.IO) {
            val entries = timeEntryDao.getOverlappingWithDetails(
                startTime.toEpochMilliseconds(),
                endTime.toEpochMilliseconds()
            ).first().map { it.toDomain() }

            val calc = OverlapStatisticsCalculator()
            calc.tagTotals(entries, window)[tagId]?.first ?: 0
        }
    }

    override fun getDailyTrendsForTag(
        tagId: Long,
        startTime: Instant,
        endTime: Instant
    ): Flow<List<DailyTrend>> {
        val window = TimeRange(startTime = startTime, endTime = endTime)
        return timeEntryDao.getOverlappingWithDetails(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).map { list ->
            val entries = list.map { it.toDomain() }.filter { entry -> entry.tags.any { it.id == tagId } }
            val calc = OverlapStatisticsCalculator()

            calc.dailyTotals(entries, window)
                .map { (date, pair) ->
                    val (minutes, count) = pair
                    DailyTrend(date = date, totalMinutes = minutes, entryCount = count)
                }
        }
    }
}

