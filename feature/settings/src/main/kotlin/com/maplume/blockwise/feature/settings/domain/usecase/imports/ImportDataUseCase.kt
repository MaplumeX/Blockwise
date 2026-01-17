package com.maplume.blockwise.feature.settings.domain.usecase.imports

import android.net.Uri
import com.maplume.blockwise.core.data.dao.ActivityTypeDao
import com.maplume.blockwise.core.data.dao.GoalDao
import com.maplume.blockwise.core.data.dao.TagDao
import com.maplume.blockwise.core.data.dao.TimeEntryDao
import com.maplume.blockwise.core.data.dao.TimeEntryTagDao
import com.maplume.blockwise.core.data.entity.ActivityTypeEntity
import com.maplume.blockwise.core.data.entity.GoalEntity
import com.maplume.blockwise.core.data.entity.TagEntity
import com.maplume.blockwise.core.data.entity.TimeEntryEntity
import com.maplume.blockwise.core.data.entity.TimeEntryTagEntity
import com.maplume.blockwise.core.domain.model.GoalPeriod
import com.maplume.blockwise.core.domain.model.GoalType
import com.maplume.blockwise.feature.settings.data.imports.FileImportManager
import com.maplume.blockwise.feature.settings.domain.model.ExportData
import com.maplume.blockwise.feature.settings.domain.model.ImportResult
import com.maplume.blockwise.feature.settings.domain.model.ImportStrategy
import com.maplume.blockwise.feature.settings.domain.usecase.backup.CreateBackupUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Use case for importing application data.
 */
class ImportDataUseCase @Inject constructor(
    private val fileImportManager: FileImportManager,
    private val activityTypeDao: ActivityTypeDao,
    private val tagDao: TagDao,
    private val timeEntryDao: TimeEntryDao,
    private val timeEntryTagDao: TimeEntryTagDao,
    private val goalDao: GoalDao,
    private val createBackupUseCase: CreateBackupUseCase
) {
    /**
     * Import data from URI with the specified strategy.
     */
    suspend operator fun invoke(uri: Uri, strategy: ImportStrategy): ImportResult =
        withContext(Dispatchers.IO) {
            val result = fileImportManager.readFromUri(uri)
            result.fold(
                onSuccess = { exportData ->
                    importData(exportData, strategy)
                },
                onFailure = { error ->
                    ImportResult.Error(error.message ?: "导入失败")
                }
            )
        }

    /**
     * Import data from ExportData object (for backup restore).
     */
    suspend fun importFromData(data: ExportData, strategy: ImportStrategy): ImportResult =
        withContext(Dispatchers.IO) {
            importData(data, strategy)
        }

    private suspend fun importData(data: ExportData, strategy: ImportStrategy): ImportResult {
        return try {
            // Create backup before REPLACE
            if (strategy == ImportStrategy.REPLACE) {
                createBackupUseCase()
            }

            val now = Clock.System.now()

            // Clear existing data if REPLACE strategy
            if (strategy == ImportStrategy.REPLACE) {
                clearAllData()
            }

            var activityTypesImported = 0
            var tagsImported = 0
            var timeEntriesImported = 0
            var goalsImported = 0

            // ID mapping for MERGE strategy
            val activityIdMapping = mutableMapOf<Long, Long>()
            val tagIdMapping = mutableMapOf<Long, Long>()

            // Import activity types
            val existingActivities = if (strategy == ImportStrategy.MERGE) {
                activityTypeDao.getAll().first().associate { it.name to it.id }
            } else {
                emptyMap()
            }

            data.activityTypes.forEach { exportActivity ->
                if (strategy == ImportStrategy.REPLACE ||
                    !existingActivities.containsKey(exportActivity.name)
                ) {
                    val entity = ActivityTypeEntity(
                        id = if (strategy == ImportStrategy.REPLACE) exportActivity.id else 0,
                        name = exportActivity.name,
                        colorHex = exportActivity.colorHex,
                        icon = exportActivity.icon,
                        parentId = exportActivity.parentId,
                        displayOrder = exportActivity.displayOrder,
                        isArchived = exportActivity.isArchived,
                        createdAt = now,
                        updatedAt = now
                    )
                    val newId = activityTypeDao.insert(entity)
                    activityIdMapping[exportActivity.id] = newId
                    activityTypesImported++
                } else {
                    activityIdMapping[exportActivity.id] = existingActivities[exportActivity.name]!!
                }
            }

            // Import tags
            val existingTags = if (strategy == ImportStrategy.MERGE) {
                tagDao.getAll().first().associate { it.name to it.id }
            } else {
                emptyMap()
            }

            data.tags.forEach { exportTag ->
                if (strategy == ImportStrategy.REPLACE ||
                    !existingTags.containsKey(exportTag.name)
                ) {
                    val entity = TagEntity(
                        id = if (strategy == ImportStrategy.REPLACE) exportTag.id else 0,
                        name = exportTag.name,
                        colorHex = exportTag.colorHex,
                        isArchived = exportTag.isArchived,
                        createdAt = now,
                        updatedAt = now
                    )
                    val newId = tagDao.insert(entity)
                    tagIdMapping[exportTag.id] = newId
                    tagsImported++
                } else {
                    tagIdMapping[exportTag.id] = existingTags[exportTag.name]!!
                }
            }

            // Import time entries
            data.timeEntries.forEach { exportEntry ->
                val mappedActivityId = activityIdMapping[exportEntry.activityId] ?: exportEntry.activityId

                val entity = TimeEntryEntity(
                    id = if (strategy == ImportStrategy.REPLACE) exportEntry.id else 0,
                    activityId = mappedActivityId,
                    startTime = Instant.fromEpochMilliseconds(exportEntry.startTime),
                    endTime = Instant.fromEpochMilliseconds(exportEntry.endTime),
                    durationMinutes = exportEntry.durationMinutes,
                    note = exportEntry.note,
                    createdAt = now,
                    updatedAt = now
                )
                val newEntryId = timeEntryDao.insert(entity)

                // Import entry-tag relations
                val mappedTagIds = exportEntry.tagIds.mapNotNull { tagIdMapping[it] ?: it }
                if (mappedTagIds.isNotEmpty()) {
                    val relations = mappedTagIds.map { tagId ->
                        TimeEntryTagEntity(
                            entryId = newEntryId,
                            tagId = tagId
                        )
                    }
                    timeEntryTagDao.insertAll(relations)
                }
                timeEntriesImported++
            }

            // Import goals
            data.goals.forEach { exportGoal ->
                val mappedTagId = tagIdMapping[exportGoal.tagId] ?: exportGoal.tagId

                val entity = GoalEntity(
                    id = if (strategy == ImportStrategy.REPLACE) exportGoal.id else 0,
                    tagId = mappedTagId,
                    targetMinutes = exportGoal.targetMinutes,
                    goalType = GoalType.valueOf(exportGoal.goalType),
                    period = GoalPeriod.valueOf(exportGoal.period),
                    startDate = exportGoal.startDate?.let { LocalDate.parse(it) },
                    endDate = exportGoal.endDate?.let { LocalDate.parse(it) },
                    isActive = exportGoal.isActive,
                    createdAt = now,
                    updatedAt = now
                )
                goalDao.insert(entity)
                goalsImported++
            }

            ImportResult.Success(
                activityTypesImported = activityTypesImported,
                tagsImported = tagsImported,
                timeEntriesImported = timeEntriesImported,
                goalsImported = goalsImported
            )
        } catch (e: Exception) {
            ImportResult.Error("导入失败: ${e.message}")
        }
    }

    private suspend fun clearAllData() {
        // Delete in reverse dependency order
        goalDao.getAllGoals().first().forEach { goalDao.deleteById(it.goal.id) }
        timeEntryDao.getAllWithDetails().first().forEach {
            timeEntryTagDao.deleteByEntryId(it.entry.id)
            timeEntryDao.deleteById(it.entry.id)
        }
        tagDao.getAll().first().forEach { tagDao.deleteById(it.id) }
        activityTypeDao.getAll().first().forEach { activityTypeDao.deleteById(it.id) }
    }
}
