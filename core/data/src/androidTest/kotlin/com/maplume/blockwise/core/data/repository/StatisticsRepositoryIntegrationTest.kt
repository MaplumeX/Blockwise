package com.maplume.blockwise.core.data.repository

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.maplume.blockwise.core.data.dao.ActivityTypeDao
import com.maplume.blockwise.core.data.dao.StatisticsDao
import com.maplume.blockwise.core.data.dao.TagDao
import com.maplume.blockwise.core.data.dao.TimeEntryDao
import com.maplume.blockwise.core.data.dao.TimeEntryTagDao
import com.maplume.blockwise.core.data.database.AppDatabase
import com.maplume.blockwise.core.data.entity.ActivityTypeEntity
import com.maplume.blockwise.core.data.entity.TagEntity
import com.maplume.blockwise.core.data.entity.TimeEntryEntity
import com.maplume.blockwise.core.data.entity.TimeEntryTagEntity
import com.maplume.blockwise.core.data.testing.TestDatabaseModule
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Integration tests for Statistics DAO operations.
 * Tests aggregation queries using an in-memory Room database.
 */
@RunWith(AndroidJUnit4::class)
class StatisticsRepositoryIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var statisticsDao: StatisticsDao
    private lateinit var timeEntryDao: TimeEntryDao
    private lateinit var activityTypeDao: ActivityTypeDao
    private lateinit var tagDao: TagDao
    private lateinit var timeEntryTagDao: TimeEntryTagDao

    private val now = Clock.System.now()

    // Test activity types
    private val workActivity = ActivityTypeEntity(
        id = 1,
        name = "工作",
        colorHex = "#4CAF50",
        icon = "work",
        displayOrder = 0,
        createdAt = now,
        updatedAt = now
    )

    private val studyActivity = ActivityTypeEntity(
        id = 2,
        name = "学习",
        colorHex = "#2196F3",
        icon = "school",
        displayOrder = 1,
        createdAt = now,
        updatedAt = now
    )

    // Test tags
    private val importantTag = TagEntity(
        id = 1,
        name = "重要",
        colorHex = "#F44336",
        createdAt = now,
        updatedAt = now
    )

    private val urgentTag = TagEntity(
        id = 2,
        name = "紧急",
        colorHex = "#FF9800",
        createdAt = now,
        updatedAt = now
    )

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = TestDatabaseModule.createInMemoryDatabase(context)
        statisticsDao = database.statisticsDao()
        timeEntryDao = database.timeEntryDao()
        activityTypeDao = database.activityTypeDao()
        tagDao = database.tagDao()
        timeEntryTagDao = database.timeEntryTagDao()

        // Insert base data
        kotlinx.coroutines.runBlocking {
            activityTypeDao.insert(workActivity)
            activityTypeDao.insert(studyActivity)
            tagDao.insert(importantTag)
            tagDao.insert(urgentTag)
        }
    }

    @After
    fun teardown() {
        database.close()
    }

    // ==================== Activity Statistics Tests ====================

    @Test
    fun getStatsByActivityType_groupsByActivity() = runTest {
        // Given
        val startTime = now.minus(4.hours)
        val endTime = now

        // Work entries: 60 + 30 = 90 minutes
        timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(3.hours), durationMinutes = 60))
        timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(1.hours), durationMinutes = 30))

        // Study entries: 45 minutes
        timeEntryDao.insert(createTimeEntry(activityId = 2, startTime = now.minus(2.hours), durationMinutes = 45))

        // When
        statisticsDao.getStatsByActivityType(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).test {
            val stats = awaitItem()

            // Then
            assertEquals(2, stats.size)

            val workStats = stats.find { it.activityId == 1L }
            assertEquals(90, workStats?.totalMinutes)
            assertEquals(2, workStats?.entryCount)

            val studyStats = stats.find { it.activityId == 2L }
            assertEquals(45, studyStats?.totalMinutes)
            assertEquals(1, studyStats?.entryCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getStatsByActivityType_emptyRange_returnsEmpty() = runTest {
        // Given - no entries in range
        val startTime = now.minus(1.hours)
        val endTime = now

        // When
        statisticsDao.getStatsByActivityType(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).test {
            val stats = awaitItem()

            // Then
            assertTrue(stats.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Tag Statistics Tests ====================

    @Test
    fun getStatsByTag_groupsByTag() = runTest {
        // Given
        val startTime = now.minus(4.hours)
        val endTime = now

        // Entry 1 with important tag: 60 minutes
        val entry1Id = timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(3.hours), durationMinutes = 60))
        timeEntryTagDao.insert(TimeEntryTagEntity(entryId = entry1Id, tagId = 1))

        // Entry 2 with important and urgent tags: 30 minutes
        val entry2Id = timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(2.hours), durationMinutes = 30))
        timeEntryTagDao.insert(TimeEntryTagEntity(entryId = entry2Id, tagId = 1))
        timeEntryTagDao.insert(TimeEntryTagEntity(entryId = entry2Id, tagId = 2))

        // Entry 3 with urgent tag: 45 minutes
        val entry3Id = timeEntryDao.insert(createTimeEntry(activityId = 2, startTime = now.minus(1.hours), durationMinutes = 45))
        timeEntryTagDao.insert(TimeEntryTagEntity(entryId = entry3Id, tagId = 2))

        // When
        statisticsDao.getStatsByTag(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).test {
            val stats = awaitItem()

            // Then
            assertEquals(2, stats.size)

            // Important tag: 60 + 30 = 90 minutes, 2 entries
            val importantStats = stats.find { it.tagId == 1L }
            assertEquals(90, importantStats?.totalMinutes)
            assertEquals(2, importantStats?.entryCount)

            // Urgent tag: 30 + 45 = 75 minutes, 2 entries
            val urgentStats = stats.find { it.tagId == 2L }
            assertEquals(75, urgentStats?.totalMinutes)
            assertEquals(2, urgentStats?.entryCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Total Statistics Tests ====================

    @Test
    fun getTotalStats_calculatesCorrectly() = runTest {
        // Given
        val startTime = now.minus(4.hours)
        val endTime = now

        timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(3.hours), durationMinutes = 60))
        timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(2.hours), durationMinutes = 30))
        timeEntryDao.insert(createTimeEntry(activityId = 2, startTime = now.minus(1.hours), durationMinutes = 45))

        // When
        val total = statisticsDao.getTotalStats(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        )

        // Then
        assertEquals(135, total.totalMinutes) // 60 + 30 + 45
        assertEquals(3, total.entryCount)
    }

    @Test
    fun getTotalStats_emptyRange_returnsZero() = runTest {
        // Given - no entries
        val startTime = now.minus(1.hours)
        val endTime = now

        // When
        val total = statisticsDao.getTotalStats(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        )

        // Then
        assertEquals(0, total.totalMinutes)
        assertEquals(0, total.entryCount)
    }

    // ==================== Tag Total Minutes Tests ====================

    @Test
    fun getTotalMinutesForTag_calculatesCorrectly() = runTest {
        // Given
        val startTime = now.minus(4.hours)
        val endTime = now

        // Entry 1 with important tag: 60 minutes
        val entry1Id = timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(3.hours), durationMinutes = 60))
        timeEntryTagDao.insert(TimeEntryTagEntity(entryId = entry1Id, tagId = 1))

        // Entry 2 with important tag: 30 minutes
        val entry2Id = timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(2.hours), durationMinutes = 30))
        timeEntryTagDao.insert(TimeEntryTagEntity(entryId = entry2Id, tagId = 1))

        // Entry 3 without important tag: 45 minutes
        timeEntryDao.insert(createTimeEntry(activityId = 2, startTime = now.minus(1.hours), durationMinutes = 45))

        // When
        val total = statisticsDao.getTotalMinutesForTag(
            tagId = 1,
            startTime = startTime.toEpochMilliseconds(),
            endTime = endTime.toEpochMilliseconds()
        )

        // Then
        assertEquals(90, total) // 60 + 30
    }

    @Test
    fun getTotalMinutesForTag_noEntriesWithTag_returnsZero() = runTest {
        // Given
        val startTime = now.minus(4.hours)
        val endTime = now

        // Entry without any tags
        timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(2.hours), durationMinutes = 60))

        // When
        val total = statisticsDao.getTotalMinutesForTag(
            tagId = 1,
            startTime = startTime.toEpochMilliseconds(),
            endTime = endTime.toEpochMilliseconds()
        )

        // Then
        assertEquals(0, total)
    }

    // ==================== Daily Trends Tests ====================

    @Test
    fun getDailyTrends_groupsByDay() = runTest {
        // Given
        val startTime = now.minus(48.hours)
        val endTime = now

        // Day 1 entries
        timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(30.hours), durationMinutes = 60))
        timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(28.hours), durationMinutes = 30))

        // Day 2 entries
        timeEntryDao.insert(createTimeEntry(activityId = 2, startTime = now.minus(6.hours), durationMinutes = 45))
        timeEntryDao.insert(createTimeEntry(activityId = 2, startTime = now.minus(4.hours), durationMinutes = 15))

        // When
        statisticsDao.getDailyStats(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).test {
            val trends = awaitItem()

            // Then
            assertEquals(2, trends.size)

            // Note: Exact day calculations depend on timezone
            // Just verify we have distinct days
            val distinctDays = trends.map { it.dateMillis }.distinct()
            assertEquals(2, distinctDays.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Hourly Distribution Tests ====================

    @Test
    fun getHourlyDistribution_groupsByHour() = runTest {
        // Given
        val startTime = now.minus(24.hours)
        val endTime = now

        // Entries at different hours
        timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(10.hours), durationMinutes = 30))
        timeEntryDao.insert(createTimeEntry(activityId = 1, startTime = now.minus(10.hours - 30.minutes), durationMinutes = 20))
        timeEntryDao.insert(createTimeEntry(activityId = 2, startTime = now.minus(5.hours), durationMinutes = 45))

        // When
        statisticsDao.getHourlyDistribution(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).test {
            val distribution = awaitItem()

            // Then
            assertTrue(distribution.isNotEmpty())
            // Verify at least two different hours
            val distinctHours = distribution.map { it.hour }.distinct()
            assertTrue(distinctHours.size >= 2)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ==================== Helper Functions ====================

    private fun createTimeEntry(
        activityId: Long = 1,
        startTime: kotlinx.datetime.Instant = now.minus(1.hours),
        durationMinutes: Int = 60,
        note: String? = null
    ) = TimeEntryEntity(
        activityId = activityId,
        startTime = startTime,
        endTime = startTime.plus(durationMinutes.minutes),
        durationMinutes = durationMinutes,
        note = note,
        createdAt = now,
        updatedAt = now
    )
}
