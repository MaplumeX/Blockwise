package com.maplume.blockwise.core.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.maplume.blockwise.core.data.dao.ActivityTypeDao
import com.maplume.blockwise.core.data.dao.TimeEntryDao
import com.maplume.blockwise.core.data.database.AppDatabase
import com.maplume.blockwise.core.data.entity.ActivityTypeEntity
import com.maplume.blockwise.core.data.entity.TimeEntryEntity
import com.maplume.blockwise.core.data.testing.TestDatabaseModule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Integration tests for TimeEntry DAO operations.
 * Tests database operations using an in-memory Room database.
 */
@RunWith(AndroidJUnit4::class)
class TimeEntryRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var timeEntryDao: TimeEntryDao
    private lateinit var activityTypeDao: ActivityTypeDao

    // Base time for consistent test data
    private val now = Clock.System.now()
    private val baseActivityType = ActivityTypeEntity(
        id = 1,
        name = "Test Activity",
        colorHex = "#4CAF50",
        icon = "work",
        displayOrder = 0,
        createdAt = now,
        updatedAt = now
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = TestDatabaseModule.createInMemoryDatabase(context)
        timeEntryDao = database.timeEntryDao()
        activityTypeDao = database.activityTypeDao()

        // Insert base activity type for foreign key constraint
        kotlinx.coroutines.runBlocking {
            activityTypeDao.insert(baseActivityType)
        }
    }

    @After
    fun teardown() {
        database.close()
    }

    // ==================== CRUD Tests ====================

    @Test
    fun create_validEntry_returnsNewId() = runTest {
        // Given
        val entry = createTimeEntry(durationMinutes = 60)

        // When
        val id = timeEntryDao.insert(entry)

        // Then
        assertTrue(id > 0)
    }

    @Test
    fun getById_existingEntry_returnsEntry() = runTest {
        // Given
        val entry = createTimeEntry(durationMinutes = 60)
        val id = timeEntryDao.insert(entry)

        // When
        val result = timeEntryDao.getById(id)

        // Then
        assertNotNull(result)
        assertEquals(id, result?.id)
        assertEquals(60, result?.durationMinutes)
    }

    @Test
    fun getById_nonExistentEntry_returnsNull() = runTest {
        // When
        val result = timeEntryDao.getById(999)

        // Then
        assertNull(result)
    }

    @Test
    fun update_existingEntry_updatesSuccessfully() = runTest {
        // Given
        val entry = createTimeEntry(durationMinutes = 60)
        val id = timeEntryDao.insert(entry)
        val updatedEntry = entry.copy(id = id, durationMinutes = 90, note = "Updated")

        // When
        timeEntryDao.update(updatedEntry)
        val result = timeEntryDao.getById(id)

        // Then
        assertEquals(90, result?.durationMinutes)
        assertEquals("Updated", result?.note)
    }

    @Test
    fun delete_existingEntry_removesEntry() = runTest {
        // Given
        val entry = createTimeEntry(durationMinutes = 60)
        val id = timeEntryDao.insert(entry)

        // When
        timeEntryDao.deleteById(id)
        val result = timeEntryDao.getById(id)

        // Then
        assertNull(result)
    }

    // ==================== Query Tests ====================

    @Test
    fun getByTimeRange_returnsEntriesInRange() = runTest {
        // Given
        val startTime = now.minus(3.hours)
        val endTime = now

        // Create entries: 2 within range, 1 outside
        timeEntryDao.insert(createTimeEntry(startTime = now.minus(2.hours), durationMinutes = 30))
        timeEntryDao.insert(createTimeEntry(startTime = now.minus(1.hours), durationMinutes = 45))
        timeEntryDao.insert(createTimeEntry(startTime = now.minus(5.hours), durationMinutes = 60)) // Outside range

        // When
        timeEntryDao.getByTimeRange(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).test {
            val entries = awaitItem()

            // Then
            assertEquals(2, entries.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRecent_returnsLimitedEntries() = runTest {
        // Given
        repeat(5) { index ->
            timeEntryDao.insert(
                createTimeEntry(
                    startTime = now.minus((index + 1).hours),
                    durationMinutes = 30
                )
            )
        }

        // When
        timeEntryDao.getRecentWithDetails(limit = 3, offset = 0).test {
            val entries = awaitItem()

            // Then
            assertEquals(3, entries.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getRecent_withOffset_skipsEntries() = runTest {
        // Given
        repeat(5) { index ->
            timeEntryDao.insert(
                createTimeEntry(
                    startTime = now.minus((index + 1).hours),
                    durationMinutes = 30
                )
            )
        }

        // When
        timeEntryDao.getRecentWithDetails(limit = 2, offset = 2).test {
            val entries = awaitItem()

            // Then
            assertEquals(2, entries.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Validation Tests ====================

    @Test
    fun hasOverlapping_withOverlappingEntry_returnsEntry() = runTest {
        // Given
        val existingStart = now.minus(2.hours)
        val existingEnd = now.minus(1.hours)
        timeEntryDao.insert(createTimeEntry(startTime = existingStart, endTime = existingEnd, durationMinutes = 60))

        // New entry overlaps: starts during existing entry
        val newStart = now.minus(90.minutes)
        val newEnd = now.minus(30.minutes)

        // When
        val overlapping = timeEntryDao.findOverlapping(
            newStart.toEpochMilliseconds(),
            newEnd.toEpochMilliseconds()
        )

        // Then
        assertNotNull(overlapping)
    }

    @Test
    fun hasOverlapping_withNoOverlap_returnsNull() = runTest {
        // Given
        val existingStart = now.minus(2.hours)
        val existingEnd = now.minus(1.hours)
        timeEntryDao.insert(createTimeEntry(startTime = existingStart, endTime = existingEnd, durationMinutes = 60))

        // New entry does not overlap
        val newStart = now.minus(30.minutes)
        val newEnd = now

        // When
        val overlapping = timeEntryDao.findOverlapping(
            newStart.toEpochMilliseconds(),
            newEnd.toEpochMilliseconds()
        )

        // Then
        assertNull(overlapping)
    }

    @Test
    fun hasOverlapping_excludesSpecificId() = runTest {
        // Given
        val entryStart = now.minus(2.hours)
        val entryEnd = now.minus(1.hours)
        val id = timeEntryDao.insert(createTimeEntry(startTime = entryStart, endTime = entryEnd, durationMinutes = 60))

        // When checking for overlap with the same time range but excluding the entry itself
        val overlapping = timeEntryDao.findOverlapping(
            entryStart.toEpochMilliseconds(),
            entryEnd.toEpochMilliseconds(),
            excludeId = id
        )

        // Then
        assertNull(overlapping)
    }

    @Test
    fun getTotalDuration_calculatesCorrectSum() = runTest {
        // Given
        val startTime = now.minus(3.hours)
        val endTime = now

        timeEntryDao.insert(createTimeEntry(startTime = now.minus(2.hours), durationMinutes = 30))
        timeEntryDao.insert(createTimeEntry(startTime = now.minus(1.hours), durationMinutes = 45))

        // When
        val total = timeEntryDao.getTotalDurationByTimeRange(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        )

        // Then
        assertEquals(75, total) // 30 + 45
    }

    @Test
    fun getEntryCount_returnsCorrectCount() = runTest {
        // Given
        val startTime = now.minus(3.hours)
        val endTime = now

        timeEntryDao.insert(createTimeEntry(startTime = now.minus(2.hours), durationMinutes = 30))
        timeEntryDao.insert(createTimeEntry(startTime = now.minus(1.hours), durationMinutes = 45))

        // When
        val count = timeEntryDao.countByTimeRange(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        )

        // Then
        assertEquals(2, count)
    }

    @Test
    fun getLatest_returnsNewestEntry() = runTest {
        // Given
        val oldestId = timeEntryDao.insert(createTimeEntry(startTime = now.minus(2.hours), durationMinutes = 30))
        val newestId = timeEntryDao.insert(createTimeEntry(startTime = now.minus(30.minutes), durationMinutes = 20))

        // When
        val latest = timeEntryDao.getLatest()

        // Then
        assertNotNull(latest)
        assertEquals(newestId, latest?.entry?.id)
    }

    // ==================== Helper Functions ====================

    private fun createTimeEntry(
        startTime: kotlinx.datetime.Instant = now.minus(1.hours),
        endTime: kotlinx.datetime.Instant = startTime.plus(1.hours),
        durationMinutes: Int = 60,
        note: String? = null
    ) = TimeEntryEntity(
        activityId = 1,
        startTime = startTime,
        endTime = endTime,
        durationMinutes = durationMinutes,
        note = note,
        createdAt = now,
        updatedAt = now
    )
}
