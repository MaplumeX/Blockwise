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

    private fun instant(date: LocalDate, time: LocalTime): Instant = date.atTime(time).toInstant(tz)

    @Test
    fun `gap threshold exactly 1 minute inserts gap`() = runTest {
        val date = LocalDate(2026, 1, 1)
        val entry1 = TestDataFactory.createTimeEntry(
            id = 1,
            startTime = instant(date, LocalTime(10, 0)),
            endTime = instant(date, LocalTime(10, 10)),
            durationMinutes = 10
        )
        val entry2 = TestDataFactory.createTimeEntry(
            id = 2,
            startTime = instant(date, LocalTime(10, 11)),
            endTime = instant(date, LocalTime(10, 20)),
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
        assertEquals(instant(date, LocalTime(10, 0)), gap[0].endTime)
        assertEquals(instant(date, LocalTime(10, 10)), gap[1].startTime)
        assertEquals(instant(date, LocalTime(10, 11)), gap[1].endTime)
        assertEquals(instant(date, LocalTime(10, 20)), gap[2].startTime)
        assertEquals(instant(LocalDate(2026, 1, 2), LocalTime(0, 0)), gap[2].endTime)
    }

    @Test
    fun `overlapping entries do not create gap`() = runTest {
        val date = LocalDate(2026, 1, 1)
        val entry1 = TestDataFactory.createTimeEntry(
            id = 1,
            startTime = instant(date, LocalTime(10, 0)),
            endTime = instant(date, LocalTime(10, 30)),
            durationMinutes = 30
        )
        val entry2 = TestDataFactory.createTimeEntry(
            id = 2,
            startTime = instant(date, LocalTime(10, 20)),
            endTime = instant(date, LocalTime(10, 40)),
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
        assertEquals(instant(date, LocalTime(10, 0)), gaps[0].endTime)
        assertEquals(instant(date, LocalTime(10, 40)), gaps[1].startTime)
        assertEquals(instant(LocalDate(2026, 1, 2), LocalTime(0, 0)), gaps[1].endTime)
    }

    @Test
    fun `start-of-day and end-of-day gaps are inserted`() = runTest {
        val date = LocalDate(2026, 1, 1)
        val entry = TestDataFactory.createTimeEntry(
            id = 1,
            startTime = instant(date, LocalTime(9, 0)),
            endTime = instant(date, LocalTime(18, 30)),
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
        assertEquals(instant(date, LocalTime(9, 0)), gaps[0].endTime)
        assertEquals(instant(date, LocalTime(18, 30)), gaps[1].startTime)
        assertEquals(instant(LocalDate(2026, 1, 2), LocalTime(0, 0)), gaps[1].endTime)
    }

    @Test
    fun `cross-midnight entry does not create false day gaps`() = runTest {
        val date = LocalDate(2026, 1, 1)
        val entry = TestDataFactory.createTimeEntry(
            id = 1,
            startTime = instant(date, LocalTime(23, 0)),
            endTime = instant(LocalDate(2026, 1, 2), LocalTime(1, 0)),
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
        assertEquals(instant(date, LocalTime(23, 0)), gaps[0].endTime)
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
                    startTime = instant(date, LocalTime(10, 0)),
                    endTime = instant(date, LocalTime(10, 10)),
                    durationMinutes = 10
                ),
                TestDataFactory.createTimeEntry(
                    id = 2,
                    startTime = instant(date, LocalTime(10, 20)),
                    endTime = instant(date, LocalTime(10, 30)),
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
