package com.maplume.blockwise.feature.timeentry.domain.usecase.tag

import com.maplume.blockwise.core.domain.repository.TagRepository
import javax.inject.Inject

/**
 * Use case for updating an existing tag.
 * Includes validation for name uniqueness (excluding self).
 */
class UpdateTagUseCase @Inject constructor(
    private val repository: TagRepository
) {
    /**
     * Update an existing tag.
     * @param id The tag ID to update.
     * @param name The new name (required, must be unique excluding self).
     * @param colorHex The new color in hex format.
     * @return Result indicating success or failure with error message.
     */
    suspend operator fun invoke(
        id: Long,
        name: String,
        colorHex: String
    ): Result<Unit> {
        // Validate tag exists
        val existing = repository.getById(id)
            ?: return Result.failure(IllegalArgumentException("标签不存在"))

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

        return try {
            repository.update(id, trimmedName, colorHex)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isValidColorHex(color: String): Boolean {
        return color.matches(Regex("^#[0-9A-Fa-f]{6}$"))
    }

    companion object {
        const val MAX_NAME_LENGTH = 30
    }
}
