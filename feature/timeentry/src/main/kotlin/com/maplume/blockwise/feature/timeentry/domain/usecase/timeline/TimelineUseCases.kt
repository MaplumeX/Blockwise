package com.maplume.blockwise.feature.timeentry.domain.usecase.timeline

import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Data class representing a group of time entries for a specific date.
 */
data class DayGroup(
    val date: LocalDate,
    val entries: List<TimeEntry>,
    val totalMinutes: Int
) {
    val formattedTotalDuration: String
        get() {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return when {
                hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
                hours > 0 -> "${hours}小时"
                else -> "${minutes}分钟"
            }
        }
}

/**
 * Use case for getting time entries grouped by date for the timeline view.
 */
class GetTimelineEntriesUseCase @Inject constructor(
    private val repository: TimeEntryRepository
) {
    /**
     * Get recent time entries grouped by date.
     * @param limit Maximum number of entries to return.
     * @param offset Number of entries to skip.
     * @return Flow of day groups sorted by date descending.
     */
    operator fun invoke(limit: Int = 50, offset: Int = 0): Flow<List<DayGroup>> {
        return repository.getRecent(limit, offset).map { entries ->
            groupEntriesByDate(entries)
        }
    }

    /**
     * Group entries by date and calculate totals.
     */
    private fun groupEntriesByDate(entries: List<TimeEntry>): List<DayGroup> {
        return entries
            .groupBy { entry ->
                entry.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
            }
            .map { (date, dayEntries) ->
                DayGroup(
                    date = date,
                    entries = dayEntries.sortedByDescending { it.startTime },
                    totalMinutes = dayEntries.sumOf { it.durationMinutes }
                )
            }
            .sortedByDescending { it.date }
    }
}

/**
 * Use case for splitting a time entry into two entries.
 */
class SplitTimeEntryUseCase @Inject constructor(
    private val repository: TimeEntryRepository
) {
    /**
     * Split a time entry at the specified time point.
     * @param entryId The ID of the entry to split.
     * @param splitTime The time point to split at (must be between start and end time).
     * @return Result containing the IDs of the two new entries.
     */
    suspend operator fun invoke(entryId: Long, splitTime: kotlinx.datetime.Instant): Result<Pair<Long, Long>> {
        val entry = repository.getById(entryId)
            ?: return Result.failure(IllegalArgumentException("时间记录不存在"))

        // Validate split time is within the entry's time range
        if (splitTime <= entry.startTime || splitTime >= entry.endTime) {
            return Result.failure(IllegalArgumentException("拆分时间点必须在记录的开始和结束时间之间"))
        }

        return try {
            // Create first entry (start to split point)
            val firstInput = com.maplume.blockwise.core.domain.model.TimeEntryInput(
                activityId = entry.activityId,
                startTime = entry.startTime,
                endTime = splitTime,
                note = entry.note,
                tagIds = entry.tags.map { it.id }
            )

            // Create second entry (split point to end)
            val secondInput = com.maplume.blockwise.core.domain.model.TimeEntryInput(
                activityId = entry.activityId,
                startTime = splitTime,
                endTime = entry.endTime,
                note = entry.note,
                tagIds = entry.tags.map { it.id }
            )

            // Create new entries
            val firstId = repository.create(firstInput)
            val secondId = repository.create(secondInput)

            // Delete original entry
            repository.delete(entryId)

            Result.success(firstId to secondId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

/**
 * Use case for merging multiple adjacent time entries into one.
 */
class MergeTimeEntriesUseCase @Inject constructor(
    private val repository: TimeEntryRepository
) {
    /**
     * Merge multiple time entries into one.
     * Entries must have the same activity type and be adjacent (no gaps).
     * @param entryIds The IDs of the entries to merge (must be at least 2).
     * @return Result containing the ID of the merged entry.
     */
    suspend operator fun invoke(entryIds: List<Long>): Result<Long> {
        if (entryIds.size < 2) {
            return Result.failure(IllegalArgumentException("至少需要选择两条记录进行合并"))
        }

        // Get all entries
        val entries = entryIds.mapNotNull { repository.getById(it) }
        if (entries.size != entryIds.size) {
            return Result.failure(IllegalArgumentException("部分时间记录不存在"))
        }

        // Sort by start time
        val sortedEntries = entries.sortedBy { it.startTime }

        // Validate all entries have the same activity type
        val activityId = sortedEntries.first().activityId
        if (!sortedEntries.all { it.activityId == activityId }) {
            return Result.failure(IllegalArgumentException("只能合并相同活动类型的记录"))
        }

        // Validate entries are adjacent (allow small gaps up to 1 minute)
        for (i in 0 until sortedEntries.size - 1) {
            val current = sortedEntries[i]
            val next = sortedEntries[i + 1]
            val gapMillis = next.startTime.toEpochMilliseconds() - current.endTime.toEpochMilliseconds()
            if (gapMillis > 60000) { // More than 1 minute gap
                return Result.failure(IllegalArgumentException("只能合并相邻的记录（间隔不超过1分钟）"))
            }
        }

        return try {
            // Collect all unique tags from all entries
            val allTagIds = sortedEntries.flatMap { entry -> entry.tags.map { it.id } }.distinct()

            // Combine notes
            val combinedNote = sortedEntries
                .mapNotNull { it.note }
                .filter { it.isNotBlank() }
                .joinToString("\n")
                .takeIf { it.isNotBlank() }

            // Create merged entry
            val mergedInput = com.maplume.blockwise.core.domain.model.TimeEntryInput(
                activityId = activityId,
                startTime = sortedEntries.first().startTime,
                endTime = sortedEntries.last().endTime,
                note = combinedNote,
                tagIds = allTagIds
            )

            val mergedId = repository.create(mergedInput)

            // Delete original entries
            entryIds.forEach { repository.delete(it) }

            Result.success(mergedId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
