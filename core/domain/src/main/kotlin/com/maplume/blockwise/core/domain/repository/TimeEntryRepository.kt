package com.maplume.blockwise.core.domain.repository

import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.model.TimeEntryInput
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

interface TimeEntryRepository {
    suspend fun create(input: TimeEntryInput): Long

    suspend fun update(id: Long, input: TimeEntryInput)

    suspend fun delete(id: Long)

    suspend fun getById(id: Long): TimeEntry?

    fun getByIdFlow(id: Long): Flow<TimeEntry?>

    fun getByTimeRange(startTime: Instant, endTime: Instant): Flow<List<TimeEntry>>

    fun getByDay(date: LocalDate): Flow<List<TimeEntry>>

    fun getRecent(limit: Int, offset: Int): Flow<List<TimeEntry>>

    suspend fun hasOverlapping(startTime: Instant, endTime: Instant, excludeId: Long = 0): Boolean

    suspend fun getTotalDuration(startTime: Instant, endTime: Instant): Int

    suspend fun getEntryCount(startTime: Instant, endTime: Instant): Int

    suspend fun getLatest(): TimeEntry?
}

