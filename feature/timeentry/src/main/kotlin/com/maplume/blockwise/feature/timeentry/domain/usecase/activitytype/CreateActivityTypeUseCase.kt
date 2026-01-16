package com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype

import com.maplume.blockwise.core.domain.repository.ActivityTypeRepository
import javax.inject.Inject

/**
 * Use case for creating a new activity type.
 * Includes validation for name uniqueness.
 */
class CreateActivityTypeUseCase @Inject constructor(
    private val repository: ActivityTypeRepository
) {
    /**
     * Create a new activity type.
     * @param name The activity type name (required, must be unique).
     * @param colorHex The color in hex format (e.g., "#FF5722").
     * @param icon Optional icon identifier.
     * @param parentId Optional parent ID for hierarchical structure.
     * @return Result containing the new ID on success, or failure with error message.
     */
    suspend operator fun invoke(
        name: String,
        colorHex: String,
        icon: String? = null,
        parentId: Long? = null
    ): Result<Long> {
        // Validate name
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("名称不能为空"))
        }

        if (trimmedName.length > MAX_NAME_LENGTH) {
            return Result.failure(IllegalArgumentException("名称不能超过${MAX_NAME_LENGTH}个字符"))
        }

        // Validate color format
        if (!isValidColorHex(colorHex)) {
            return Result.failure(IllegalArgumentException("颜色格式无效"))
        }

        // Check name uniqueness
        if (repository.isNameExists(trimmedName)) {
            return Result.failure(IllegalArgumentException("该名称已存在"))
        }

        // Validate parent exists if specified
        if (parentId != null) {
            val parent = repository.getById(parentId)
            if (parent == null) {
                return Result.failure(IllegalArgumentException("父级活动类型不存在"))
            }
            if (parent.isArchived) {
                return Result.failure(IllegalArgumentException("不能在已归档的活动类型下创建子类型"))
            }
        }

        return try {
            val id = repository.create(trimmedName, colorHex, icon, parentId)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isValidColorHex(color: String): Boolean {
        return color.matches(Regex("^#[0-9A-Fa-f]{6}$"))
    }

    companion object {
        const val MAX_NAME_LENGTH = 50
    }
}
