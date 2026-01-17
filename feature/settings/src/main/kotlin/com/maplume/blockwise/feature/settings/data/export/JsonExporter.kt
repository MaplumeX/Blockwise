package com.maplume.blockwise.feature.settings.data.export

import com.maplume.blockwise.feature.settings.domain.model.ExportData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Exports data to JSON format.
 */
@Singleton
class JsonExporter @Inject constructor() {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Export data to JSON string.
     */
    fun export(data: ExportData): String {
        return json.encodeToString(data)
    }

    /**
     * Parse JSON string to ExportData.
     */
    fun parse(jsonString: String): ExportData {
        return json.decodeFromString(jsonString)
    }
}
