package com.maplume.blockwise.feature.goal.domain.usecase

import com.maplume.blockwise.core.domain.model.GoalInput
import com.maplume.blockwise.core.domain.model.GoalPeriod
import com.maplume.blockwise.core.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for updating an existing goal.
 * Validates input and prevents modification of inactive goals.
 */
class UpdateGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(id: Long, input: GoalInput): Result<Unit> {
        // Check if goal exists
        val existingGoal = repository.getById(id)
            ?: return Result.failure(IllegalArgumentException("目标不存在"))

        // Prevent modification of inactive goals
        if (!existingGoal.isActive) {
            return Result.failure(IllegalArgumentException("不能修改已归档的目标"))
        }

        // Validate target minutes
        if (input.targetMinutes <= 0) {
            return Result.failure(IllegalArgumentException("目标时长必须大于0"))
        }

        // Validate custom period dates
        if (input.period == GoalPeriod.CUSTOM) {
            val startDate = input.startDate
            val endDate = input.endDate
            if (startDate == null || endDate == null) {
                return Result.failure(IllegalArgumentException("自定义周期需要设置起止日期"))
            }
            if (startDate >= endDate) {
                return Result.failure(IllegalArgumentException("结束日期必须晚于开始日期"))
            }
        }

        // Check if changing tag and new tag already has active goal
        if (input.tagId != existingGoal.tagId) {
            val conflictingGoal = repository.getActiveByTagId(input.tagId)
            if (conflictingGoal != null) {
                return Result.failure(IllegalArgumentException("该标签已有活动目标"))
            }
        }

        return try {
            repository.update(id, input)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
