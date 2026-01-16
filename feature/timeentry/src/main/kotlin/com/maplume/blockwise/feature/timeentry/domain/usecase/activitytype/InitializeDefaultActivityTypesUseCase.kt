package com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype

import com.maplume.blockwise.core.domain.repository.ActivityTypeRepository
import javax.inject.Inject

/**
 * Use case for initializing default activity types on first launch.
 * Creates preset activity types if none exist.
 */
class InitializeDefaultActivityTypesUseCase @Inject constructor(
    private val repository: ActivityTypeRepository
) {
    /**
     * Initialize default activity types if the database is empty.
     * @return Result indicating success or failure.
     */
    suspend operator fun invoke(): Result<Unit> {
        return try {
            // Check if any activity types exist
            val existingTypes = repository.getById(1)
            if (existingTypes != null) {
                // Already initialized
                return Result.success(Unit)
            }

            // Create default activity types
            DefaultActivityTypes.forEachIndexed { index, type ->
                repository.create(
                    name = type.name,
                    colorHex = type.colorHex,
                    icon = type.icon,
                    parentId = null
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        /**
         * Default activity types to create on first launch.
         */
        val DefaultActivityTypes = listOf(
            DefaultActivityType(
                name = "工作",
                colorHex = "#2196F3",
                icon = "💼"
            ),
            DefaultActivityType(
                name = "学习",
                colorHex = "#4CAF50",
                icon = "📚"
            ),
            DefaultActivityType(
                name = "运动",
                colorHex = "#FF9800",
                icon = "🏃"
            ),
            DefaultActivityType(
                name = "休息",
                colorHex = "#9E9E9E",
                icon = "😴"
            ),
            DefaultActivityType(
                name = "娱乐",
                colorHex = "#9C27B0",
                icon = "🎮"
            ),
            DefaultActivityType(
                name = "其他",
                colorHex = "#00BCD4",
                icon = "📌"
            )
        )
    }
}

/**
 * Data class for default activity type definition.
 */
data class DefaultActivityType(
    val name: String,
    val colorHex: String,
    val icon: String
)
