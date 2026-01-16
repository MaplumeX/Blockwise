package com.maplume.blockwise.core.data.mapper

import com.maplume.blockwise.core.data.dao.ActivityStatistics
import com.maplume.blockwise.core.data.dao.DailyStatistics
import com.maplume.blockwise.core.data.dao.HourlyDistribution
import com.maplume.blockwise.core.data.dao.TagStatistics
import com.maplume.blockwise.core.data.dao.TotalStatistics
import com.maplume.blockwise.core.data.entity.ActivityTypeEntity
import com.maplume.blockwise.core.data.entity.GoalEntity
import com.maplume.blockwise.core.data.entity.GoalWithTag
import com.maplume.blockwise.core.data.entity.TagEntity
import com.maplume.blockwise.core.data.entity.TimeEntryEntity
import com.maplume.blockwise.core.data.entity.TimeEntryWithDetails
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.CategoryStatistics
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalInput
import com.maplume.blockwise.core.domain.model.HourlyPattern
import com.maplume.blockwise.core.domain.model.StatisticsSummary
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.model.TimeEntryInput
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

fun ActivityTypeEntity.toDomain(children: List<ActivityType> = emptyList()): ActivityType = ActivityType(
    id = id,
    name = name,
    colorHex = colorHex,
    icon = icon,
    parentId = parentId,
    displayOrder = displayOrder,
    isArchived = isArchived,
    children = children
)

fun ActivityType.toEntity(
    createdAt: Instant = Clock.System.now(),
    updatedAt: Instant = Clock.System.now()
): ActivityTypeEntity = ActivityTypeEntity(
    id = id,
    name = name,
    colorHex = colorHex,
    icon = icon,
    parentId = parentId,
    displayOrder = displayOrder,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TagEntity.toDomain(): Tag = Tag(
    id = id,
    name = name,
    colorHex = colorHex,
    isArchived = isArchived
)

fun Tag.toEntity(
    createdAt: Instant = Clock.System.now(),
    updatedAt: Instant = Clock.System.now()
): TagEntity = TagEntity(
    id = id,
    name = name,
    colorHex = colorHex,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun TimeEntryWithDetails.toDomain(): TimeEntry = TimeEntry(
    id = entry.id,
    activity = activity.toDomain(),
    startTime = entry.startTime,
    endTime = entry.endTime,
    durationMinutes = entry.durationMinutes,
    note = entry.note,
    tags = tags.map { it.toDomain() }
)

fun TimeEntryInput.toEntity(
    id: Long = 0,
    createdAt: Instant = Clock.System.now(),
    updatedAt: Instant = Clock.System.now()
): TimeEntryEntity = TimeEntryEntity(
    id = id,
    activityId = activityId,
    startTime = startTime,
    endTime = endTime,
    durationMinutes = durationMinutes,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun GoalWithTag.toDomain(): Goal = Goal(
    id = goal.id,
    tag = tag.toDomain(),
    targetMinutes = goal.targetMinutes,
    goalType = goal.goalType,
    period = goal.period,
    startDate = goal.startDate,
    endDate = goal.endDate,
    isActive = goal.isActive
)

fun GoalInput.toEntity(
    id: Long = 0,
    createdAt: Instant = Clock.System.now(),
    updatedAt: Instant = Clock.System.now()
): GoalEntity = GoalEntity(
    id = id,
    tagId = tagId,
    targetMinutes = targetMinutes,
    goalType = goalType,
    period = period,
    startDate = startDate,
    endDate = endDate,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ActivityStatistics.toDomain(): CategoryStatistics = CategoryStatistics(
    id = activityId,
    name = activityName,
    colorHex = colorHex,
    totalMinutes = totalMinutes,
    entryCount = entryCount,
    percentage = 0f
)

fun TagStatistics.toDomain(): CategoryStatistics = CategoryStatistics(
    id = tagId,
    name = tagName,
    colorHex = colorHex,
    totalMinutes = totalMinutes,
    entryCount = entryCount,
    percentage = 0f
)

fun DailyStatistics.toDomain(): DailyTrend = DailyTrend(
    date = LocalDate.fromEpochDays((dateMillis / 86400000).toInt()),
    totalMinutes = totalMinutes,
    entryCount = entryCount
)

fun HourlyDistribution.toDomain(): HourlyPattern = HourlyPattern(
    hour = hour,
    totalMinutes = totalMinutes
)

fun TotalStatistics.toDomain(): StatisticsSummary = StatisticsSummary(
    totalMinutes = totalMinutes,
    entryCount = entryCount
)

