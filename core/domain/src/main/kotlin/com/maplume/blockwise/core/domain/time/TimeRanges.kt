package com.maplume.blockwise.core.domain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

data class TimeRange(
    val startTime: Instant,
    val endTime: Instant
) {
    init {
        require(endTime >= startTime) { "endTime must be >= startTime" }
    }
}

fun dayWindow(date: LocalDate, timeZone: TimeZone): TimeRange {
    val start = date.atTime(LocalTime(0, 0)).toInstant(timeZone)
    val end = LocalDate.fromEpochDays(date.toEpochDays() + 1)
        .atTime(LocalTime(0, 0))
        .toInstant(timeZone)
    return TimeRange(startTime = start, endTime = end)
}

fun overlapRange(a: TimeRange, b: TimeRange): TimeRange? {
    val start = maxOf(a.startTime, b.startTime)
    val end = minOf(a.endTime, b.endTime)
    return if (end > start) TimeRange(startTime = start, endTime = end) else null
}

fun truncateToMinute(instant: Instant, timeZone: TimeZone): Instant {
    val local = instant.toLocalDateTime(timeZone)
    val truncated = LocalDateTime(
        date = local.date,
        time = LocalTime(local.hour, local.minute)
    )
    return truncated.toInstant(timeZone)
}

fun overlapMinutes(entryRange: TimeRange, window: TimeRange, timeZone: TimeZone): Int {
    val overlap = overlapRange(entryRange, window) ?: return 0

    val start = truncateToMinute(overlap.startTime, timeZone)
    val end = truncateToMinute(overlap.endTime, timeZone)

    val millis = end.toEpochMilliseconds() - start.toEpochMilliseconds()
    if (millis <= 0) return 0

    return (millis / 60_000L).toInt()
}
