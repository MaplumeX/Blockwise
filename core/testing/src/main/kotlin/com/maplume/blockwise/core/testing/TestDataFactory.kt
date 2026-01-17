package com.maplume.blockwise.core.testing

import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.CategoryStatistics
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalInput
import com.maplume.blockwise.core.domain.model.GoalPeriod
import com.maplume.blockwise.core.domain.model.GoalProgress
import com.maplume.blockwise.core.domain.model.GoalType
import com.maplume.blockwise.core.domain.model.HourlyPattern
import com.maplume.blockwise.core.domain.model.StatisticsSummary
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.model.TimeEntryInput
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Factory object for creating test data.
 * Provides consistent test data across all test files.
 */
object TestDataFactory {

    // ==================== ActivityType ====================

    /**
     * Create an ActivityType with default values.
     */
    fun createActivityType(
        id: Long = 1,
        name: String = "Test Activity",
        colorHex: String = "#FF5722",
        icon: String? = "work",
        parentId: Long? = null,
        displayOrder: Int = 0,
        isArchived: Boolean = false,
        children: List<ActivityType> = emptyList()
    ) = ActivityType(
        id = id,
        name = name,
        colorHex = colorHex,
        icon = icon,
        parentId = parentId,
        displayOrder = displayOrder,
        isArchived = isArchived,
        children = children
    )

    /**
     * Create a list of default activity types for testing.
     */
    fun createDefaultActivityTypes(): List<ActivityType> = listOf(
        createActivityType(id = 1, name = "工作", colorHex = "#4CAF50", icon = "work"),
        createActivityType(id = 2, name = "学习", colorHex = "#2196F3", icon = "school"),
        createActivityType(id = 3, name = "运动", colorHex = "#FF9800", icon = "fitness_center"),
        createActivityType(id = 4, name = "休息", colorHex = "#9C27B0", icon = "hotel"),
        createActivityType(id = 5, name = "已归档", colorHex = "#607D8B", icon = "archive", isArchived = true)
    )

    // ==================== Tag ====================

    /**
     * Create a Tag with default values.
     */
    fun createTag(
        id: Long = 1,
        name: String = "Test Tag",
        colorHex: String = "#E91E63",
        isArchived: Boolean = false
    ) = Tag(
        id = id,
        name = name,
        colorHex = colorHex,
        isArchived = isArchived
    )

    /**
     * Create a list of default tags for testing.
     */
    fun createDefaultTags(): List<Tag> = listOf(
        createTag(id = 1, name = "重要", colorHex = "#F44336"),
        createTag(id = 2, name = "紧急", colorHex = "#FF9800"),
        createTag(id = 3, name = "高效", colorHex = "#4CAF50"),
        createTag(id = 4, name = "低效", colorHex = "#9E9E9E"),
        createTag(id = 5, name = "已归档", colorHex = "#607D8B", isArchived = true)
    )

    // ==================== TimeEntry ====================

    /**
     * Create a TimeEntry with default values.
     */
    fun createTimeEntry(
        id: Long = 1,
        activity: ActivityType = createActivityType(),
        startTime: Instant = Clock.System.now().minus(1.hours),
        endTime: Instant = Clock.System.now(),
        durationMinutes: Int = 60,
        note: String? = null,
        tags: List<Tag> = emptyList()
    ) = TimeEntry(
        id = id,
        activity = activity,
        startTime = startTime,
        endTime = endTime,
        durationMinutes = durationMinutes,
        note = note,
        tags = tags
    )

    /**
     * Create a TimeEntryInput with default values.
     */
    fun createTimeEntryInput(
        activityId: Long = 1,
        startTime: Instant = Clock.System.now().minus(1.hours),
        endTime: Instant = Clock.System.now(),
        note: String? = null,
        tagIds: List<Long> = emptyList()
    ) = TimeEntryInput(
        activityId = activityId,
        startTime = startTime,
        endTime = endTime,
        note = note,
        tagIds = tagIds
    )

    /**
     * Create a time entry with specific duration in minutes.
     */
    fun createTimeEntryWithDuration(
        id: Long = 1,
        activity: ActivityType = createActivityType(),
        durationMinutes: Int = 60,
        baseTime: Instant = Clock.System.now(),
        note: String? = null,
        tags: List<Tag> = emptyList()
    ): TimeEntry {
        val endTime = baseTime
        val startTime = endTime.minus(durationMinutes.minutes)
        return TimeEntry(
            id = id,
            activity = activity,
            startTime = startTime,
            endTime = endTime,
            durationMinutes = durationMinutes,
            note = note,
            tags = tags
        )
    }

    // ==================== Goal ====================

    /**
     * Create a Goal with default values.
     */
    fun createGoal(
        id: Long = 1,
        tag: Tag = createTag(),
        targetMinutes: Int = 120,
        goalType: GoalType = GoalType.MIN,
        period: GoalPeriod = GoalPeriod.DAILY,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        isActive: Boolean = true
    ) = Goal(
        id = id,
        tag = tag,
        targetMinutes = targetMinutes,
        goalType = goalType,
        period = period,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive
    )

    /**
     * Create a GoalInput with default values.
     */
    fun createGoalInput(
        tagId: Long = 1,
        targetMinutes: Int = 120,
        goalType: GoalType = GoalType.MIN,
        period: GoalPeriod = GoalPeriod.DAILY,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        isActive: Boolean = true
    ) = GoalInput(
        tagId = tagId,
        targetMinutes = targetMinutes,
        goalType = goalType,
        period = period,
        startDate = startDate,
        endDate = endDate,
        isActive = isActive
    )

    /**
     * Create a GoalProgress with default values.
     */
    fun createGoalProgress(
        goal: Goal = createGoal(),
        currentMinutes: Int = 60,
        targetMinutes: Int = goal.targetMinutes
    ) = GoalProgress(
        goal = goal,
        currentMinutes = currentMinutes,
        targetMinutes = targetMinutes
    )

    // ==================== Statistics ====================

    /**
     * Create a StatisticsSummary with default values.
     */
    fun createStatisticsSummary(
        totalMinutes: Int = 480,
        entryCount: Int = 8,
        previousPeriodMinutes: Int? = null
    ) = StatisticsSummary(
        totalMinutes = totalMinutes,
        entryCount = entryCount,
        previousPeriodMinutes = previousPeriodMinutes
    )

    /**
     * Create a CategoryStatistics with default values.
     */
    fun createCategoryStatistics(
        id: Long = 1,
        name: String = "Test Category",
        colorHex: String = "#4CAF50",
        totalMinutes: Int = 120,
        entryCount: Int = 3,
        percentage: Float = 25f
    ) = CategoryStatistics(
        id = id,
        name = name,
        colorHex = colorHex,
        totalMinutes = totalMinutes,
        entryCount = entryCount,
        percentage = percentage
    )

    /**
     * Create a list of CategoryStatistics for testing.
     */
    fun createCategoryStatisticsList(): List<CategoryStatistics> = listOf(
        createCategoryStatistics(id = 1, name = "工作", colorHex = "#4CAF50", totalMinutes = 240, percentage = 50f),
        createCategoryStatistics(id = 2, name = "学习", colorHex = "#2196F3", totalMinutes = 120, percentage = 25f),
        createCategoryStatistics(id = 3, name = "运动", colorHex = "#FF9800", totalMinutes = 60, percentage = 12.5f),
        createCategoryStatistics(id = 4, name = "休息", colorHex = "#9C27B0", totalMinutes = 60, percentage = 12.5f)
    )

    /**
     * Create a DailyTrend with default values.
     */
    fun createDailyTrend(
        date: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
        totalMinutes: Int = 480,
        entryCount: Int = 8
    ) = DailyTrend(
        date = date,
        totalMinutes = totalMinutes,
        entryCount = entryCount
    )

    /**
     * Create a list of DailyTrend for testing (7 days).
     */
    fun createDailyTrendList(days: Int = 7): List<DailyTrend> {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        return (0 until days).map { dayOffset ->
            val date = today.plus(-dayOffset, DateTimeUnit.DAY)
            createDailyTrend(
                date = date,
                totalMinutes = (300..600).random(),
                entryCount = (4..12).random()
            )
        }.reversed()
    }

    /**
     * Create a HourlyPattern with default values.
     */
    fun createHourlyPattern(
        hour: Int = 9,
        totalMinutes: Int = 45
    ) = HourlyPattern(
        hour = hour,
        totalMinutes = totalMinutes
    )

    /**
     * Create a list of HourlyPattern for testing (24 hours).
     */
    fun createHourlyPatternList(): List<HourlyPattern> {
        return (0..23).map { hour ->
            val minutes = when (hour) {
                in 9..11 -> (30..60).random()
                in 14..17 -> (30..60).random()
                in 19..21 -> (15..30).random()
                else -> (0..15).random()
            }
            createHourlyPattern(hour = hour, totalMinutes = minutes)
        }
    }

    // ==================== Time Utilities ====================

    /**
     * Get today's start instant.
     */
    fun todayStart(): Instant {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        return today.atStartOfDayIn(tz)
    }

    /**
     * Get tomorrow's start instant.
     */
    fun tomorrowStart(): Instant {
        val tz = TimeZone.currentSystemDefault()
        val tomorrow = Clock.System.now().toLocalDateTime(tz).date.plus(1, DateTimeUnit.DAY)
        return tomorrow.atStartOfDayIn(tz)
    }

    /**
     * Get an instant representing a specific time today.
     */
    fun todayAt(hour: Int, minute: Int = 0): Instant {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        return today.atStartOfDayIn(tz).plus(hour.hours).plus(minute.minutes)
    }

    /**
     * Get today's date.
     */
    fun today(): LocalDate {
        return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
}
