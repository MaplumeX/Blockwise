package com.maplume.blockwise.feature.settings.data.imports

import android.content.Context
import android.net.Uri
import com.maplume.blockwise.feature.settings.data.export.JsonExporter
import com.maplume.blockwise.feature.settings.domain.model.ExportData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages file import from device storage using SAF.
 */
@Singleton
class FileImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val jsonExporter: JsonExporter
) {
    /**
     * Read and parse JSON file from URI.
     */
    suspend fun readFromUri(uri: Uri): Result<ExportData> = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            } ?: return@withContext Result.failure(Exception("无法读取文件"))

            val exportData = jsonExporter.parse(content)
            Result.success(exportData)
        } catch (e: Exception) {
            Result.failure(Exception("解析文件失败: ${e.message}"))
        }
    }
}
