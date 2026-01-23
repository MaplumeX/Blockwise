package com.maplume.blockwise.feature.timeentry.presentation.timeline

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Timeline default prefill")
class TimelineDefaultPrefillTest {

    private val tz = TimeZone.UTC

    @Test
    fun `selected date is today uses today's date`() {
        val now = Instant.parse("2026-01-20T10:15:59Z")
        val selectedDate = LocalDate(2026, 1, 20)

        val prefill = defaultPrefillForSelectedTimelineDate(
            selectedDate = selectedDate,
            now = now,
            timeZone = tz
        )

        assertEquals(LocalDate(2026, 1, 20), prefill.startDate)
        assertEquals(LocalDate(2026, 1, 20), prefill.endDate)
        assertEquals(10, prefill.startTime.hour)
        assertEquals(15, prefill.startTime.minute)
        assertEquals(10, prefill.endTime.hour)
        assertEquals(15, prefill.endTime.minute)
    }

    @Test
    fun `selected date is before today uses selected date`() {
        val now = Instant.parse("2026-01-20T10:15:59Z")
        val selectedDate = LocalDate(2026, 1, 10)

        val prefill = defaultPrefillForSelectedTimelineDate(
            selectedDate = selectedDate,
            now = now,
            timeZone = tz
        )

        assertEquals(LocalDate(2026, 1, 10), prefill.startDate)
        assertEquals(LocalDate(2026, 1, 10), prefill.endDate)
        assertEquals(10, prefill.startTime.hour)
        assertEquals(15, prefill.startTime.minute)
    }

    @Test
    fun `prefill aligns to minute precision by flooring seconds`() {
        val now = Instant.parse("2026-01-20T10:15:59Z")
        val selectedDate = LocalDate(2026, 1, 20)

        val prefill = defaultPrefillForSelectedTimelineDate(
            selectedDate = selectedDate,
            now = now,
            timeZone = tz
        )

        assertEquals(10, prefill.startTime.hour)
        assertEquals(15, prefill.startTime.minute)
        assertEquals(prefill.startTime, prefill.endTime)
    }

    @Test
    fun `selected date is after today clamps to today`() {
        val now = Instant.parse("2026-01-20T10:15:59Z")
        val selectedDate = LocalDate(2026, 1, 21)

        val prefill = defaultPrefillForSelectedTimelineDate(
            selectedDate = selectedDate,
            now = now,
            timeZone = tz
        )

        assertEquals(LocalDate(2026, 1, 20), prefill.startDate)
        assertEquals(LocalDate(2026, 1, 20), prefill.endDate)
    }
}
