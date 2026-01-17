package com.maplume.blockwise.feature.settings.data.export

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.maplume.blockwise.feature.settings.domain.model.ExportFormat
import com.maplume.blockwise.feature.settings.domain.model.ExportResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.Clock
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages file export to device storage using MediaStore API.
 */
@Singleton
class FileExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    /**
     * Save content to Downloads folder.
     */
    fun saveToDownloads(content: String, format: ExportFormat): ExportResult {
        val timestamp = dateFormat.format(Date())
        val extension = when (format) {
            ExportFormat.JSON -> "json"
            ExportFormat.CSV -> "csv"
        }
        val fileName = "blockwise_export_$timestamp.$extension"
        val mimeType = when (format) {
            ExportFormat.JSON -> "application/json"
            ExportFormat.CSV -> "text/csv"
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Use MediaStore for Android 10+
                saveWithMediaStore(content, fileName, mimeType)
            } else {
                // Use direct file access for older Android versions
                saveToExternalStorage(content, fileName)
            }
        } catch (e: Exception) {
            ExportResult.Error("导出失败: ${e.message}")
        }
    }

    private fun saveWithMediaStore(content: String, fileName: String, mimeType: String): ExportResult {
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return ExportResult.Error("无法创建文件")

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray(Charsets.UTF_8))
            }

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            ExportResult.Success(
                filePath = uri.toString(),
                fileName = fileName
            )
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            ExportResult.Error("写入文件失败: ${e.message}")
        }
    }

    @Suppress("DEPRECATION")
    private fun saveToExternalStorage(content: String, fileName: String): ExportResult {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        val file = File(downloadsDir, fileName)
        FileOutputStream(file).use { outputStream ->
            outputStream.write(content.toByteArray(Charsets.UTF_8))
        }

        return ExportResult.Success(
            filePath = file.absolutePath,
            fileName = fileName
        )
    }
}
