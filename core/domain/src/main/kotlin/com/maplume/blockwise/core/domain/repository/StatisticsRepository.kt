package com.maplume.blockwise.core.domain.repository

import com.maplume.blockwise.core.domain.model.CategoryStatistics
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.maplume.blockwise.core.domain.model.HourlyPattern
import com.maplume.blockwise.core.domain.model.StatisticsSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

interface StatisticsRepository {
    fun getStatsByActivityType(startTime: Instant, endTime: Instant): Flow<List<CategoryStatistics>>

    fun getStatsByTag(startTime: Instant, endTime: Instant): Flow<List<CategoryStatistics>>

    fun getDailyTrends(startTime: Instant, endTime: Instant): Flow<List<DailyTrend>>

    fun getHourlyDistribution(startTime: Instant, endTime: Instant): Flow<List<HourlyPattern>>

    suspend fun getTotalStats(startTime: Instant, endTime: Instant): StatisticsSummary

    suspend fun getTotalMinutesForTag(tagId: Long, startTime: Instant, endTime: Instant): Int

    fun getDailyTrendsForTag(tagId: Long, startTime: Instant, endTime: Instant): Flow<List<DailyTrend>>
}

