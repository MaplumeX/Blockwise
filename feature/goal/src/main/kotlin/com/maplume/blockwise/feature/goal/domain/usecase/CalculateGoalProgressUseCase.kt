package com.maplume.blockwise.feature.goal.domain.usecase

import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalPeriod
import com.maplume.blockwise.core.domain.model.GoalProgress
import com.maplume.blockwise.core.domain.repository.StatisticsRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Use case for calculating goal progress.
 * Computes current progress based on time entries within the goal's period.
 */
class CalculateGoalProgressUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    /**
     * Calculate progress for a single goal.
     */
    suspend operator fun invoke(goal: Goal): GoalProgress {
        val (startTime, endTime) = goal.currentPeriodRange()

        val currentMinutes = statisticsRepository.getTotalMinutesForTag(
            tagId = goal.tagId,
            startTime = startTime,
            endTime = endTime
        )

        return GoalProgress(
            goal = goal,
            currentMinutes = currentMinutes,
            targetMinutes = goal.targetMinutes
        )
    }

    /**
     * Calculate progress for multiple goals.
     */
    suspend fun calculateAll(goals: List<Goal>): List<GoalProgress> {
        return goals.map { invoke(it) }
    }
}

/**
 * Extension function to calculate the current period's time range for a goal.
 */
fun Goal.currentPeriodRange(): Pair<Instant, Instant> {
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(tz).date

    return when (period) {
        GoalPeriod.DAILY -> {
            val start = now.atStartOfDayIn(tz)
            val end = now.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz)
            start to end
        }
        GoalPeriod.WEEKLY -> {
            val weekStart = now.startOfWeek()
            val start = weekStart.atStartOfDayIn(tz)
            val end = weekStart.plus(7, DateTimeUnit.DAY).atStartOfDayIn(tz)
            start to end
        }
        GoalPeriod.MONTHLY -> {
            val monthStart = LocalDate(now.year, now.monthNumber, 1)
            val start = monthStart.atStartOfDayIn(tz)
            val end = monthStart.plus(1, DateTimeUnit.MONTH).atStartOfDayIn(tz)
            start to end
        }
        GoalPeriod.CUSTOM -> {
            val start = startDate!!.atStartOfDayIn(tz)
            val end = endDate!!.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz)
            start to end
        }
    }
}

/**
 * Extension function to get the start of the week (Monday).
 */
fun LocalDate.startOfWeek(): LocalDate {
    val daysFromMonday = when (dayOfWeek) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        DayOfWeek.SUNDAY -> 6
    }
    return this.plus(-daysFromMonday, DateTimeUnit.DAY)
}
