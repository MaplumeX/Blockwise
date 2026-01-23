package com.maplume.blockwise.core.domain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HourlyBucketsTest {

    private val tz = TimeZone.UTC

    private fun instant(date: LocalDate, time: LocalTime, second: Int = 0): Instant {
        return LocalDateTime(date, LocalTime(time.hour, time.minute, second)).toInstant(tz)
    }

    @Test
    fun `overlapMinutesByHourOfDay splits across hours`() {
        val d1 = LocalDate(2026, 1, 1)
        val d2 = LocalDate(2026, 1, 2)

        val entry = TimeRange(
            startTime = instant(d1, LocalTime(23, 30), second = 10),
            endTime = instant(d2, LocalTime(0, 30), second = 20)
        )

        val window = TimeRange(
            startTime = instant(d1, LocalTime(0, 0)),
            endTime = instant(d2, LocalTime(0, 0))
        )

        val minutesByHour = overlapMinutesByHourOfDay(entry, window, tz)

        assertEquals(30, minutesByHour[23])
        assertEquals(null, minutesByHour[0])
    }
}
