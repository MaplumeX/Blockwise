package com.maplume.blockwise.feature.settings.domain.usecase.export

import com.maplume.blockwise.core.data.dao.ActivityTypeDao
import com.maplume.blockwise.core.data.dao.GoalDao
import com.maplume.blockwise.core.data.dao.TagDao
import com.maplume.blockwise.core.data.dao.TimeEntryDao
import com.maplume.blockwise.core.data.dao.TimeEntryTagDao
import com.maplume.blockwise.feature.settings.data.export.CsvExporter
import com.maplume.blockwise.feature.settings.data.export.FileExportManager
import com.maplume.blockwise.feature.settings.data.export.JsonExporter
import com.maplume.blockwise.feature.settings.domain.model.ExportActivityType
import com.maplume.blockwise.feature.settings.domain.model.ExportData
import com.maplume.blockwise.feature.settings.domain.model.ExportFormat
import com.maplume.blockwise.feature.settings.domain.model.ExportGoal
import com.maplume.blockwise.feature.settings.domain.model.ExportOptions
import com.maplume.blockwise.feature.settings.domain.model.ExportResult
import com.maplume.blockwise.feature.settings.domain.model.ExportTag
import com.maplume.blockwise.feature.settings.domain.model.ExportTimeEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import javax.inject.Inject

/**
 * Use case for exporting application data.
 */
class ExportDataUseCase @Inject constructor(
    private val activityTypeDao: ActivityTypeDao,
    private val tagDao: TagDao,
    private val timeEntryDao: TimeEntryDao,
    private val timeEntryTagDao: TimeEntryTagDao,
    private val goalDao: GoalDao,
    private val jsonExporter: JsonExporter,
    private val csvExporter: CsvExporter,
    private val fileExportManager: FileExportManager
) {
    /**
     * Export data with the specified options.
     */
    suspend operator fun invoke(options: ExportOptions): ExportResult = withContext(Dispatchers.IO) {
        try {
            val exportData = collectExportData(options)
            val content = when (options.format) {
                ExportFormat.JSON -> jsonExporter.export(exportData)
                ExportFormat.CSV -> csvExporter.export(exportData)
            }
            fileExportManager.saveToDownloads(content, options.format)
        } catch (e: Exception) {
            ExportResult.Error("导出失败: ${e.message}")
        }
    }

    /**
     * Get export data without saving to file (for backup purposes).
     */
    suspend fun getExportData(options: ExportOptions = ExportOptions()): ExportData =
        withContext(Dispatchers.IO) {
            collectExportData(options)
        }

    private suspend fun collectExportData(options: ExportOptions): ExportData {
        val activityTypes = if (options.includeActivityTypes) {
            activityTypeDao.getAll().first().map { entity ->
                ExportActivityType(
                    id = entity.id,
                    name = entity.name,
                    colorHex = entity.colorHex,
                    icon = entity.icon,
                    parentId = entity.parentId,
                    displayOrder = entity.displayOrder,
                    isArchived = entity.isArchived
                )
            }
        } else {
            emptyList()
        }

        val tags = if (options.includeTags) {
            tagDao.getAll().first().map { entity ->
                ExportTag(
                    id = entity.id,
                    name = entity.name,
                    colorHex = entity.colorHex,
                    isArchived = entity.isArchived
                )
            }
        } else {
            emptyList()
        }

        val timeEntries = if (options.includeTimeEntries) {
            val allEntries = timeEntryDao.getAllWithDetails().first()

            // Filter by date range if specified
            val filteredEntries = if (options.startDate != null || options.endDate != null) {
                val tz = TimeZone.currentSystemDefault()
                val startInstant = options.startDate?.atStartOfDayIn(tz)
                val endInstant = options.endDate?.let {
                    it.atStartOfDayIn(tz).plus(kotlin.time.Duration.parse("24h"))
                }

                allEntries.filter { entryWithDetails ->
                    val entryStart = entryWithDetails.entry.startTime
                    val afterStart = startInstant == null || entryStart >= startInstant
                    val beforeEnd = endInstant == null || entryStart < endInstant
                    afterStart && beforeEnd
                }
            } else {
                allEntries
            }

            filteredEntries.map { entryWithDetails ->
                ExportTimeEntry(
                    id = entryWithDetails.entry.id,
                    activityId = entryWithDetails.entry.activityId,
                    startTime = entryWithDetails.entry.startTime.toEpochMilliseconds(),
                    endTime = entryWithDetails.entry.endTime.toEpochMilliseconds(),
                    durationMinutes = entryWithDetails.entry.durationMinutes,
                    note = entryWithDetails.entry.note,
                    tagIds = entryWithDetails.tags.map { it.id }
                )
            }
        } else {
            emptyList()
        }

        val goals = if (options.includeGoals) {
            goalDao.getAllGoals().first().map { goalWithTag ->
                ExportGoal(
                    id = goalWithTag.goal.id,
                    tagId = goalWithTag.goal.tagId,
                    targetMinutes = goalWithTag.goal.targetMinutes,
                    goalType = goalWithTag.goal.goalType.name,
                    period = goalWithTag.goal.period.name,
                    startDate = goalWithTag.goal.startDate?.toString(),
                    endDate = goalWithTag.goal.endDate?.toString(),
                    isActive = goalWithTag.goal.isActive
                )
            }
        } else {
            emptyList()
        }

        return ExportData(
            exportedAt = Clock.System.now().toEpochMilliseconds(),
            activityTypes = activityTypes,
            tags = tags,
            timeEntries = timeEntries,
            goals = goals
        )
    }
}
