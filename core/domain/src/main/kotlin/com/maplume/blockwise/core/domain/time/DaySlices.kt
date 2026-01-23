package com.maplume.blockwise.core.domain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

data class DaySlice(
    val date: LocalDate,
    val sliceStart: Instant,
    val sliceEnd: Instant
) {
    init {
        require(sliceEnd >= sliceStart) { "sliceEnd must be >= sliceStart" }
    }
}

fun daySliceForEntry(entryRange: TimeRange, date: LocalDate, timeZone: TimeZone): DaySlice? {
    val window = dayWindow(date, timeZone)
    val overlap = overlapRange(entryRange, window) ?: return null
    return DaySlice(date = date, sliceStart = overlap.startTime, sliceEnd = overlap.endTime)
}
