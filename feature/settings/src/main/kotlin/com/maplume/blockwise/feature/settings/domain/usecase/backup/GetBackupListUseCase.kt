package com.maplume.blockwise.feature.settings.domain.usecase.backup

import com.maplume.blockwise.feature.settings.data.backup.BackupManager
import com.maplume.blockwise.feature.settings.domain.model.BackupInfo
import javax.inject.Inject

/**
 * Use case for getting the list of available backups.
 */
class GetBackupListUseCase @Inject constructor(
    private val backupManager: BackupManager
) {
    suspend operator fun invoke(): List<BackupInfo> {
        return backupManager.getBackupList()
    }
}
