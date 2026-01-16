package com.maplume.blockwise.feature.timeentry.domain.usecase.tag

import com.maplume.blockwise.core.domain.repository.TagRepository
import javax.inject.Inject

/**
 * Use case for creating a new tag.
 * Includes validation for name uniqueness.
 */
class CreateTagUseCase @Inject constructor(
    private val repository: TagRepository
) {
    /**
     * Create a new tag.
     * @param name The tag name (required, must be unique).
     * @param colorHex The color in hex format (e.g., "#FF5722").
     * @return Result containing the new ID on success, or failure with error message.
     */
    suspend operator fun invoke(
        name: String,
        colorHex: String
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

        return try {
            val id = repository.create(trimmedName, colorHex)
            Result.success(id)
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
