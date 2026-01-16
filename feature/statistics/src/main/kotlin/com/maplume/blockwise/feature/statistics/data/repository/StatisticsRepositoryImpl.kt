package com.maplume.blockwise.feature.statistics.data.repository

import com.maplume.blockwise.core.data.dao.StatisticsDao
import com.maplume.blockwise.core.data.mapper.toDomain
import com.maplume.blockwise.core.domain.model.CategoryStatistics
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.maplume.blockwise.core.domain.model.HourlyPattern
import com.maplume.blockwise.core.domain.model.StatisticsSummary
import com.maplume.blockwise.core.domain.repository.StatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import javax.inject.Inject

class StatisticsRepositoryImpl @Inject constructor(
    private val dao: StatisticsDao
) : StatisticsRepository {

    override fun getStatsByActivityType(
        startTime: Instant,
        endTime: Instant
    ): Flow<List<CategoryStatistics>> {
        return dao.getStatsByActivityType(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun getStatsByTag(startTime: Instant, endTime: Instant): Flow<List<CategoryStatistics>> {
        return dao.getStatsByTag(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun getDailyTrends(startTime: Instant, endTime: Instant): Flow<List<DailyTrend>> {
        return dao.getDailyStats(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun getHourlyDistribution(startTime: Instant, endTime: Instant): Flow<List<HourlyPattern>> {
        return dao.getHourlyDistribution(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getTotalStats(startTime: Instant, endTime: Instant): StatisticsSummary {
        return withContext(Dispatchers.IO) {
            dao.getTotalStats(
                startTime.toEpochMilliseconds(),
                endTime.toEpochMilliseconds()
            ).toDomain()
        }
    }

    override suspend fun getTotalMinutesForTag(tagId: Long, startTime: Instant, endTime: Instant): Int {
        return withContext(Dispatchers.IO) {
            dao.getTotalMinutesForTag(
                tagId,
                startTime.toEpochMilliseconds(),
                endTime.toEpochMilliseconds()
            )
        }
    }

    override fun getDailyTrendsForTag(
        tagId: Long,
        startTime: Instant,
        endTime: Instant
    ): Flow<List<DailyTrend>> {
        return dao.getDailyStatsForTag(
            tagId,
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).map { list -> list.map { it.toDomain() } }
    }
}

