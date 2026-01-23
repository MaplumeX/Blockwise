package com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry

import com.maplume.blockwise.core.domain.model.TimeEntryInput
import com.maplume.blockwise.core.domain.repository.ActivityTypeRepository
import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import javax.inject.Inject

/**
 * Use case for creating a new time entry.
 * Includes validation for time range and activity type.
 */
class CreateTimeEntryUseCase @Inject constructor(
    private val repository: TimeEntryRepository,
    private val activityTypeRepository: ActivityTypeRepository
) {
    /**
     * Create a new time entry.
     * @param input The time entry input data.
     * @return Result containing the new ID on success, or failure with error message.
     */
    suspend operator fun invoke(input: TimeEntryInput): Result<Long> {
        // Validate time range
        if (input.startTime >= input.endTime) {
            return Result.failure(IllegalArgumentException("结束时间必须晚于开始时间"))
        }

        // Validate activity type exists and is active
        val activityType = activityTypeRepository.getById(input.activityId)
        if (activityType == null) {
            return Result.failure(IllegalArgumentException("活动类型不存在"))
        }
        if (activityType.isArchived) {
            return Result.failure(IllegalArgumentException("该活动类型已被归档"))
        }

        return try {
            val id = repository.create(input)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
