package com.maplume.blockwise.core.data.calculator

import com.maplume.blockwise.core.domain.time.TimeRange
import com.maplume.blockwise.core.domain.time.dayWindow
import com.maplume.blockwise.core.domain.time.overlapMinutes
import com.maplume.blockwise.core.domain.time.overlapMinutesByHourOfDay
import com.maplume.blockwise.core.domain.time.overlapRange
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

class OverlapStatisticsCalculator(private val timeZone: TimeZone = TimeZone.currentSystemDefault()) {

    fun totalMinutes(entries: List<com.maplume.blockwise.core.domain.model.TimeEntry>, window: TimeRange): Int {
        return entries.sumOf { entry ->
            overlapMinutes(
                entryRange = TimeRange(entry.startTime, entry.endTime),
                window = window,
                timeZone = timeZone
            )
        }
    }

    fun entryCount(entries: List<com.maplume.blockwise.core.domain.model.TimeEntry>, window: TimeRange): Int {
        return entries.count { entry ->
            overlapMinutes(
                entryRange = TimeRange(entry.startTime, entry.endTime),
                window = window,
                timeZone = timeZone
            ) > 0
        }
    }

    fun dailyTotals(
        entries: List<com.maplume.blockwise.core.domain.model.TimeEntry>,
        window: TimeRange
    ): Map<LocalDate, Pair<Int, Int>> {
        if (window.endTime <= window.startTime) return emptyMap()

        val startDate = window.startTime.toLocalDateTime(timeZone).date
        val endDateExclusive = window.endTime.toLocalDateTime(timeZone).date

        val result = linkedMapOf<LocalDate, Pair<Int, Int>>()

        var date = startDate
        while (date < endDateExclusive) {
            val dayWin = dayWindow(date, timeZone)
            val clipped = overlapRange(dayWin, window)
            if (clipped != null) {
                val minutes = entries.sumOf { entry ->
                    overlapMinutes(TimeRange(entry.startTime, entry.endTime), clipped, timeZone)
                }
                val count = entries.count { entry ->
                    overlapMinutes(TimeRange(entry.startTime, entry.endTime), clipped, timeZone) > 0
                }
                result[date] = minutes to count
            } else {
                result[date] = 0 to 0
            }
            date = date.plus(1, DateTimeUnit.DAY)
        }

        return result
    }

    fun hourlyTotals(
        entries: List<com.maplume.blockwise.core.domain.model.TimeEntry>,
        window: TimeRange
    ): Map<Int, Int> {
        val out = mutableMapOf<Int, Int>()
        for (entry in entries) {
            val byHour = overlapMinutesByHourOfDay(TimeRange(entry.startTime, entry.endTime), window, timeZone)
            for ((hour, minutes) in byHour) {
                out[hour] = (out[hour] ?: 0) + minutes
            }
        }
        return out
    }

    fun tagTotals(
        entries: List<com.maplume.blockwise.core.domain.model.TimeEntry>,
        window: TimeRange
    ): Map<Long, Pair<Int, Int>> {
        val out = mutableMapOf<Long, Pair<Int, Int>>()

        for (entry in entries) {
            val minutes = overlapMinutes(TimeRange(entry.startTime, entry.endTime), window, timeZone)
            if (minutes <= 0) continue

            val tagIds = entry.tags.map { it.id }
            for (tagId in tagIds) {
                val prev = out[tagId]
                val prevMinutes = prev?.first ?: 0
                val prevCount = prev?.second ?: 0
                out[tagId] = (prevMinutes + minutes) to (prevCount + 1)
            }
        }

        return out
    }

    fun activityTotals(
        entries: List<com.maplume.blockwise.core.domain.model.TimeEntry>,
        window: TimeRange
    ): Map<Long, Pair<Int, Int>> {
        val out = mutableMapOf<Long, Pair<Int, Int>>()

        for (entry in entries) {
            val minutes = overlapMinutes(TimeRange(entry.startTime, entry.endTime), window, timeZone)
            if (minutes <= 0) continue

            val activityId = entry.activityId
            val prev = out[activityId]
            val prevMinutes = prev?.first ?: 0
            val prevCount = prev?.second ?: 0
            out[activityId] = (prevMinutes + minutes) to (prevCount + 1)
        }

        return out
    }
}
