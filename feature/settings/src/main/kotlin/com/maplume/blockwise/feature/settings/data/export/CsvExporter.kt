package com.maplume.blockwise.feature.settings.data.export

import com.maplume.blockwise.feature.settings.domain.model.ExportData
import com.maplume.blockwise.feature.settings.domain.model.ExportTimeEntry
import kotlinx.datetime.Instant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exports time entries to CSV format.
 */
@Singleton
class CsvExporter @Inject constructor() {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Export time entries to CSV string with UTF-8 BOM for Excel compatibility.
     */
    fun export(data: ExportData): String {
        val sb = StringBuilder()
        // UTF-8 BOM for Excel
        sb.append('\uFEFF')

        // Header
        sb.appendLine("ID,活动ID,活动名称,开始时间,结束时间,持续分钟,备注,标签ID")

        // Build activity name lookup
        val activityNames = data.activityTypes.associate { it.id to it.name }

        // Data rows
        data.timeEntries.forEach { entry ->
            val activityName = activityNames[entry.activityId] ?: "未知"
            val startTime = formatInstant(entry.startTime)
            val endTime = formatInstant(entry.endTime)
            val note = escapeCSV(entry.note ?: "")
            val tagIds = entry.tagIds.joinToString(";")

            sb.appendLine("${entry.id},${entry.activityId},\"$activityName\",\"$startTime\",\"$endTime\",${entry.durationMinutes},\"$note\",\"$tagIds\"")
        }

        return sb.toString()
    }

    private fun formatInstant(epochMillis: Long): String {
        return dateTimeFormat.format(Date(epochMillis))
    }

    private fun escapeCSV(value: String): String {
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", "")
    }
}
