package com.maplume.blockwise.feature.timeentry.domain.usecase.timeline

import com.maplume.blockwise.core.testing.TestDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.Assertions.assertEquals

import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineUntrackedGapTest {

    private val tz = TimeZone.UTC

    private fun instant(
        date: LocalDate,
        time: LocalTime,
        second: Int = 0
    ): Instant = date.atTime(LocalTime(time.hour, time.minute, second)).toInstant(tz)

    @Test
    fun `gap threshold exactly 1 minute inserts gap`() = runTest {
        val date = LocalDate(2026, 1, 1)
        val entry1 = TestDataFactory.createTimeEntry(
            id = 1,
            startTime = instant(date, LocalTime(10, 0), second = 10),
            endTime = instant(date, LocalTime(10, 10), second = 20),
            durationMinutes = 10
        )
        val entry2 = TestDataFactory.createTimeEntry(
            id = 2,
            startTime = instant(date, LocalTime(10, 11), second = 30),
            endTime = instant(date, LocalTime(10, 20), second = 40),
            durationMinutes = 9
        )

        val items = buildTimelineItemsForDay(
            date = date,
            sortedEntriesByStart = listOf(entry1, entry2),
            timeZone = tz
        )

        val gap = items.filterIsInstance<TimelineItem.UntrackedGap>()
        assertEquals(3, gap.size)
        assertEquals(instant(date, LocalTime(0, 0)), gap[0].startTime)
        assertEquals(instant(date, LocalTime(10, 0), second = 10), gap[0].endTime)
        assertEquals(instant(date, LocalTime(10, 10), second = 20), gap[1].startTime)
        assertEquals(instant(date, LocalTime(10, 11), second = 30), gap[1].endTime)
        assertEquals(instant(date, LocalTime(10, 20), second = 40), gap[2].startTime)
        assertEquals(instant(LocalDate(2026, 1, 2), LocalTime(0, 0)), gap[2].endTime)
    }

    @Test
    fun `overlapping entries do not create gap`() = runTest {
        val date = LocalDate(2026, 1, 1)
        val entry1 = TestDataFactory.createTimeEntry(
            id = 1,
            startTime = instant(date, LocalTime(10, 0), second = 1),
            endTime = instant(date, LocalTime(10, 30), second = 59),
            durationMinutes = 30
        )
        val entry2 = TestDataFactory.createTimeEntry(
            id = 2,
            startTime = instant(date, LocalTime(10, 20), second = 0),
            endTime = instant(date, LocalTime(10, 40), second = 2),
            durationMinutes = 20
        )

        val items = buildTimelineItemsForDay(
            date = date,
            sortedEntriesByStart = listOf(entry1, entry2),
            timeZone = tz
        )

        val gaps = items.filterIsInstance<TimelineItem.UntrackedGap>()
        assertEquals(2, gaps.size)
        assertEquals(instant(date, LocalTime(0, 0)), gaps[0].startTime)
        assertEquals(instant(date, LocalTime(10, 0), second = 1), gaps[0].endTime)
        assertEquals(instant(date, LocalTime(10, 40), second = 2), gaps[1].startTime)
        assertEquals(instant(LocalDate(2026, 1, 2), LocalTime(0, 0)), gaps[1].endTime)
    }

    @Test
    fun `start-of-day and end-of-day gaps are inserted`() = runTest {
        val date = LocalDate(2026, 1, 1)
        val entry = TestDataFactory.createTimeEntry(
            id = 1,
            startTime = instant(date, LocalTime(9, 0), second = 15),
            endTime = instant(date, LocalTime(18, 30), second = 45),
            durationMinutes = 570
        )

        val items = buildTimelineItemsForDay(
            date = date,
            sortedEntriesByStart = listOf(entry),
            timeZone = tz
        )

        val gaps = items.filterIsInstance<TimelineItem.UntrackedGap>()
        assertEquals(2, gaps.size)
        assertEquals(instant(date, LocalTime(0, 0)), gaps[0].startTime)
        assertEquals(instant(date, LocalTime(9, 0), second = 15), gaps[0].endTime)
        assertEquals(instant(date, LocalTime(18, 30), second = 45), gaps[1].startTime)
        assertEquals(instant(LocalDate(2026, 1, 2), LocalTime(0, 0)), gaps[1].endTime)
    }

    @Test
    fun `cross-midnight entry does not create false day gaps`() = runTest {
        val date = LocalDate(2026, 1, 1)
        val entry = TestDataFactory.createTimeEntry(
            id = 1,
            startTime = instant(date, LocalTime(23, 0), second = 10),
            endTime = instant(LocalDate(2026, 1, 2), LocalTime(1, 0), second = 20),
            durationMinutes = 120
        )

        val items = buildTimelineItemsForDay(
            date = date,
            sortedEntriesByStart = listOf(entry),
            timeZone = tz
        )

        val gaps = items.filterIsInstance<TimelineItem.UntrackedGap>()
        assertEquals(1, gaps.size)
        assertEquals(instant(date, LocalTime(0, 0)), gaps[0].startTime)
        assertEquals(instant(date, LocalTime(23, 0), second = 10), gaps[0].endTime)

        val entries = items.filterIsInstance<TimelineItem.Entry>()
        assertEquals(1, entries.size)
        assertEquals(entry.id, entries[0].slice.entry.id)
        assertEquals(date, entries[0].slice.date)
        assertEquals(instant(date, LocalTime(23, 0), second = 10), entries[0].slice.sliceStart)
        assertEquals(instant(LocalDate(2026, 1, 2), LocalTime(0, 0)), entries[0].slice.sliceEnd)
    }

    @Test
    fun `GetTimelineEntriesUseCase emits dayGroups with gap items`() = runTest {
        val repo = com.maplume.blockwise.core.testing.fake.FakeTimeEntryRepository().apply {
            inputToEntryMapper = { input, id ->
                TestDataFactory.createTimeEntry(
                    id = id,
                    startTime = input.startTime,
                    endTime = input.endTime,
                    durationMinutes = input.durationMinutes
                )
            }
        }

        val date = LocalDate(2026, 1, 1)
        repo.setEntries(
            listOf(
                TestDataFactory.createTimeEntry(
                    id = 1,
                    startTime = instant(date, LocalTime(10, 0), second = 5),
                    endTime = instant(date, LocalTime(10, 10), second = 10),
                    durationMinutes = 10
                ),
                TestDataFactory.createTimeEntry(
                    id = 2,
                    startTime = instant(date, LocalTime(10, 20), second = 15),
                    endTime = instant(date, LocalTime(10, 30), second = 20),
                    durationMinutes = 10
                )
            )
        )

        val useCase = GetTimelineEntriesUseCase(repository = repo)
        val groups = useCase(limit = 50, offset = 0).first()

        assertEquals(1, groups.size)
        val group = groups.first()
        assertEquals(date, group.date)
        assertEquals(2, group.entryCount)
        org.junit.jupiter.api.Assertions.assertTrue(group.items.any { it is TimelineItem.UntrackedGap })
    }
}
