package com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype

import com.maplume.blockwise.core.domain.repository.ActivityTypeRepository
import javax.inject.Inject

/**
 * Use case for updating an existing activity type.
 * Includes validation for name uniqueness (excluding self).
 */
class UpdateActivityTypeUseCase @Inject constructor(
    private val repository: ActivityTypeRepository
) {
    /**
     * Update an existing activity type.
     * @param id The activity type ID to update.
     * @param name The new name (required, must be unique excluding self).
     * @param colorHex The new color in hex format.
     * @param icon Optional new icon identifier.
     * @param parentId Optional new parent ID.
     * @return Result indicating success or failure with error message.
     */
    suspend operator fun invoke(
        id: Long,
        name: String,
        colorHex: String,
        icon: String? = null,
        parentId: Long? = null
    ): Result<Unit> {
        // Validate activity type exists
        val existing = repository.getById(id)
            ?: return Result.failure(IllegalArgumentException("活动类型不存在"))

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

        // Check name uniqueness (excluding self)
        if (repository.isNameExists(trimmedName, excludeId = id)) {
            return Result.failure(IllegalArgumentException("该名称已存在"))
        }

        // Validate parent if specified
        if (parentId != null) {
            // Cannot set self as parent
            if (parentId == id) {
                return Result.failure(IllegalArgumentException("不能将自己设为父级"))
            }

            val parent = repository.getById(parentId)
            if (parent == null) {
                return Result.failure(IllegalArgumentException("父级活动类型不存在"))
            }
            if (parent.isArchived) {
                return Result.failure(IllegalArgumentException("不能移动到已归档的活动类型下"))
            }

            // Prevent circular reference (parent cannot be a child of this type)
            if (isDescendant(parentId, id)) {
                return Result.failure(IllegalArgumentException("不能将子级设为父级"))
            }
        }

        return try {
            repository.update(id, trimmedName, colorHex, icon, parentId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Check if potentialDescendant is a descendant of ancestorId.
     */
    private suspend fun isDescendant(potentialDescendant: Long, ancestorId: Long): Boolean {
        var currentId: Long? = potentialDescendant
        val visited = mutableSetOf<Long>()

        while (currentId != null && currentId !in visited) {
            visited.add(currentId)
            val current = repository.getById(currentId) ?: return false
            if (current.parentId == ancestorId) {
                return true
            }
            currentId = current.parentId
        }
        return false
    }

    private fun isValidColorHex(color: String): Boolean {
        return color.matches(Regex("^#[0-9A-Fa-f]{6}$"))
    }

    companion object {
        const val MAX_NAME_LENGTH = 50
    }
}
