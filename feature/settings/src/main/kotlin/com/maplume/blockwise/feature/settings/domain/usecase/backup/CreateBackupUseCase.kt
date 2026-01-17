package com.maplume.blockwise.feature.settings.domain.usecase.backup

import com.maplume.blockwise.feature.settings.data.backup.BackupManager
import com.maplume.blockwise.feature.settings.domain.model.BackupResult
import com.maplume.blockwise.feature.settings.domain.model.ExportOptions
import com.maplume.blockwise.feature.settings.domain.usecase.export.ExportDataUseCase
import javax.inject.Inject

/**
 * Use case for creating a backup.
 */
class CreateBackupUseCase @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase,
    private val backupManager: BackupManager
) {
    suspend operator fun invoke(): BackupResult {
        return try {
            val exportData = exportDataUseCase.getExportData(ExportOptions())
            val result = backupManager.createBackup(exportData)
            result.fold(
                onSuccess = { BackupResult.Success(it) },
                onFailure = { BackupResult.Error(it.message ?: "创建备份失败") }
            )
        } catch (e: Exception) {
            BackupResult.Error("创建备份失败: ${e.message}")
        }
    }
}
