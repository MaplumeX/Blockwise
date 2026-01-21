package com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry

import com.maplume.blockwise.core.domain.model.TimeEntryInput
import com.maplume.blockwise.core.domain.repository.ActivityTypeRepository
import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import javax.inject.Inject

/**
 * Use case for updating an existing time entry.
 * Includes validation for time range and activity type.
 */
class UpdateTimeEntryUseCase @Inject constructor(
    private val repository: TimeEntryRepository,
    private val activityTypeRepository: ActivityTypeRepository
) {
    /**
     * Update an existing time entry.
     * @param id The time entry ID to update.
     * @param input The updated time entry data.
     * @return Result indicating success or failure with error message.
     */
    suspend operator fun invoke(id: Long, input: TimeEntryInput): Result<Unit> {
        // Validate time entry exists
        val existing = repository.getById(id)
            ?: return Result.failure(IllegalArgumentException("时间记录不存在"))

        // Validate time range
        if (input.startTime >= input.endTime) {
            return Result.failure(IllegalArgumentException("结束时间必须晚于开始时间"))
        }


        if (input.durationMinutes > MAX_DURATION_MINUTES) {
            return Result.failure(
                IllegalArgumentException("单次记录时长不能超过${MAX_DURATION_MINUTES / 60}小时")
            )
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
            repository.update(id, input)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        const val MAX_DURATION_MINUTES = 24 * 60 // 24 hours
    }
}
