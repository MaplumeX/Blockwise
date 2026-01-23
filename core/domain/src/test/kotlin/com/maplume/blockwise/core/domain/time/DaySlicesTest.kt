package com.maplume.blockwise.core.domain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DaySlicesTest {

    private val tz = TimeZone.UTC

    private fun instant(date: LocalDate, time: LocalTime, second: Int = 0): Instant {
        return LocalDateTime(date, LocalTime(time.hour, time.minute, second)).toInstant(tz)
    }

    @Test
    fun `daySliceForEntry returns null when no overlap`() {
        val date = LocalDate(2026, 1, 1)
        val entry = TimeRange(
            startTime = instant(LocalDate(2025, 12, 31), LocalTime(22, 0)),
            endTime = instant(LocalDate(2025, 12, 31), LocalTime(23, 0))
        )

        assertNull(daySliceForEntry(entry, date, tz))
    }

    @Test
    fun `daySliceForEntry clips cross-day entry to day window`() {
        val d1 = LocalDate(2026, 1, 1)
        val d2 = LocalDate(2026, 1, 2)

        val entry = TimeRange(
            startTime = instant(d1, LocalTime(23, 0), second = 10),
            endTime = instant(d2, LocalTime(1, 0), second = 20)
        )

        val sliceD1 = daySliceForEntry(entry, d1, tz)
        assertNotNull(sliceD1)
        assertEquals(instant(d1, LocalTime(23, 0), second = 10), sliceD1!!.sliceStart)
        assertEquals(instant(d2, LocalTime(0, 0)), sliceD1.sliceEnd)

        val sliceD2 = daySliceForEntry(entry, d2, tz)
        assertNotNull(sliceD2)
        assertEquals(instant(d2, LocalTime(0, 0)), sliceD2!!.sliceStart)
        assertEquals(instant(d2, LocalTime(1, 0), second = 20), sliceD2.sliceEnd)
    }
}
