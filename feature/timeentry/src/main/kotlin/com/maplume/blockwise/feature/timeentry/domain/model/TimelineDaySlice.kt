package com.maplume.blockwise.feature.timeentry.domain.model

import com.maplume.blockwise.core.domain.model.TimeEntry
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

data class TimelineDaySlice(
    val entry: TimeEntry,
    val date: LocalDate,
    val sliceStart: Instant,
    val sliceEnd: Instant
)
