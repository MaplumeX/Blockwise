package com.maplume.blockwise.feature.statistics.presentation

import app.cash.turbine.test
import com.maplume.blockwise.core.testing.TestDataFactory
import com.maplume.blockwise.core.testing.TestDispatcherExtension
import com.maplume.blockwise.core.domain.model.PeriodStatistics
import com.maplume.blockwise.core.domain.model.StatisticsSummary
import com.maplume.blockwise.feature.statistics.domain.model.StatisticsPeriod
import com.maplume.blockwise.feature.statistics.domain.usecase.GetPeriodStatisticsUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

/**
 * Unit tests for StatisticsViewModel.
 * Tests period selection, navigation, and data loading.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("StatisticsViewModel")
class StatisticsViewModelTest {

    @JvmField
    @RegisterExtension
    val testDispatcherExtension = TestDispatcherExtension()

    private lateinit var getPeriodStatistics: GetPeriodStatisticsUseCase

    private val testStatistics = PeriodStatistics(
        summary = TestDataFactory.createStatisticsSummary(totalMinutes = 480, entryCount = 8),
        byActivity = TestDataFactory.createCategoryStatisticsList(),
        byTag = emptyList(),
        dailyTrends = TestDataFactory.createDailyTrendList(7),
        hourlyPattern = TestDataFactory.createHourlyPatternList()
    )

    @BeforeEach
    fun setup() {
        getPeriodStatistics = mockk()
    }

    private fun createViewModel(): StatisticsViewModel {
        // Default mock - return test statistics for any period
        every { getPeriodStatistics(any()) } returns flowOf(testStatistics)

        return StatisticsViewModel(getPeriodStatistics)
    }

    @Nested
    @DisplayName("Initialization")
    inner class Initialization {

        @Test
        @DisplayName("starts with week period type")
        fun `starts with week period type`() = runTest {
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(PeriodType.WEEK, state.periodType)
        }

        @Test
        @DisplayName("loads statistics on init")
        fun `loads statistics on init`() = runTest {
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertNotNull(state.statistics)
            assertEquals(480, state.statistics?.summary?.totalMinutes)
            assertFalse(state.isLoading)
        }

        @Test
        @DisplayName("current period is marked correctly")
        fun `current period is marked correctly`() = runTest {
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value.isCurrentPeriod)
        }
    }

    @Nested
    @DisplayName("Period type selection")
    inner class PeriodTypeSelection {

        @Test
        @DisplayName("switching to day updates period type and reloads")
        fun `switching to day updates period type and reloads`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.onEvent(StatisticsEvent.SelectPeriodType(PeriodType.DAY))
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(PeriodType.DAY, state.periodType)
            assertTrue(state.currentPeriod is StatisticsPeriod.Day)
        }

        @Test
        @DisplayName("switching to month updates period type")
        fun `switching to month updates period type`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.onEvent(StatisticsEvent.SelectPeriodType(PeriodType.MONTH))
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(PeriodType.MONTH, state.periodType)
            assertTrue(state.currentPeriod is StatisticsPeriod.Month)
        }

        @Test
        @DisplayName("switching to year updates period type")
        fun `switching to year updates period type`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.onEvent(StatisticsEvent.SelectPeriodType(PeriodType.YEAR))
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(PeriodType.YEAR, state.periodType)
            assertTrue(state.currentPeriod is StatisticsPeriod.Year)
        }

        @Test
        @DisplayName("selecting same period type does nothing")
        fun `selecting same period type does nothing`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()
            val initialPeriod = viewModel.uiState.value.currentPeriod

            // When
            viewModel.onEvent(StatisticsEvent.SelectPeriodType(PeriodType.WEEK))
            advanceUntilIdle()

            // Then - period should remain the same
            assertEquals(initialPeriod, viewModel.uiState.value.currentPeriod)
        }
    }

    @Nested
    @DisplayName("Period navigation")
    inner class PeriodNavigation {

        @Test
        @DisplayName("navigate to previous updates period")
        fun `navigate to previous updates period`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()
            val initialPeriod = viewModel.uiState.value.currentPeriod

            // When
            viewModel.onEvent(StatisticsEvent.NavigateToPrevious)
            advanceUntilIdle()

            // Then
            val newPeriod = viewModel.uiState.value.currentPeriod
            assertTrue(newPeriod.startTime < initialPeriod.startTime)
        }

        @Test
        @DisplayName("navigate to next is blocked when at current period")
        fun `navigate to next is blocked when at current period`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isCurrentPeriod)
            val initialPeriod = viewModel.uiState.value.currentPeriod

            // When
            viewModel.onEvent(StatisticsEvent.NavigateToNext)
            advanceUntilIdle()

            // Then - period should not change
            assertEquals(initialPeriod, viewModel.uiState.value.currentPeriod)
        }

        @Test
        @DisplayName("navigate to next works when not at current period")
        fun `navigate to next works when not at current period`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            // First navigate to previous
            viewModel.onEvent(StatisticsEvent.NavigateToPrevious)
            advanceUntilIdle()
            val previousPeriod = viewModel.uiState.value.currentPeriod

            // When
            viewModel.onEvent(StatisticsEvent.NavigateToNext)
            advanceUntilIdle()

            // Then
            val newPeriod = viewModel.uiState.value.currentPeriod
            assertTrue(newPeriod.startTime > previousPeriod.startTime)
        }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandling {

        @Test
        @DisplayName("error from use case updates state")
        fun `error from use case updates state`() = runTest {
            // Given
            every { getPeriodStatistics(any()) } returns flow {
                throw RuntimeException("Network error")
            }

            // When
            val viewModel = StatisticsViewModel(getPeriodStatistics)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertNotNull(state.error)
            assertTrue(state.error?.contains("error") == true || state.error?.contains("失败") == true)
            assertFalse(state.isLoading)
        }
    }

    @Nested
    @DisplayName("Refresh")
    inner class Refresh {

        @Test
        @DisplayName("refresh reloads statistics")
        fun `refresh reloads statistics`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.onEvent(StatisticsEvent.Refresh)
            advanceUntilIdle()

            // Then - verify use case was called multiple times (init + refresh)
            verify(atLeast = 2) { getPeriodStatistics(any()) }
        }
    }

    @Nested
    @DisplayName("Loading state")
    inner class LoadingState {

        @Test
        @DisplayName("shows loading while fetching data")
        fun `shows loading while fetching data`() = runTest {
            // Given - slow response
            every { getPeriodStatistics(any()) } returns flow {
                kotlinx.coroutines.delay(100)
                emit(testStatistics)
            }

            // When
            val viewModel = StatisticsViewModel(getPeriodStatistics)

            // Then - should be loading initially
            viewModel.uiState.test {
                val initial = awaitItem()
                assertTrue(initial.isLoading)

                // Wait for data to load
                val loaded = awaitItem()
                assertFalse(loaded.isLoading)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
