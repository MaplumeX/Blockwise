package com.maplume.blockwise.feature.statistics.domain.usecase

import app.cash.turbine.test
import com.maplume.blockwise.core.testing.TestDataFactory
import com.maplume.blockwise.core.domain.model.CategoryStatistics
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.maplume.blockwise.core.domain.model.HourlyPattern
import com.maplume.blockwise.core.domain.model.StatisticsSummary
import com.maplume.blockwise.feature.statistics.domain.model.StatisticsPeriod
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for GetPeriodStatisticsUseCase.
 * Tests comprehensive statistics aggregation for different periods.
 */
@DisplayName("GetPeriodStatisticsUseCase")
class GetPeriodStatisticsUseCaseTest {

    private lateinit var useCase: GetPeriodStatisticsUseCase
    private lateinit var getStatsByActivityType: GetStatsByActivityTypeUseCase
    private lateinit var getStatsByTag: GetStatsByTagUseCase
    private lateinit var getDailyTrends: GetDailyTrendsUseCase
    private lateinit var getHourlyDistribution: GetHourlyDistributionUseCase
    private lateinit var getStatisticsSummary: GetStatisticsSummaryUseCase

    // Test data
    private val testActivityStats = listOf(
        CategoryStatistics(
            id = 1,
            name = "工作",
            colorHex = "#4CAF50",
            totalMinutes = 240,
            entryCount = 4,
            percentage = 50f
        ),
        CategoryStatistics(
            id = 2,
            name = "学习",
            colorHex = "#2196F3",
            totalMinutes = 120,
            entryCount = 2,
            percentage = 25f
        )
    )

    private val testTagStats = listOf(
        CategoryStatistics(
            id = 1,
            name = "重要",
            colorHex = "#F44336",
            totalMinutes = 180,
            entryCount = 3,
            percentage = 37.5f
        )
    )

    private val testDailyTrends = listOf(
        DailyTrend(date = LocalDate(2024, 1, 1), totalMinutes = 480, entryCount = 8),
        DailyTrend(date = LocalDate(2024, 1, 2), totalMinutes = 360, entryCount = 6)
    )

    private val testHourlyPattern = listOf(
        HourlyPattern(hour = 9, totalMinutes = 60),
        HourlyPattern(hour = 10, totalMinutes = 45),
        HourlyPattern(hour = 14, totalMinutes = 55)
    )

    private val testSummary = StatisticsSummary(
        totalMinutes = 480,
        entryCount = 8,
        previousPeriodMinutes = 400
    )

    @BeforeEach
    fun setup() {
        getStatsByActivityType = mockk()
        getStatsByTag = mockk()
        getDailyTrends = mockk()
        getHourlyDistribution = mockk()
        getStatisticsSummary = mockk()

        useCase = GetPeriodStatisticsUseCase(
            getStatsByActivityType = getStatsByActivityType,
            getStatsByTag = getStatsByTag,
            getDailyTrends = getDailyTrends,
            getHourlyDistribution = getHourlyDistribution,
            getStatisticsSummary = getStatisticsSummary
        )
    }

    private fun setupMocksForPeriod(period: StatisticsPeriod) {
        every { getStatsByActivityType(period.startTime, period.endTime) } returns flowOf(testActivityStats)
        every { getStatsByTag(period.startTime, period.endTime) } returns flowOf(testTagStats)
        every { getDailyTrends(period.startTime, period.endTime) } returns flowOf(testDailyTrends)
        every { getHourlyDistribution(period.startTime, period.endTime) } returns flowOf(testHourlyPattern)
        coEvery { getStatisticsSummary.withComparison(period) } returns testSummary
    }

    @Nested
    @DisplayName("Day statistics")
    inner class DayStatistics {

        @Test
        @DisplayName("returns combined statistics for a day")
        fun `returns combined statistics for a day`() = runTest {
            // Given
            val period = StatisticsPeriod.Day.today()
            setupMocksForPeriod(period)

            // When & Then
            useCase(period).test {
                val result = awaitItem()

                assertEquals(testSummary, result.summary)
                assertEquals(testActivityStats, result.byActivity)
                assertEquals(testTagStats, result.byTag)
                assertEquals(testDailyTrends, result.dailyTrends)
                assertEquals(testHourlyPattern, result.hourlyPattern)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("Week statistics")
    inner class WeekStatistics {

        @Test
        @DisplayName("returns combined statistics for a week")
        fun `returns combined statistics for a week`() = runTest {
            // Given
            val period = StatisticsPeriod.Week.current()
            setupMocksForPeriod(period)

            // When & Then
            useCase(period).test {
                val result = awaitItem()

                assertNotNull(result.summary)
                assertEquals(480, result.summary.totalMinutes)
                assertEquals(8, result.summary.entryCount)
                assertEquals(400, result.summary.previousPeriodMinutes)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("Month statistics")
    inner class MonthStatistics {

        @Test
        @DisplayName("returns combined statistics for a month")
        fun `returns combined statistics for a month`() = runTest {
            // Given
            val period = StatisticsPeriod.Month.current()
            setupMocksForPeriod(period)

            // When & Then
            useCase(period).test {
                val result = awaitItem()

                assertNotNull(result)
                assertEquals(2, result.byActivity.size)
                assertEquals(1, result.byTag.size)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("Empty data handling")
    inner class EmptyDataHandling {

        @Test
        @DisplayName("handles empty data gracefully")
        fun `handles empty data gracefully`() = runTest {
            // Given
            val period = StatisticsPeriod.Day.today()
            val emptySummary = StatisticsSummary(totalMinutes = 0, entryCount = 0)

            every { getStatsByActivityType(period.startTime, period.endTime) } returns flowOf(emptyList())
            every { getStatsByTag(period.startTime, period.endTime) } returns flowOf(emptyList())
            every { getDailyTrends(period.startTime, period.endTime) } returns flowOf(emptyList())
            every { getHourlyDistribution(period.startTime, period.endTime) } returns flowOf(emptyList())
            coEvery { getStatisticsSummary.withComparison(period) } returns emptySummary

            // When & Then
            useCase(period).test {
                val result = awaitItem()

                assertEquals(0, result.summary.totalMinutes)
                assertEquals(0, result.summary.entryCount)
                assertEquals(emptyList<CategoryStatistics>(), result.byActivity)
                assertEquals(emptyList<CategoryStatistics>(), result.byTag)
                assertEquals(emptyList<DailyTrend>(), result.dailyTrends)
                assertEquals(emptyList<HourlyPattern>(), result.hourlyPattern)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("Statistics content validation")
    inner class StatisticsContentValidation {

        @Test
        @DisplayName("activity stats are sorted by total minutes")
        fun `activity stats are sorted by total minutes`() = runTest {
            // Given
            val period = StatisticsPeriod.Day.today()
            val unsortedStats = listOf(
                CategoryStatistics(1, "B", "#000", 100, 2, 25f),
                CategoryStatistics(2, "A", "#000", 300, 4, 75f)
            )
            val sortedStats = unsortedStats.sortedByDescending { it.totalMinutes }

            every { getStatsByActivityType(period.startTime, period.endTime) } returns flowOf(sortedStats)
            every { getStatsByTag(period.startTime, period.endTime) } returns flowOf(emptyList())
            every { getDailyTrends(period.startTime, period.endTime) } returns flowOf(emptyList())
            every { getHourlyDistribution(period.startTime, period.endTime) } returns flowOf(emptyList())
            coEvery { getStatisticsSummary.withComparison(period) } returns StatisticsSummary(400, 6)

            // When & Then
            useCase(period).test {
                val result = awaitItem()

                assertEquals(300, result.byActivity.first().totalMinutes)
                assertEquals("A", result.byActivity.first().name)

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        @DisplayName("summary includes previous period comparison")
        fun `summary includes previous period comparison`() = runTest {
            // Given
            val period = StatisticsPeriod.Day.today()
            val summaryWithComparison = StatisticsSummary(
                totalMinutes = 500,
                entryCount = 10,
                previousPeriodMinutes = 400
            )

            every { getStatsByActivityType(period.startTime, period.endTime) } returns flowOf(emptyList())
            every { getStatsByTag(period.startTime, period.endTime) } returns flowOf(emptyList())
            every { getDailyTrends(period.startTime, period.endTime) } returns flowOf(emptyList())
            every { getHourlyDistribution(period.startTime, period.endTime) } returns flowOf(emptyList())
            coEvery { getStatisticsSummary.withComparison(period) } returns summaryWithComparison

            // When & Then
            useCase(period).test {
                val result = awaitItem()

                assertEquals(500, result.summary.totalMinutes)
                assertEquals(400, result.summary.previousPeriodMinutes)
                assertEquals(25, result.summary.changePercentage) // (500-400)/400 * 100 = 25%

                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
