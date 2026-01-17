package com.maplume.blockwise.feature.timeentry.presentation.timeentry

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.maplume.blockwise.core.testing.TestDataFactory
import com.maplume.blockwise.core.testing.TestDispatcherExtension
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype.GetActivityTypesUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.tag.GetTagsUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.CreateTimeEntryUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.GetTimeEntryByIdUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.UpdateTimeEntryUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
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
 * Unit tests for TimeEntryEditViewModel.
 * Tests state management and user interactions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("TimeEntryEditViewModel")
class TimeEntryEditViewModelTest {

    @JvmField
    @RegisterExtension
    val testDispatcherExtension = TestDispatcherExtension()

    private lateinit var getTimeEntryById: GetTimeEntryByIdUseCase
    private lateinit var createTimeEntry: CreateTimeEntryUseCase
    private lateinit var updateTimeEntry: UpdateTimeEntryUseCase
    private lateinit var getActivityTypes: GetActivityTypesUseCase
    private lateinit var getTags: GetTagsUseCase

    private val testActivityTypes = TestDataFactory.createDefaultActivityTypes().filter { !it.isArchived }
    private val testTags = TestDataFactory.createDefaultTags().filter { !it.isArchived }

    @BeforeEach
    fun setup() {
        getTimeEntryById = mockk()
        createTimeEntry = mockk()
        updateTimeEntry = mockk()
        getActivityTypes = mockk()
        getTags = mockk()

        // Default mock behavior
        every { getActivityTypes(includeArchived = false) } returns flowOf(testActivityTypes)
        every { getTags(includeArchived = false) } returns flowOf(testTags)
    }

    private fun createViewModel(entryId: Long? = null): TimeEntryEditViewModel {
        val savedStateHandle = SavedStateHandle().apply {
            if (entryId != null) {
                set("entryId", entryId)
            }
        }
        return TimeEntryEditViewModel(
            savedStateHandle = savedStateHandle,
            getTimeEntryById = getTimeEntryById,
            createTimeEntry = createTimeEntry,
            updateTimeEntry = updateTimeEntry,
            getActivityTypes = getActivityTypes,
            getTags = getTags
        )
    }

    @Nested
    @DisplayName("Initialization - Create mode")
    inner class CreateModeInitialization {

        @Test
        @DisplayName("loads activity types and tags")
        fun `loads activity types and tags`() = runTest {
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertEquals(testActivityTypes.size, state.activityTypes.size)
            assertEquals(testTags.size, state.availableTags.size)
            assertFalse(state.isLoading)
        }

        @Test
        @DisplayName("sets default date and time to now")
        fun `sets default date and time to now`() = runTest {
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertNotNull(state.selectedDate)
            assertNotNull(state.startTime)
            assertNotNull(state.endTime)
        }

        @Test
        @DisplayName("is in create mode when no entryId")
        fun `is in create mode when no entryId`() = runTest {
            // When
            val viewModel = createViewModel()

            // Then
            assertFalse(viewModel.isEditMode)
        }
    }

    @Nested
    @DisplayName("Initialization - Edit mode")
    inner class EditModeInitialization {

        @Test
        @DisplayName("loads existing entry when entryId provided")
        fun `loads existing entry when entryId provided`() = runTest {
            // Given
            val existingEntry = TestDataFactory.createTimeEntry(id = 1)
            coEvery { getTimeEntryById(1L) } returns existingEntry

            // When
            val viewModel = createViewModel(entryId = 1L)
            advanceUntilIdle()

            // Then
            assertTrue(viewModel.isEditMode)
            val state = viewModel.uiState.value
            assertEquals(existingEntry.activity.id, state.selectedActivityId)
        }

        @Test
        @DisplayName("navigates back when entry not found")
        fun `navigates back when entry not found`() = runTest {
            // Given
            coEvery { getTimeEntryById(999L) } returns null

            // When
            val viewModel = createViewModel(entryId = 999L)

            // Then
            viewModel.events.test {
                advanceUntilIdle()
                val event1 = awaitItem()
                assertTrue(event1 is TimeEntryEditEvent.Error)

                val event2 = awaitItem()
                assertEquals(TimeEntryEditEvent.NavigateBack, event2)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("User interactions")
    inner class UserInteractions {

        @Test
        @DisplayName("select activity type updates state")
        fun `select activity type updates state`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            val activityType = testActivityTypes.first()

            // When
            viewModel.onActivityTypeSelect(activityType)

            // Then
            val state = viewModel.uiState.value
            assertEquals(activityType.id, state.selectedActivityId)
            assertNull(state.activityTypeError)
        }

        @Test
        @DisplayName("change date updates state")
        fun `change date updates state`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            val newDate = LocalDate(2024, 6, 15)

            // When
            viewModel.onDateChange(newDate)

            // Then
            val state = viewModel.uiState.value
            assertEquals(newDate, state.selectedDate)
            assertNull(state.timeError)
        }

        @Test
        @DisplayName("change start time updates state")
        fun `change start time updates state`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            val newTime = LocalTime(9, 30)

            // When
            viewModel.onStartTimeChange(newTime)

            // Then
            val state = viewModel.uiState.value
            assertEquals(newTime, state.startTime)
        }

        @Test
        @DisplayName("change end time updates state")
        fun `change end time updates state`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            val newTime = LocalTime(10, 30)

            // When
            viewModel.onEndTimeChange(newTime)

            // Then
            val state = viewModel.uiState.value
            assertEquals(newTime, state.endTime)
        }

        @Test
        @DisplayName("toggle tag adds when not selected")
        fun `toggle tag adds when not selected`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.onTagToggle(1L)

            // Then
            val state = viewModel.uiState.value
            assertTrue(1L in state.selectedTagIds)
        }

        @Test
        @DisplayName("toggle tag removes when already selected")
        fun `toggle tag removes when already selected`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.onTagToggle(1L) // Select first

            // When
            viewModel.onTagToggle(1L) // Toggle again

            // Then
            val state = viewModel.uiState.value
            assertFalse(1L in state.selectedTagIds)
        }

        @Test
        @DisplayName("change note updates state")
        fun `change note updates state`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.onNoteChange("Test note")

            // Then
            val state = viewModel.uiState.value
            assertEquals("Test note", state.note)
        }
    }

    @Nested
    @DisplayName("Save operation")
    inner class SaveOperation {

        @Test
        @DisplayName("save without activity type shows error")
        fun `save without activity type shows error`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When - save without selecting activity type
            viewModel.save()

            // Then
            val state = viewModel.uiState.value
            assertNotNull(state.activityTypeError)
            assertEquals("请选择活动类型", state.activityTypeError)
        }

        @Test
        @DisplayName("create success sends navigation event")
        fun `create success sends navigation event`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onActivityTypeSelect(testActivityTypes.first())
            viewModel.onDateChange(LocalDate(2024, 1, 15))
            viewModel.onStartTimeChange(LocalTime(9, 0))
            viewModel.onEndTimeChange(LocalTime(10, 0))

            coEvery { createTimeEntry(any()) } returns Result.success(1L)

            // When
            viewModel.events.test {
                viewModel.save()
                advanceUntilIdle()

                // Then
                assertEquals(TimeEntryEditEvent.SaveSuccess, awaitItem())
                assertEquals(TimeEntryEditEvent.NavigateBack, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        @DisplayName("create failure shows error")
        fun `create failure shows error`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onActivityTypeSelect(testActivityTypes.first())
            viewModel.onDateChange(LocalDate(2024, 1, 15))
            viewModel.onStartTimeChange(LocalTime(9, 0))
            viewModel.onEndTimeChange(LocalTime(10, 0))

            coEvery { createTimeEntry(any()) } returns Result.failure(Exception("创建失败"))

            // When
            viewModel.events.test {
                viewModel.save()
                advanceUntilIdle()

                // Then
                val event = awaitItem()
                assertTrue(event is TimeEntryEditEvent.Error)
                assertEquals("创建失败", (event as TimeEntryEditEvent.Error).message)

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("Duration calculation")
    inner class DurationCalculation {

        @Test
        @DisplayName("calculates duration correctly")
        fun `calculates duration correctly`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onStartTimeChange(LocalTime(9, 0))
            viewModel.onEndTimeChange(LocalTime(10, 30))

            // Then
            val state = viewModel.uiState.value
            assertEquals(90, state.durationMinutes) // 1.5 hours = 90 minutes
            assertEquals("1小时30分钟", state.formattedDuration)
        }

        @Test
        @DisplayName("calculates overnight duration correctly")
        fun `calculates overnight duration correctly`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onStartTimeChange(LocalTime(23, 0))
            viewModel.onEndTimeChange(LocalTime(1, 0)) // Next day

            // Then
            val state = viewModel.uiState.value
            assertEquals(120, state.durationMinutes) // 2 hours crossing midnight
        }

        @Test
        @DisplayName("null times returns null duration")
        fun `null times returns null duration`() = runTest {
            // Given
            val viewModel = createViewModel()
            // Don't set times

            // Then - initial state might have times from "now", so check directly
            val state = TimeEntryEditUiState()
            assertNull(state.durationMinutes)
            assertNull(state.formattedDuration)
        }
    }

    @Nested
    @DisplayName("Validation state")
    inner class ValidationState {

        @Test
        @DisplayName("isValid returns true when all required fields set")
        fun `isValid returns true when all required fields set`() = runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.onActivityTypeSelect(testActivityTypes.first())
            viewModel.onDateChange(LocalDate(2024, 1, 15))
            viewModel.onStartTimeChange(LocalTime(9, 0))
            viewModel.onEndTimeChange(LocalTime(10, 0))

            // Then
            assertTrue(viewModel.uiState.value.isValid)
        }

        @Test
        @DisplayName("canSave is false when saving")
        fun `canSave is false when saving`() {
            // Given
            val state = TimeEntryEditUiState(
                selectedActivityId = 1,
                selectedDate = LocalDate(2024, 1, 15),
                startTime = LocalTime(9, 0),
                endTime = LocalTime(10, 0),
                isSaving = true
            )

            // Then
            assertFalse(state.canSave)
        }
    }
}
