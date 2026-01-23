package com.maplume.blockwise.core.domain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun hourWindows(window: TimeRange, timeZone: TimeZone): List<TimeRange> {
    if (window.endTime <= window.startTime) return emptyList()

    val startLocal = window.startTime.toLocalDateTime(timeZone)
    val endLocal = window.endTime.toLocalDateTime(timeZone)

    var cursor = LocalDateTime(
        date = startLocal.date,
        time = kotlinx.datetime.LocalTime(startLocal.hour, 0)
    ).toInstant(timeZone)

    val result = mutableListOf<TimeRange>()

    while (cursor < window.endTime) {
        val next = cursor.plus(1, kotlinx.datetime.DateTimeUnit.HOUR, timeZone)
        result += TimeRange(startTime = cursor, endTime = next)
        cursor = next

        if (result.size > 24 * 366 * 2) break
    }

    return result.mapNotNull { overlapRange(it, window) }
}

fun overlapMinutesByHourOfDay(entryRange: TimeRange, window: TimeRange, timeZone: TimeZone): Map<Int, Int> {
    if (window.endTime <= window.startTime) return emptyMap()

    val buckets = hourWindows(window, timeZone)
    val out = mutableMapOf<Int, Int>()

    for (bucket in buckets) {
        val minutes = overlapMinutes(entryRange, bucket, timeZone)
        if (minutes <= 0) continue

        val hour = bucket.startTime.toLocalDateTime(timeZone).hour
        out[hour] = (out[hour] ?: 0) + minutes
    }

    return out
}
