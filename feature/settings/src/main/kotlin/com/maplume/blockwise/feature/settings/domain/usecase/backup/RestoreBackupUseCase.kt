package com.maplume.blockwise.feature.settings.domain.usecase.backup

import com.maplume.blockwise.feature.settings.data.backup.BackupManager
import com.maplume.blockwise.feature.settings.domain.model.BackupInfo
import com.maplume.blockwise.feature.settings.domain.model.ImportStrategy
import com.maplume.blockwise.feature.settings.domain.model.RestoreResult
import com.maplume.blockwise.feature.settings.domain.usecase.imports.ImportDataUseCase
import javax.inject.Inject

/**
 * Use case for restoring from a backup.
 */
class RestoreBackupUseCase @Inject constructor(
    private val backupManager: BackupManager,
    private val importDataUseCase: ImportDataUseCase
) {
    suspend operator fun invoke(backupInfo: BackupInfo): RestoreResult {
        return try {
            val readResult = backupManager.readBackup(backupInfo)
            readResult.fold(
                onSuccess = { exportData ->
                    // Always use REPLACE strategy for restore
                    val importResult = importDataUseCase.importFromData(exportData, ImportStrategy.REPLACE)
                    when (importResult) {
                        is com.maplume.blockwise.feature.settings.domain.model.ImportResult.Success -> {
                            RestoreResult.Success
                        }
                        is com.maplume.blockwise.feature.settings.domain.model.ImportResult.Error -> {
                            RestoreResult.Error(importResult.message)
                        }
                    }
                },
                onFailure = { error ->
                    RestoreResult.Error(error.message ?: "恢复失败")
                }
            )
        } catch (e: Exception) {
            RestoreResult.Error("恢复失败: ${e.message}")
        }
    }
}
