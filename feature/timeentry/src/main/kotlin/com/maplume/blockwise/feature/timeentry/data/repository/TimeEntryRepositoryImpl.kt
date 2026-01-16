package com.maplume.blockwise.feature.timeentry.data.repository

import com.maplume.blockwise.core.data.dao.TimeEntryDao
import com.maplume.blockwise.core.data.dao.TimeEntryTagDao
import com.maplume.blockwise.core.data.mapper.toDomain
import com.maplume.blockwise.core.data.mapper.toEntity
import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.model.TimeEntryInput
import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

class TimeEntryRepositoryImpl @Inject constructor(
    private val timeEntryDao: TimeEntryDao,
    private val timeEntryTagDao: TimeEntryTagDao
) : TimeEntryRepository {

    override suspend fun create(input: TimeEntryInput): Long = withContext(Dispatchers.IO) {
        val now = Clock.System.now()
        val entity = input.toEntity(createdAt = now, updatedAt = now)
        val entryId = timeEntryDao.insert(entity)
        timeEntryTagDao.replaceTagsForEntry(entryId, input.tagIds)
        entryId
    }

    override suspend fun update(id: Long, input: TimeEntryInput) = withContext(Dispatchers.IO) {
        val existing = timeEntryDao.getById(id) ?: return@withContext
        val now = Clock.System.now()
        val entity = input.toEntity(id = id, createdAt = existing.createdAt, updatedAt = now)
        timeEntryDao.update(entity)
        timeEntryTagDao.replaceTagsForEntry(id, input.tagIds)
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        timeEntryDao.deleteById(id)
    }

    override suspend fun getById(id: Long): TimeEntry? {
        return timeEntryDao.getByIdWithDetails(id)?.toDomain()
    }

    override fun getByIdFlow(id: Long): Flow<TimeEntry?> {
        return timeEntryDao.getByIdWithDetailsFlow(id).map { it?.toDomain() }
    }

    override fun getByTimeRange(startTime: Instant, endTime: Instant): Flow<List<TimeEntry>> {
        return timeEntryDao.getByTimeRangeWithDetails(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun getByDay(date: LocalDate): Flow<List<TimeEntry>> {
        val tz = TimeZone.currentSystemDefault()
        val dayStart = date.atStartOfDayIn(tz)
        val dayEnd = dayStart + 1.days
        return timeEntryDao.getByDay(
            dayStart.toEpochMilliseconds(),
            dayEnd.toEpochMilliseconds()
        ).map { list -> list.map { it.toDomain() } }
    }

    override fun getRecent(limit: Int, offset: Int): Flow<List<TimeEntry>> {
        return timeEntryDao.getRecentWithDetails(limit, offset)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun hasOverlapping(startTime: Instant, endTime: Instant, excludeId: Long): Boolean {
        return timeEntryDao.findOverlapping(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds(),
            excludeId
        ) != null
    }

    override suspend fun getTotalDuration(startTime: Instant, endTime: Instant): Int {
        return timeEntryDao.getTotalDurationByTimeRange(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        )
    }

    override suspend fun getEntryCount(startTime: Instant, endTime: Instant): Int {
        return timeEntryDao.countByTimeRange(
            startTime.toEpochMilliseconds(),
            endTime.toEpochMilliseconds()
        )
    }

    override suspend fun getLatest(): TimeEntry? {
        return timeEntryDao.getLatest()?.toDomain()
    }
}

