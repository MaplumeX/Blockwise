package com.maplume.blockwise.feature.timeentry.domain.usecase.timer

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("buildTimerEntries")
class BuildTimerEntriesTest {

    @Test
    fun `end not after start is corrected to plus 1s`() {
        val start = Instant.parse("2026-01-01T10:00:00Z")
        val end = start

        val inputs = buildTimerEntries(
            activityId = 1L,
            startTime = start,
            endTime = end,
            tagIds = emptyList()
        )

        assertEquals(1, inputs.size)
        assertEquals(start, inputs.first().startTime)
        assertEquals(start.plus(1, DateTimeUnit.SECOND, TimeZone.UTC), inputs.first().endTime)
    }

    @Test
    fun `cross-midnight range is split at midnight`() {
        val tz = TimeZone.currentSystemDefault()
        val startDate = LocalDate(2026, 1, 1)
        val nextDate = LocalDate(2026, 1, 2)

        val start = LocalDateTime(startDate, LocalTime(23, 59, 59)).toInstant(tz)
        val end = LocalDateTime(nextDate, LocalTime(0, 0, 10)).toInstant(tz)
        val midnight = nextDate.atTime(LocalTime(0, 0)).toInstant(tz)

        val inputs = buildTimerEntries(
            activityId = 1L,
            startTime = start,
            endTime = end,
            tagIds = emptyList()
        )

        assertEquals(2, inputs.size)
        assertEquals(start, inputs[0].startTime)
        assertEquals(midnight, inputs[0].endTime)
        assertEquals(midnight, inputs[1].startTime)
        assertEquals(end, inputs[1].endTime)
    }
}
