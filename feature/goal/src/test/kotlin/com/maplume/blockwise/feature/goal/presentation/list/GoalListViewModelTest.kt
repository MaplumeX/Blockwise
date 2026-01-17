package com.maplume.blockwise.feature.goal.presentation.list

import app.cash.turbine.test
import com.maplume.blockwise.core.testing.TestDataFactory
import com.maplume.blockwise.core.testing.TestDispatcherExtension
import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalProgress
import com.maplume.blockwise.feature.goal.domain.usecase.CalculateGoalProgressUseCase
import com.maplume.blockwise.feature.goal.domain.usecase.DeleteGoalUseCase
import com.maplume.blockwise.feature.goal.domain.usecase.GetGoalsUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
 * Unit tests for GoalListViewModel.
 * Tests goal list management, filtering, and delete operations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("GoalListViewModel")
class GoalListViewModelTest {

    @JvmField
    @RegisterExtension
    val testDispatcherExtension = TestDispatcherExtension()

    private lateinit var getGoals: GetGoalsUseCase
    private lateinit var calculateProgress: CalculateGoalProgressUseCase
    private lateinit var deleteGoalUseCase: DeleteGoalUseCase

    private val testGoals = listOf(
        TestDataFactory.createGoal(id = 1, targetMinutes = 120),
        TestDataFactory.createGoal(id = 2, targetMinutes = 60),
        TestDataFactory.createGoal(id = 3, targetMinutes = 180)
    )

    private val testProgressList = testGoals.map { goal ->
        GoalProgress(
            goal = goal,
            currentMinutes = 60,
            targetMinutes = goal.targetMinutes
        )
    }

    @BeforeEach
    fun setup() {
        getGoals = mockk()
        calculateProgress = mockk()
        deleteGoalUseCase = mockk()
    }

    private fun createViewModel(): GoalListViewModel {
        // Default mocks
        every { getGoals.getActiveGoals() } returns flowOf(testGoals)
        every { getGoals.getAllGoals() } returns flowOf(testGoals)
        coEvery { calculateProgress.calculateAll(any()) } returns testProgressList

        return GoalListViewModel(
            getGoals = getGoals,
            calculateProgress = calculateProgress,
            deleteGoalUseCase = deleteGoalUseCase
        )
    }

    @Nested
    @DisplayName("Loading goals")
    inner class LoadingGoals {

        @Test
        @DisplayName("loads active goals on init")
        fun `loads active goals on init`() = runTest {
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertFalse(state.isLoading)
            assertEquals(3, state.goalProgressList.size)
        }

        @Test
        @DisplayName("calculates progress for each goal")
        fun `calculates progress for each goal`() = runTest {
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            coVerify { calculateProgress.calculateAll(testGoals) }
            val state = viewModel.uiState.value
            state.goalProgressList.forEach { progress ->
                assertEquals(60, progress.currentMinutes)
            }
        }

        @Test
        @DisplayName("handles empty goals list")
        fun `handles empty goals list`() = runTest {
            // Given
            every { getGoals.getActiveGoals() } returns flowOf(emptyList())
            coEvery { calculateProgress.calculateAll(emptyList()) } returns emptyList()

            // When
            val viewModel = GoalListViewModel(getGoals, calculateProgress, deleteGoalUseCase)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertTrue(state.goalProgressList.isEmpty())
            assertFalse(state.isLoading)
        }
    }

    @Nested
    @DisplayName("Filter toggle")
    inner class FilterToggle {

        @Test
        @DisplayName("toggleShowArchived switches filter state")
        fun `toggleShowArchived switches filter state`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.showArchived)

            // When
            viewModel.toggleShowArchived()
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.uiState.value.showArchived)
        }

        @Test
        @DisplayName("toggling to show archived calls getAllGoals")
        fun `toggling to show archived calls getAllGoals`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.toggleShowArchived()
            advanceUntilIdle()

            // Then
            coVerify { getGoals.getAllGoals() }
        }
    }

    @Nested
    @DisplayName("Navigation events")
    inner class NavigationEvents {

        @Test
        @DisplayName("onAddClick emits NavigateToAdd event")
        fun `onAddClick emits NavigateToAdd event`() = runTest {
            // Given
            val viewModel = createViewModel()

            // When & Then
            viewModel.events.test {
                viewModel.onAddClick()
                assertEquals(GoalListEvent.NavigateToAdd, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        @DisplayName("onGoalClick emits NavigateToDetail event")
        fun `onGoalClick emits NavigateToDetail event`() = runTest {
            // Given
            val viewModel = createViewModel()
            val goal = testGoals.first()

            // When & Then
            viewModel.events.test {
                viewModel.onGoalClick(goal)
                val event = awaitItem()
                assertTrue(event is GoalListEvent.NavigateToDetail)
                assertEquals(goal.id, (event as GoalListEvent.NavigateToDetail).goalId)
                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        @DisplayName("onEditClick emits NavigateToEdit event")
        fun `onEditClick emits NavigateToEdit event`() = runTest {
            // Given
            val viewModel = createViewModel()
            val goal = testGoals.first()

            // When & Then
            viewModel.events.test {
                viewModel.onEditClick(goal)
                val event = awaitItem()
                assertTrue(event is GoalListEvent.NavigateToEdit)
                assertEquals(goal.id, (event as GoalListEvent.NavigateToEdit).goalId)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("Delete operations")
    inner class DeleteOperations {

        @Test
        @DisplayName("onDeleteRequest sets goalToDelete")
        fun `onDeleteRequest sets goalToDelete`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            val goal = testGoals.first()

            // When
            viewModel.onDeleteRequest(goal)

            // Then
            assertEquals(goal, viewModel.uiState.value.goalToDelete)
        }

        @Test
        @DisplayName("onDeleteCancel clears goalToDelete")
        fun `onDeleteCancel clears goalToDelete`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onDeleteRequest(testGoals.first())

            // When
            viewModel.onDeleteCancel()

            // Then
            assertNull(viewModel.uiState.value.goalToDelete)
        }

        @Test
        @DisplayName("onArchiveConfirm calls archive use case")
        fun `onArchiveConfirm calls archive use case`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            val goal = testGoals.first()
            viewModel.onDeleteRequest(goal)

            coEvery { deleteGoalUseCase.archive(goal.id) } returns Result.success(Unit)

            // When & Then
            viewModel.events.test {
                viewModel.onArchiveConfirm()
                advanceUntilIdle()

                assertEquals(GoalListEvent.ArchiveSuccess, awaitItem())
                coVerify { deleteGoalUseCase.archive(goal.id) }
                assertNull(viewModel.uiState.value.goalToDelete)

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        @DisplayName("onArchiveConfirm handles failure")
        fun `onArchiveConfirm handles failure`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            val goal = testGoals.first()
            viewModel.onDeleteRequest(goal)

            coEvery { deleteGoalUseCase.archive(goal.id) } returns Result.failure(Exception("归档失败"))

            // When & Then
            viewModel.events.test {
                viewModel.onArchiveConfirm()
                advanceUntilIdle()

                val event = awaitItem()
                assertTrue(event is GoalListEvent.Error)
                assertEquals("归档失败", (event as GoalListEvent.Error).message)

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        @DisplayName("onDeleteConfirm calls delete use case")
        fun `onDeleteConfirm calls delete use case`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            val goal = testGoals.first()
            viewModel.onDeleteRequest(goal)

            coEvery { deleteGoalUseCase.delete(goal.id) } returns Result.success(Unit)

            // When & Then
            viewModel.events.test {
                viewModel.onDeleteConfirm()
                advanceUntilIdle()

                assertEquals(GoalListEvent.DeleteSuccess, awaitItem())
                coVerify { deleteGoalUseCase.delete(goal.id) }

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("Refresh")
    inner class Refresh {

        @Test
        @DisplayName("refresh reloads goals")
        fun `refresh reloads goals`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.refresh()
            advanceUntilIdle()

            // Then - getActiveGoals should be called multiple times (init + refresh)
            coVerify(atLeast = 2) { getGoals.getActiveGoals() }
        }
    }

    @Nested
    @DisplayName("Error handling")
    inner class ErrorHandling {

        @Test
        @DisplayName("calculation error updates state")
        fun `calculation error updates state`() = runTest {
            // Given
            every { getGoals.getActiveGoals() } returns flowOf(testGoals)
            coEvery { calculateProgress.calculateAll(any()) } throws RuntimeException("计算失败")

            // When
            val viewModel = GoalListViewModel(getGoals, calculateProgress, deleteGoalUseCase)
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertNotNull(state.error)
            assertFalse(state.isLoading)
        }

        @Test
        @DisplayName("clearError removes error state")
        fun `clearError removes error state`() = runTest {
            // Given
            every { getGoals.getActiveGoals() } returns flowOf(testGoals)
            coEvery { calculateProgress.calculateAll(any()) } throws RuntimeException("计算失败")

            val viewModel = GoalListViewModel(getGoals, calculateProgress, deleteGoalUseCase)
            advanceUntilIdle()

            assertNotNull(viewModel.uiState.value.error)

            // When
            viewModel.clearError()

            // Then
            assertNull(viewModel.uiState.value.error)
        }
    }
}
