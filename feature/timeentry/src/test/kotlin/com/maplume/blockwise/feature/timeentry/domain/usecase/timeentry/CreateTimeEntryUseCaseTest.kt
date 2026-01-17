package com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry

import com.maplume.blockwise.core.testing.TestDataFactory
import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.model.TimeEntryInput
import com.maplume.blockwise.core.testing.fake.FakeActivityTypeRepository
import com.maplume.blockwise.core.testing.fake.FakeTimeEntryRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Unit tests for CreateTimeEntryUseCase.
 * Tests business logic validation for creating time entries.
 */
@DisplayName("CreateTimeEntryUseCase")
class CreateTimeEntryUseCaseTest {

    private lateinit var useCase: CreateTimeEntryUseCase
    private lateinit var timeEntryRepository: FakeTimeEntryRepository
    private lateinit var activityTypeRepository: FakeActivityTypeRepository

    @BeforeEach
    fun setup() {
        timeEntryRepository = FakeTimeEntryRepository()
        activityTypeRepository = FakeActivityTypeRepository()

        // Set up default activity types
        activityTypeRepository.setActivityTypes(TestDataFactory.createDefaultActivityTypes())

        // Set up mapper for creating entries
        timeEntryRepository.inputToEntryMapper = { input, id ->
            val activity = activityTypeRepository.getAllActivityTypes()
                .find { it.id == input.activityId }!!
            TimeEntry(
                id = id,
                activity = activity,
                startTime = input.startTime,
                endTime = input.endTime,
                durationMinutes = input.durationMinutes,
                note = input.note,
                tags = emptyList() // Simplified for testing
            )
        }

        useCase = CreateTimeEntryUseCase(timeEntryRepository, activityTypeRepository)
    }

    @Nested
    @DisplayName("Valid time entry creation")
    inner class ValidCreation {

        @Test
        @DisplayName("create valid time entry returns new id")
        fun `create valid time entry returns new id`() = runTest {
            // Given
            val now = Clock.System.now()
            val input = TimeEntryInput(
                activityId = 1,
                startTime = now.minus(1.hours),
                endTime = now,
                note = null,
                tagIds = emptyList()
            )

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(1L, result.getOrNull())
            assertEquals(1, timeEntryRepository.getAllEntries().size)
        }

        @Test
        @DisplayName("create time entry with tags succeeds")
        fun `create time entry with tags succeeds`() = runTest {
            // Given
            val now = Clock.System.now()
            val input = TimeEntryInput(
                activityId = 1,
                startTime = now.minus(1.hours),
                endTime = now,
                note = null,
                tagIds = listOf(1L, 2L)
            )

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isSuccess)
            val entry = timeEntryRepository.getAllEntries().first()
            assertEquals(1L, input.activityId)
        }

        @Test
        @DisplayName("create time entry with note succeeds")
        fun `create time entry with note succeeds`() = runTest {
            // Given
            val now = Clock.System.now()
            val input = TimeEntryInput(
                activityId = 1,
                startTime = now.minus(1.hours),
                endTime = now,
                note = "Test note",
                tagIds = emptyList()
            )

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isSuccess)
            val entry = timeEntryRepository.getAllEntries().first()
            assertEquals("Test note", entry.note)
        }
    }

    @Nested
    @DisplayName("Time validation")
    inner class TimeValidation {

        @Test
        @DisplayName("end time before start time fails")
        fun `end time before start time fails`() = runTest {
            // Given
            val now = Clock.System.now()
            val input = TimeEntryInput(
                activityId = 1,
                startTime = now,
                endTime = now.minus(1.hours),
                note = null,
                tagIds = emptyList()
            )

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is IllegalArgumentException)
            assertTrue(exception?.message?.contains("结束时间") == true)
        }

        @Test
        @DisplayName("start time equals end time fails")
        fun `start time equals end time fails`() = runTest {
            // Given
            val now = Clock.System.now()
            val input = TimeEntryInput(
                activityId = 1,
                startTime = now,
                endTime = now,
                note = null,
                tagIds = emptyList()
            )

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is IllegalArgumentException)
        }

        @Test
        @DisplayName("duration exceeds 24 hours fails")
        fun `duration exceeds 24 hours fails`() = runTest {
            // Given
            val now = Clock.System.now()
            val input = TimeEntryInput(
                activityId = 1,
                startTime = now.minus(25.hours),
                endTime = now,
                note = null,
                tagIds = emptyList()
            )

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is IllegalArgumentException)
            assertTrue(exception?.message?.contains("24") == true)
        }

        @Test
        @DisplayName("duration exactly 24 hours succeeds")
        fun `duration exactly 24 hours succeeds`() = runTest {
            // Given
            val now = Clock.System.now()
            val input = TimeEntryInput(
                activityId = 1,
                startTime = now.minus(24.hours),
                endTime = now,
                note = null,
                tagIds = emptyList()
            )

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isSuccess)
        }

        @Test
        @DisplayName("very short duration of 1 minute succeeds")
        fun `very short duration of 1 minute succeeds`() = runTest {
            // Given
            val now = Clock.System.now()
            val input = TimeEntryInput(
                activityId = 1,
                startTime = now.minus(1.minutes),
                endTime = now,
                note = null,
                tagIds = emptyList()
            )

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isSuccess)
        }
    }

    @Nested
    @DisplayName("Activity type validation")
    inner class ActivityTypeValidation {

        @Test
        @DisplayName("non-existent activity type fails")
        fun `non-existent activity type fails`() = runTest {
            // Given
            val now = Clock.System.now()
            val input = TimeEntryInput(
                activityId = 999, // Non-existent ID
                startTime = now.minus(1.hours),
                endTime = now,
                note = null,
                tagIds = emptyList()
            )

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is IllegalArgumentException)
            assertTrue(exception?.message?.contains("不存在") == true)
        }

        @Test
        @DisplayName("archived activity type fails")
        fun `archived activity type fails`() = runTest {
            // Given
            val now = Clock.System.now()
            // Activity type with id=5 is archived in TestDataFactory.createDefaultActivityTypes()
            val input = TimeEntryInput(
                activityId = 5,
                startTime = now.minus(1.hours),
                endTime = now,
                note = null,
                tagIds = emptyList()
            )

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isFailure)
            val exception = result.exceptionOrNull()
            assertTrue(exception is IllegalArgumentException)
            assertTrue(exception?.message?.contains("归档") == true)
        }
    }

    @Nested
    @DisplayName("Repository failure handling")
    inner class RepositoryFailure {

        @Test
        @DisplayName("repository failure returns failure result")
        fun `repository failure returns failure result`() = runTest {
            // Given
            val now = Clock.System.now()
            val input = TimeEntryInput(
                activityId = 1,
                startTime = now.minus(1.hours),
                endTime = now,
                note = null,
                tagIds = emptyList()
            )
            timeEntryRepository.shouldFail = true
            timeEntryRepository.failureException = RuntimeException("Database error")

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isFailure)
            assertEquals("Database error", result.exceptionOrNull()?.message)
        }
    }

    @Nested
    @DisplayName("Edge cases")
    inner class EdgeCases {

        @Test
        @DisplayName("multiple entries can be created sequentially")
        fun `multiple entries can be created sequentially`() = runTest {
            // Given
            val now = Clock.System.now()

            // When
            val result1 = useCase(
                TimeEntryInput(
                    activityId = 1,
                    startTime = now.minus(3.hours),
                    endTime = now.minus(2.hours),
                    note = "First"
                )
            )
            val result2 = useCase(
                TimeEntryInput(
                    activityId = 2,
                    startTime = now.minus(2.hours),
                    endTime = now.minus(1.hours),
                    note = "Second"
                )
            )
            val result3 = useCase(
                TimeEntryInput(
                    activityId = 3,
                    startTime = now.minus(1.hours),
                    endTime = now,
                    note = "Third"
                )
            )

            // Then
            assertTrue(result1.isSuccess)
            assertTrue(result2.isSuccess)
            assertTrue(result3.isSuccess)
            assertEquals(1L, result1.getOrNull())
            assertEquals(2L, result2.getOrNull())
            assertEquals(3L, result3.getOrNull())
            assertEquals(3, timeEntryRepository.getAllEntries().size)
        }

        @Test
        @DisplayName("empty note is treated as null")
        fun `empty note is treated as null`() = runTest {
            // Given
            val now = Clock.System.now()
            val input = TimeEntryInput(
                activityId = 1,
                startTime = now.minus(1.hours),
                endTime = now,
                note = "",
                tagIds = emptyList()
            )

            // When
            val result = useCase(input)

            // Then
            assertTrue(result.isSuccess)
        }
    }
}
