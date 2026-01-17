package com.maplume.blockwise.feature.settings.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

/**
 * Export data format.
 */
enum class ExportFormat {
    JSON,
    CSV
}

/**
 * Import strategy for handling conflicts.
 */
enum class ImportStrategy {
    /** Skip existing records and only add new ones */
    MERGE,
    /** Clear all data and replace with imported data */
    REPLACE
}

/**
 * Complete data export structure containing all app data.
 */
@Serializable
data class ExportData(
    val version: Int = CURRENT_VERSION,
    val exportedAt: Long,
    val activityTypes: List<ExportActivityType>,
    val tags: List<ExportTag>,
    val timeEntries: List<ExportTimeEntry>,
    val goals: List<ExportGoal>
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

/**
 * Exported activity type data.
 */
@Serializable
data class ExportActivityType(
    val id: Long,
    val name: String,
    val colorHex: String,
    val icon: String?,
    val parentId: Long?,
    val displayOrder: Int,
    val isArchived: Boolean
)

/**
 * Exported tag data.
 */
@Serializable
data class ExportTag(
    val id: Long,
    val name: String,
    val colorHex: String,
    val isArchived: Boolean
)

/**
 * Exported time entry data.
 */
@Serializable
data class ExportTimeEntry(
    val id: Long,
    val activityId: Long,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int,
    val note: String?,
    val tagIds: List<Long>
)

/**
 * Exported goal data.
 */
@Serializable
data class ExportGoal(
    val id: Long,
    val tagId: Long,
    val targetMinutes: Int,
    val goalType: String,
    val period: String,
    val startDate: String?,
    val endDate: String?,
    val isActive: Boolean
)

/**
 * Export options for filtering data.
 */
data class ExportOptions(
    val format: ExportFormat = ExportFormat.JSON,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val includeActivityTypes: Boolean = true,
    val includeTags: Boolean = true,
    val includeTimeEntries: Boolean = true,
    val includeGoals: Boolean = true
)

/**
 * Result of export operation.
 */
sealed class ExportResult {
    data class Success(val filePath: String, val fileName: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

/**
 * Result of import operation.
 */
sealed class ImportResult {
    data class Success(
        val activityTypesImported: Int,
        val tagsImported: Int,
        val timeEntriesImported: Int,
        val goalsImported: Int
    ) : ImportResult()
    data class Error(val message: String) : ImportResult()
}
