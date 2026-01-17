package com.maplume.blockwise.feature.settings.presentation.data

import com.maplume.blockwise.feature.settings.domain.model.BackupInfo
import com.maplume.blockwise.feature.settings.domain.model.ExportFormat
import com.maplume.blockwise.feature.settings.domain.model.ImportStrategy
import kotlinx.datetime.LocalDate

/**
 * UI state for the Data Management screen.
 */
data class DataManagementUiState(
    // Export
    val exportFormat: ExportFormat = ExportFormat.JSON,
    val exportStartDate: LocalDate? = null,
    val exportEndDate: LocalDate? = null,
    val isExporting: Boolean = false,
    val exportMessage: String? = null,

    // Import
    val importStrategy: ImportStrategy = ImportStrategy.MERGE,
    val isImporting: Boolean = false,
    val importMessage: String? = null,

    // Backup
    val backupList: List<BackupInfo> = emptyList(),
    val isLoadingBackups: Boolean = false,
    val isCreatingBackup: Boolean = false,
    val isRestoring: Boolean = false,
    val backupMessage: String? = null
)

/**
 * Events from the Data Management screen.
 */
sealed class DataManagementEvent {
    data class ShowSnackbar(val message: String) : DataManagementEvent()
    data object RequestFileSelection : DataManagementEvent()
}
