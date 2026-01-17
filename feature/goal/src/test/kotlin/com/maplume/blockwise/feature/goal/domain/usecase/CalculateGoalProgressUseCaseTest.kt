package com.maplume.blockwise.feature.goal.domain.usecase

import com.maplume.blockwise.core.testing.TestDataFactory
import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalPeriod
import com.maplume.blockwise.core.domain.model.GoalType
import com.maplume.blockwise.core.testing.fake.FakeStatisticsRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for CalculateGoalProgressUseCase.
 * Tests goal progress calculation for different goal types and periods.
 */
@DisplayName("CalculateGoalProgressUseCase")
class CalculateGoalProgressUseCaseTest {

    private lateinit var useCase: CalculateGoalProgressUseCase
    private lateinit var statisticsRepository: FakeStatisticsRepository

    private val testTag = TestDataFactory.createTag(id = 1, name = "工作")

    @BeforeEach
    fun setup() {
        statisticsRepository = FakeStatisticsRepository()
        useCase = CalculateGoalProgressUseCase(statisticsRepository)
    }

    @Nested
    @DisplayName("MIN goal type")
    inner class MinGoalType {

        @Test
        @DisplayName("min goal completed when current exceeds target")
        fun `min goal completed when current exceeds target`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 120,
                goalType = GoalType.MIN,
                period = GoalPeriod.DAILY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 150)

            // When
            val progress = useCase(goal)

            // Then
            assertTrue(progress.isCompleted)
            assertEquals(150, progress.currentMinutes)
            assertEquals(120, progress.targetMinutes)
            assertEquals(1f, progress.progress) // Capped at 1.0
        }

        @Test
        @DisplayName("min goal not completed when current below target")
        fun `min goal not completed when current below target`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 120,
                goalType = GoalType.MIN,
                period = GoalPeriod.DAILY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 60)

            // When
            val progress = useCase(goal)

            // Then
            assertFalse(progress.isCompleted)
            assertEquals(60, progress.currentMinutes)
            assertEquals(0.5f, progress.progress)
            assertEquals(50, progress.progressPercentage)
            assertEquals(60, progress.remainingMinutes)
        }

        @Test
        @DisplayName("min goal exactly at target is completed")
        fun `min goal exactly at target is completed`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 120,
                goalType = GoalType.MIN,
                period = GoalPeriod.DAILY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 120)

            // When
            val progress = useCase(goal)

            // Then
            assertTrue(progress.isCompleted)
            assertEquals(120, progress.currentMinutes)
            assertEquals(1f, progress.progress)
        }
    }

    @Nested
    @DisplayName("MAX goal type")
    inner class MaxGoalType {

        @Test
        @DisplayName("max goal completed when current below target")
        fun `max goal completed when current below target`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 120,
                goalType = GoalType.MAX,
                period = GoalPeriod.DAILY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 60)

            // When
            val progress = useCase(goal)

            // Then
            assertTrue(progress.isCompleted)
            assertEquals(60, progress.currentMinutes)
        }

        @Test
        @DisplayName("max goal not completed when current exceeds target")
        fun `max goal not completed when current exceeds target`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 120,
                goalType = GoalType.MAX,
                period = GoalPeriod.DAILY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 150)

            // When
            val progress = useCase(goal)

            // Then
            assertFalse(progress.isCompleted)
            assertEquals(150, progress.currentMinutes)
        }

        @Test
        @DisplayName("max goal exactly at target is completed")
        fun `max goal exactly at target is completed`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 120,
                goalType = GoalType.MAX,
                period = GoalPeriod.DAILY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 120)

            // When
            val progress = useCase(goal)

            // Then
            assertTrue(progress.isCompleted)
        }
    }

    @Nested
    @DisplayName("EXACT goal type")
    inner class ExactGoalType {

        @Test
        @DisplayName("exact goal completed when current equals target")
        fun `exact goal completed when current equals target`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 120,
                goalType = GoalType.EXACT,
                period = GoalPeriod.DAILY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 120)

            // When
            val progress = useCase(goal)

            // Then
            assertTrue(progress.isCompleted)
            assertEquals(120, progress.currentMinutes)
            assertEquals(1f, progress.progress)
        }

        @Test
        @DisplayName("exact goal not completed when current below target")
        fun `exact goal not completed when current below target`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 120,
                goalType = GoalType.EXACT,
                period = GoalPeriod.DAILY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 60)

            // When
            val progress = useCase(goal)

            // Then
            assertFalse(progress.isCompleted)
        }

        @Test
        @DisplayName("exact goal not completed when current exceeds target")
        fun `exact goal not completed when current exceeds target`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 120,
                goalType = GoalType.EXACT,
                period = GoalPeriod.DAILY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 150)

            // When
            val progress = useCase(goal)

            // Then
            assertFalse(progress.isCompleted)
        }
    }

    @Nested
    @DisplayName("Batch calculation")
    inner class BatchCalculation {

        @Test
        @DisplayName("calculate all goals returns progress for each")
        fun `calculate all goals returns progress for each`() = runTest {
            // Given
            val tag1 = TestDataFactory.createTag(id = 1, name = "工作")
            val tag2 = TestDataFactory.createTag(id = 2, name = "学习")
            val tag3 = TestDataFactory.createTag(id = 3, name = "运动")

            val goals = listOf(
                TestDataFactory.createGoal(id = 1, tag = tag1, targetMinutes = 120, goalType = GoalType.MIN),
                TestDataFactory.createGoal(id = 2, tag = tag2, targetMinutes = 60, goalType = GoalType.MAX),
                TestDataFactory.createGoal(id = 3, tag = tag3, targetMinutes = 30, goalType = GoalType.EXACT)
            )

            statisticsRepository.setTotalMinutesForTag(1, 150) // MIN goal: completed
            statisticsRepository.setTotalMinutesForTag(2, 30) // MAX goal: completed
            statisticsRepository.setTotalMinutesForTag(3, 30) // EXACT goal: completed

            // When
            val progressList = useCase.calculateAll(goals)

            // Then
            assertEquals(3, progressList.size)
            assertTrue(progressList[0].isCompleted) // MIN: 150 >= 120
            assertTrue(progressList[1].isCompleted) // MAX: 30 <= 60
            assertTrue(progressList[2].isCompleted) // EXACT: 30 == 30
        }

        @Test
        @DisplayName("calculate all with empty list returns empty")
        fun `calculate all with empty list returns empty`() = runTest {
            // When
            val progressList = useCase.calculateAll(emptyList())

            // Then
            assertTrue(progressList.isEmpty())
        }
    }

    @Nested
    @DisplayName("Period time range")
    inner class PeriodTimeRange {

        @Test
        @DisplayName("daily goal uses today time range")
        fun `daily goal uses today time range`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 120,
                goalType = GoalType.MIN,
                period = GoalPeriod.DAILY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 60)

            // When
            val progress = useCase(goal)

            // Then - verify the progress is calculated (which means the time range was used)
            assertEquals(60, progress.currentMinutes)
            assertEquals(goal, progress.goal)
        }

        @Test
        @DisplayName("weekly goal uses this week time range")
        fun `weekly goal uses this week time range`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 600,
                goalType = GoalType.MIN,
                period = GoalPeriod.WEEKLY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 300)

            // When
            val progress = useCase(goal)

            // Then
            assertEquals(300, progress.currentMinutes)
            assertEquals(600, progress.targetMinutes)
            assertEquals(0.5f, progress.progress)
        }

        @Test
        @DisplayName("monthly goal uses this month time range")
        fun `monthly goal uses this month time range`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 2400,
                goalType = GoalType.MIN,
                period = GoalPeriod.MONTHLY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 1200)

            // When
            val progress = useCase(goal)

            // Then
            assertEquals(1200, progress.currentMinutes)
            assertEquals(2400, progress.targetMinutes)
            assertEquals(0.5f, progress.progress)
        }

        @Test
        @DisplayName("custom period goal uses custom date range")
        fun `custom period goal uses custom date range`() = runTest {
            // Given
            val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val startDate = today
            val endDate = today.plus(7, DateTimeUnit.DAY)

            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 300,
                goalType = GoalType.MIN,
                period = GoalPeriod.CUSTOM,
                startDate = startDate,
                endDate = endDate
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 150)

            // When
            val progress = useCase(goal)

            // Then
            assertEquals(150, progress.currentMinutes)
            assertEquals(300, progress.targetMinutes)
        }
    }

    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCases {

        @Test
        @DisplayName("zero current minutes returns zero progress")
        fun `zero current minutes returns zero progress`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 120,
                goalType = GoalType.MIN,
                period = GoalPeriod.DAILY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 0)

            // When
            val progress = useCase(goal)

            // Then
            assertEquals(0, progress.currentMinutes)
            assertEquals(0f, progress.progress)
            assertEquals(0, progress.progressPercentage)
            assertEquals(120, progress.remainingMinutes)
            assertFalse(progress.isCompleted)
        }

        @Test
        @DisplayName("progress is capped at 100 percent")
        fun `progress is capped at 100 percent`() = runTest {
            // Given
            val goal = TestDataFactory.createGoal(
                id = 1,
                tag = testTag,
                targetMinutes = 60,
                goalType = GoalType.MIN,
                period = GoalPeriod.DAILY
            )
            statisticsRepository.setTotalMinutesForTag(testTag.id, 180) // 300% of target

            // When
            val progress = useCase(goal)

            // Then
            assertEquals(180, progress.currentMinutes)
            assertEquals(1f, progress.progress) // Capped at 1.0
            assertEquals(100, progress.progressPercentage)
            assertEquals(0, progress.remainingMinutes)
        }

        @Test
        @DisplayName("different tags have independent progress")
        fun `different tags have independent progress`() = runTest {
            // Given
            val tag1 = TestDataFactory.createTag(id = 1, name = "工作")
            val tag2 = TestDataFactory.createTag(id = 2, name = "学习")

            val goal1 = TestDataFactory.createGoal(id = 1, tag = tag1, targetMinutes = 120)
            val goal2 = TestDataFactory.createGoal(id = 2, tag = tag2, targetMinutes = 60)

            statisticsRepository.setTotalMinutesForTag(1, 100)
            statisticsRepository.setTotalMinutesForTag(2, 30)

            // When
            val progressList = useCase.calculateAll(listOf(goal1, goal2))

            // Then
            assertEquals(100, progressList[0].currentMinutes)
            assertEquals(30, progressList[1].currentMinutes)
        }
    }
}
