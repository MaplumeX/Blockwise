package com.maplume.blockwise.feature.settings.presentation.data

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.feature.settings.domain.model.BackupInfo
import com.maplume.blockwise.feature.settings.domain.model.BackupResult
import com.maplume.blockwise.feature.settings.domain.model.ExportFormat
import com.maplume.blockwise.feature.settings.domain.model.ExportOptions
import com.maplume.blockwise.feature.settings.domain.model.ExportResult
import com.maplume.blockwise.feature.settings.domain.model.ImportResult
import com.maplume.blockwise.feature.settings.domain.model.ImportStrategy
import com.maplume.blockwise.feature.settings.domain.model.RestoreResult
import com.maplume.blockwise.feature.settings.domain.usecase.backup.CreateBackupUseCase
import com.maplume.blockwise.feature.settings.domain.usecase.backup.GetBackupListUseCase
import com.maplume.blockwise.feature.settings.domain.usecase.backup.RestoreBackupUseCase
import com.maplume.blockwise.feature.settings.domain.usecase.export.ExportDataUseCase
import com.maplume.blockwise.feature.settings.domain.usecase.imports.ImportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * ViewModel for the Data Management screen.
 */
@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,
    private val createBackupUseCase: CreateBackupUseCase,
    private val getBackupListUseCase: GetBackupListUseCase,
    private val restoreBackupUseCase: RestoreBackupUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataManagementUiState())
    val uiState: StateFlow<DataManagementUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DataManagementEvent>()
    val events: SharedFlow<DataManagementEvent> = _events.asSharedFlow()

    init {
        loadBackupList()
    }

    // ==================== Export ====================

    fun onExportFormatSelected(format: ExportFormat) {
        _uiState.update { it.copy(exportFormat = format) }
    }

    fun onExportStartDateSelected(date: LocalDate?) {
        _uiState.update { it.copy(exportStartDate = date) }
    }

    fun onExportEndDateSelected(date: LocalDate?) {
        _uiState.update { it.copy(exportEndDate = date) }
    }

    fun onExportClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportMessage = null) }

            val options = ExportOptions(
                format = _uiState.value.exportFormat,
                startDate = _uiState.value.exportStartDate,
                endDate = _uiState.value.exportEndDate
            )

            when (val result = exportDataUseCase(options)) {
                is ExportResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportMessage = "导出成功: ${result.fileName}"
                        )
                    }
                    _events.emit(DataManagementEvent.ShowSnackbar("已导出到下载目录: ${result.fileName}"))
                }
                is ExportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            exportMessage = result.message
                        )
                    }
                    _events.emit(DataManagementEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    // ==================== Import ====================

    fun onImportStrategySelected(strategy: ImportStrategy) {
        _uiState.update { it.copy(importStrategy = strategy) }
    }

    fun onImportClick() {
        viewModelScope.launch {
            _events.emit(DataManagementEvent.RequestFileSelection)
        }
    }

    fun onFileSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, importMessage = null) }

            when (val result = importDataUseCase(uri, _uiState.value.importStrategy)) {
                is ImportResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importMessage = "导入成功"
                        )
                    }
                    val message = buildString {
                        append("导入完成: ")
                        val items = mutableListOf<String>()
                        if (result.activityTypesImported > 0) items.add("${result.activityTypesImported}个活动类型")
                        if (result.tagsImported > 0) items.add("${result.tagsImported}个标签")
                        if (result.timeEntriesImported > 0) items.add("${result.timeEntriesImported}条时间记录")
                        if (result.goalsImported > 0) items.add("${result.goalsImported}个目标")
                        append(items.joinToString(", "))
                    }
                    _events.emit(DataManagementEvent.ShowSnackbar(message))
                }
                is ImportResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isImporting = false,
                            importMessage = result.message
                        )
                    }
                    _events.emit(DataManagementEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    // ==================== Backup ====================

    private fun loadBackupList() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBackups = true) }
            val backups = getBackupListUseCase()
            _uiState.update { it.copy(backupList = backups, isLoadingBackups = false) }
        }
    }

    fun onCreateBackupClick() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingBackup = true, backupMessage = null) }

            when (val result = createBackupUseCase()) {
                is BackupResult.Success -> {
                    _uiState.update { it.copy(isCreatingBackup = false) }
                    loadBackupList()
                    _events.emit(DataManagementEvent.ShowSnackbar("备份创建成功"))
                }
                is BackupResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCreatingBackup = false,
                            backupMessage = result.message
                        )
                    }
                    _events.emit(DataManagementEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    fun onRestoreBackupClick(backupInfo: BackupInfo) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true, backupMessage = null) }

            when (val result = restoreBackupUseCase(backupInfo)) {
                is RestoreResult.Success -> {
                    _uiState.update { it.copy(isRestoring = false) }
                    _events.emit(DataManagementEvent.ShowSnackbar("恢复成功"))
                }
                is RestoreResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isRestoring = false,
                            backupMessage = result.message
                        )
                    }
                    _events.emit(DataManagementEvent.ShowSnackbar(result.message))
                }
            }
        }
    }

    fun onDeleteBackupClick(backupInfo: BackupInfo) {
        // TODO: Implement backup deletion
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                exportMessage = null,
                importMessage = null,
                backupMessage = null
            )
        }
    }
}
