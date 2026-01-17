package com.maplume.blockwise.core.testing.fake

import com.maplume.blockwise.core.domain.model.CategoryStatistics
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.maplume.blockwise.core.domain.model.HourlyPattern
import com.maplume.blockwise.core.domain.model.StatisticsSummary
import com.maplume.blockwise.core.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Instant

/**
 * Fake implementation of StatisticsRepository for testing.
 * Provides pre-configured statistics data.
 */
class FakeStatisticsRepository : StatisticsRepository {

    private val _statsByActivityType = MutableStateFlow<List<CategoryStatistics>>(emptyList())
    private val _statsByTag = MutableStateFlow<List<CategoryStatistics>>(emptyList())
    private val _dailyTrends = MutableStateFlow<List<DailyTrend>>(emptyList())
    private val _hourlyDistribution = MutableStateFlow<List<HourlyPattern>>(emptyList())

    /**
     * Control whether operations should fail.
     */
    var shouldFail = false
    var failureException: Exception = RuntimeException("Test failure")

    /**
     * Pre-configured total stats.
     */
    var totalStats = StatisticsSummary(totalMinutes = 0, entryCount = 0)

    /**
     * Pre-configured total minutes for tags.
     */
    private val totalMinutesForTags = mutableMapOf<Long, Int>()

    /**
     * Pre-configured daily trends for tags.
     */
    private val dailyTrendsForTags = mutableMapOf<Long, MutableStateFlow<List<DailyTrend>>>()

    /**
     * Set statistics by activity type.
     */
    fun setStatsByActivityType(stats: List<CategoryStatistics>) {
        _statsByActivityType.value = stats
    }

    /**
     * Set statistics by tag.
     */
    fun setStatsByTag(stats: List<CategoryStatistics>) {
        _statsByTag.value = stats
    }

    /**
     * Set daily trends.
     */
    fun setDailyTrends(trends: List<DailyTrend>) {
        _dailyTrends.value = trends
    }

    /**
     * Set hourly distribution.
     */
    fun setHourlyDistribution(patterns: List<HourlyPattern>) {
        _hourlyDistribution.value = patterns
    }

    /**
     * Set total minutes for a specific tag.
     */
    fun setTotalMinutesForTag(tagId: Long, minutes: Int) {
        totalMinutesForTags[tagId] = minutes
    }

    /**
     * Set daily trends for a specific tag.
     */
    fun setDailyTrendsForTag(tagId: Long, trends: List<DailyTrend>) {
        dailyTrendsForTags.getOrPut(tagId) { MutableStateFlow(emptyList()) }.value = trends
    }

    /**
     * Clear all data.
     */
    fun clear() {
        _statsByActivityType.value = emptyList()
        _statsByTag.value = emptyList()
        _dailyTrends.value = emptyList()
        _hourlyDistribution.value = emptyList()
        totalStats = StatisticsSummary(totalMinutes = 0, entryCount = 0)
        totalMinutesForTags.clear()
        dailyTrendsForTags.clear()
    }

    override fun getStatsByActivityType(startTime: Instant, endTime: Instant): Flow<List<CategoryStatistics>> {
        return _statsByActivityType
    }

    override fun getStatsByTag(startTime: Instant, endTime: Instant): Flow<List<CategoryStatistics>> {
        return _statsByTag
    }

    override fun getDailyTrends(startTime: Instant, endTime: Instant): Flow<List<DailyTrend>> {
        return _dailyTrends
    }

    override fun getHourlyDistribution(startTime: Instant, endTime: Instant): Flow<List<HourlyPattern>> {
        return _hourlyDistribution
    }

    override suspend fun getTotalStats(startTime: Instant, endTime: Instant): StatisticsSummary {
        if (shouldFail) throw failureException
        return totalStats
    }

    override suspend fun getTotalMinutesForTag(tagId: Long, startTime: Instant, endTime: Instant): Int {
        if (shouldFail) throw failureException
        return totalMinutesForTags[tagId] ?: 0
    }

    override fun getDailyTrendsForTag(tagId: Long, startTime: Instant, endTime: Instant): Flow<List<DailyTrend>> {
        return dailyTrendsForTags.getOrPut(tagId) { MutableStateFlow(emptyList()) }
    }
}
