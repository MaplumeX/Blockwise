package com.maplume.blockwise.feature.settings.data.backup

import android.content.Context
import com.maplume.blockwise.feature.settings.data.export.JsonExporter
import com.maplume.blockwise.feature.settings.domain.model.BackupInfo
import com.maplume.blockwise.feature.settings.domain.model.ExportData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages backup creation and restoration in app private storage.
 */
@Singleton
class BackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val jsonExporter: JsonExporter
) {
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    private val backupsDir: File
        get() = File(context.filesDir, "backups").also {
            if (!it.exists()) it.mkdirs()
        }

    companion object {
        private const val MAX_BACKUPS = 5
        private const val BACKUP_EXTENSION = ".json"
    }

    /**
     * Create a backup from ExportData.
     */
    suspend fun createBackup(data: ExportData): Result<BackupInfo> = withContext(Dispatchers.IO) {
        try {
            val id = UUID.randomUUID().toString()
            val timestamp = dateFormat.format(Date())
            val fileName = "backup_$timestamp$BACKUP_EXTENSION"
            val file = File(backupsDir, fileName)

            val jsonContent = jsonExporter.export(data)
            file.writeText(jsonContent, Charsets.UTF_8)

            // Clean up old backups
            cleanupOldBackups()

            val backupInfo = BackupInfo(
                id = id,
                fileName = fileName,
                filePath = file.absolutePath,
                createdAt = Clock.System.now(),
                sizeBytes = file.length()
            )

            Result.success(backupInfo)
        } catch (e: Exception) {
            Result.failure(Exception("创建备份失败: ${e.message}"))
        }
    }

    /**
     * Read backup data from a BackupInfo.
     */
    suspend fun readBackup(backupInfo: BackupInfo): Result<ExportData> = withContext(Dispatchers.IO) {
        try {
            val file = File(backupInfo.filePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("备份文件不存在"))
            }

            val content = file.readText(Charsets.UTF_8)
            val exportData = jsonExporter.parse(content)
            Result.success(exportData)
        } catch (e: Exception) {
            Result.failure(Exception("读取备份失败: ${e.message}"))
        }
    }

    /**
     * Get list of available backups sorted by creation time (newest first).
     */
    suspend fun getBackupList(): List<BackupInfo> = withContext(Dispatchers.IO) {
        backupsDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(BACKUP_EXTENSION) }
            ?.sortedByDescending { it.lastModified() }
            ?.map { file ->
                BackupInfo(
                    id = file.name.removeSuffix(BACKUP_EXTENSION),
                    fileName = file.name,
                    filePath = file.absolutePath,
                    createdAt = Instant.fromEpochMilliseconds(file.lastModified()),
                    sizeBytes = file.length()
                )
            }
            ?: emptyList()
    }

    /**
     * Delete a specific backup.
     */
    suspend fun deleteBackup(backupInfo: BackupInfo): Boolean = withContext(Dispatchers.IO) {
        try {
            File(backupInfo.filePath).delete()
        } catch (e: Exception) {
            false
        }
    }

    private fun cleanupOldBackups() {
        val backups = backupsDir.listFiles()
            ?.filter { it.isFile && it.name.endsWith(BACKUP_EXTENSION) }
            ?.sortedByDescending { it.lastModified() }
            ?: return

        // Keep only MAX_BACKUPS, delete older ones
        backups.drop(MAX_BACKUPS).forEach { it.delete() }
    }
}
