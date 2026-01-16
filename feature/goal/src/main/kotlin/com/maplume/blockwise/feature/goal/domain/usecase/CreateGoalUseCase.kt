package com.maplume.blockwise.feature.goal.domain.usecase

import com.maplume.blockwise.core.domain.model.GoalInput
import com.maplume.blockwise.core.domain.model.GoalPeriod
import com.maplume.blockwise.core.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for creating a new goal.
 * Validates input and ensures no duplicate active goals for the same tag.
 */
class CreateGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(input: GoalInput): Result<Long> {
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

        // Check if active goal exists for this tag
        val existingGoal = repository.getActiveByTagId(input.tagId)
        if (existingGoal != null) {
            return Result.failure(IllegalArgumentException("该标签已有活动目标"))
        }

        return try {
            val id = repository.create(input)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
