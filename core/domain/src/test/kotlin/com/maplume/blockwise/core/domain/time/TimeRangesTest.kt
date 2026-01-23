package com.maplume.blockwise.core.domain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TimeRangesTest {

    private val tz = TimeZone.UTC

    private fun instant(date: LocalDate, time: LocalTime, second: Int = 0): Instant {
        return LocalDateTime(date, LocalTime(time.hour, time.minute, second)).toInstant(tz)
    }

    @Test
    fun `dayWindow is half-open for a date`() {
        val date = LocalDate(2026, 1, 1)
        val window = dayWindow(date, tz)

        assertEquals(instant(date, LocalTime(0, 0)), window.startTime)
        assertEquals(instant(LocalDate(2026, 1, 2), LocalTime(0, 0)), window.endTime)
    }

    @Test
    fun `overlapMinutes splits cross-midnight across day windows`() {
        val d1 = LocalDate(2026, 1, 1)
        val d2 = LocalDate(2026, 1, 2)

        val entry = TimeRange(
            startTime = instant(d1, LocalTime(23, 30), second = 10),
            endTime = instant(d2, LocalTime(0, 30), second = 20)
        )

        val minutesD1 = overlapMinutes(entry, dayWindow(d1, tz), tz)
        val minutesD2 = overlapMinutes(entry, dayWindow(d2, tz), tz)

        assertEquals(30, minutesD1)
        assertEquals(30, minutesD2)
    }

    @Test
    fun `overlapMinutes truncates seconds without rounding`() {
        val date = LocalDate(2026, 1, 1)

        val entry = TimeRange(
            startTime = instant(date, LocalTime(9, 0), second = 59),
            endTime = instant(date, LocalTime(9, 1), second = 1)
        )

        val minutes = overlapMinutes(entry, dayWindow(date, tz), tz)
        assertEquals(1, minutes)
    }
}
