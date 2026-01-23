package com.maplume.blockwise.core.testing.fake

import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.model.TimeEntryInput
import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class FakeTimeEntryRepository : TimeEntryRepository {


    private val entries = MutableStateFlow<List<TimeEntry>>(emptyList())
    private var nextId = 1L

    var inputToEntryMapper: ((TimeEntryInput, Long) -> TimeEntry)? = null

    var shouldFail = false
    var failureException: Exception = RuntimeException("Test failure")

    fun getAllEntries(): List<TimeEntry> = entries.value

    fun setEntries(newEntries: List<TimeEntry>) {
        entries.value = newEntries
        nextId = (newEntries.maxOfOrNull { it.id } ?: 0) + 1
    }

    fun clear() {
        entries.value = emptyList()
        nextId = 1L
    }

    override suspend fun create(input: TimeEntryInput): Long {
        if (shouldFail) throw failureException

        val mapper = inputToEntryMapper
            ?: throw IllegalStateException("inputToEntryMapper must be set before creating entries")

        val id = nextId++
        val entry = mapper(input, id)
        entries.value = entries.value + entry
        return id
    }

    override suspend fun update(id: Long, input: TimeEntryInput) {
        if (shouldFail) throw failureException

        val mapper = inputToEntryMapper
            ?: throw IllegalStateException("inputToEntryMapper must be set before updating entries")

        entries.value = entries.value.map { entry ->
            if (entry.id == id) mapper(input, id) else entry
        }
    }

    override suspend fun delete(id: Long) {
        if (shouldFail) throw failureException
        entries.value = entries.value.filter { it.id != id }
    }

    override suspend fun getById(id: Long): TimeEntry? {
        if (shouldFail) throw failureException
        return entries.value.find { it.id == id }
    }

    override fun getByIdFlow(id: Long): Flow<TimeEntry?> {
        return entries.map { list -> list.find { it.id == id } }
    }

    override fun getByTimeRange(startTime: Instant, endTime: Instant): Flow<List<TimeEntry>> {
        return entries.map { list ->
            list.filter { entry ->
                entry.startTime >= startTime && entry.endTime <= endTime
            }
        }
    }

    override fun getByDay(date: LocalDate): Flow<List<TimeEntry>> {
        return entries.map { list ->
            list.filter { entry ->
                val tz = TimeZone.currentSystemDefault()
                val entryDate = entry.startTime.toLocalDateTime(tz).date
                entryDate == date
            }
        }
    }

    override fun getOverlapping(startTime: Instant, endTime: Instant): Flow<List<TimeEntry>> {
        return entries.map { list ->
            list.filter { entry ->
                entry.startTime < endTime && entry.endTime > startTime
            }
        }
    }

    override fun getRecent(limit: Int, offset: Int): Flow<List<TimeEntry>> {
        return entries.map { list ->
            list.sortedByDescending { it.startTime }
                .drop(offset)
                .take(limit)
        }
    }

    override suspend fun hasOverlapping(startTime: Instant, endTime: Instant, excludeId: Long): Boolean {
        if (shouldFail) throw failureException
        return entries.value.any { entry ->
            entry.id != excludeId &&
            entry.startTime < endTime &&
            entry.endTime > startTime
        }
    }

    override suspend fun getTotalDuration(startTime: Instant, endTime: Instant): Int {
        if (shouldFail) throw failureException
        return entries.value
            .filter { it.startTime >= startTime && it.endTime <= endTime }
            .sumOf { it.durationMinutes }
    }

    override suspend fun getEntryCount(startTime: Instant, endTime: Instant): Int {
        if (shouldFail) throw failureException
        return entries.value.count { it.startTime >= startTime && it.endTime <= endTime }
    }

    override suspend fun getLatest(): TimeEntry? {
        if (shouldFail) throw failureException
        return entries.value.maxByOrNull { it.startTime }
    }
}
