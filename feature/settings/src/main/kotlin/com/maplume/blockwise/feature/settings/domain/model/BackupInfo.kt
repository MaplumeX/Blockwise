package com.maplume.blockwise.feature.settings.domain.model

import kotlinx.datetime.Instant

/**
 * Information about a backup file.
 */
data class BackupInfo(
    val id: String,
    val fileName: String,
    val filePath: String,
    val createdAt: Instant,
    val sizeBytes: Long,
    val description: String? = null
) {
    val formattedSize: String
        get() = when {
            sizeBytes < 1024 -> "$sizeBytes B"
            sizeBytes < 1024 * 1024 -> "${sizeBytes / 1024} KB"
            else -> String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0))
        }
}

/**
 * Result of backup operation.
 */
sealed class BackupResult {
    data class Success(val backupInfo: BackupInfo) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

/**
 * Result of restore operation.
 */
sealed class RestoreResult {
    data object Success : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}
