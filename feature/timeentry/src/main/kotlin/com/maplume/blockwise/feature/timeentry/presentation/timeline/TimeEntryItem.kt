package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.core.domain.model.TimeEntry
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimeEntryItem(
    entry: TimeEntry,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: (Offset) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val axisWidth = 32.dp
    val nodeSize = 14.dp
    val lineWidth = 2.dp
    val activityColor = parseColor(entry.activity.colorHex)
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val surfaceColor = MaterialTheme.colorScheme.surface

    Box(modifier = modifier.fillMaxWidth()) {
        // Vertical Timeline Axis
        Canvas(modifier = Modifier.matchParentSize()) {
            val axisCenterX = axisWidth.toPx() / 2
            
            // Draw continuous line
            drawLine(
                color = lineColor,
                start = Offset(axisCenterX, 0f),
                end = Offset(axisCenterX, size.height),
                strokeWidth = lineWidth.toPx()
            )
            
            // Draw node (aligned with card content top area)
            // Card padding is 16.dp. Title is approx 24dp high (TitleMedium).
            // Center aligns at roughly 16 + 12 = 28dp.
            val nodeCenterY = 28.dp.toPx()
            val nodeRadius = nodeSize.toPx() / 2
            
            // Draw hollow dot (Surface fill + Activity color border)
            drawCircle(
                color = surfaceColor,
                radius = nodeRadius,
                center = Offset(axisCenterX, nodeCenterY)
            )
            drawCircle(
                color = activityColor,
                radius = nodeRadius,
                center = Offset(axisCenterX, nodeCenterY),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Card Content
        var lastTapOffset by remember { mutableStateOf(Offset.Zero) }

        Box(modifier = Modifier.padding(start = axisWidth, bottom = 8.dp)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("timeEntryItem-${entry.id}")
                    .pointerInput(Unit) {
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            lastTapOffset = down.position
                            waitForUpOrCancellation()
                        }
                    }
                    .combinedClickable(
                        onClick = { onClick(lastTapOffset) },
                        onLongClick = onLongClick
                    ),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 4.dp else 1.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (!entry.note.isNullOrBlank()) entry.note!! else entry.activity.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = entry.activity.name,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = formatTimeRange(entry),
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }

                        if (entry.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TagsRow(tags = entry.tags)
                        }
                    }

                    if (isSelectionMode) {
                        Spacer(modifier = Modifier.width(16.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "已选择",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Row of tag chips.
 */
@Composable
private fun TagsRow(
    tags: List<Tag>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tags.take(3).forEach { tag ->
            TagChip(tag = tag)
        }
        if (tags.size > 3) {
            Text(
                text = "+${tags.size - 3}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Small tag chip.
 */
@Composable
private fun TagChip(
    tag: Tag,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(parseColor(tag.colorHex).copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(parseColor(tag.colorHex))
        )
        Text(
            text = tag.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatTimeRange(entry: TimeEntry): String {
    val tz = TimeZone.currentSystemDefault()
    val startLocal = entry.startTime.toLocalDateTime(tz)
    val endLocal = entry.endTime.toLocalDateTime(tz)

    val isCrossDay = startLocal.date != endLocal.date

    val startTimeStr = String.format("%02d:%02d", startLocal.hour, startLocal.minute)
    val startStr = if (isCrossDay) {
        "${startLocal.date.monthNumber}/${startLocal.date.dayOfMonth} $startTimeStr"
    } else {
        startTimeStr
    }

    val endTimeStr = if (
        endLocal.hour == 0 &&
            endLocal.minute == 0 &&
            endLocal.date.toEpochDays() == startLocal.date.toEpochDays() + 1
    ) {
        "24:00"
    } else {
        String.format("%02d:%02d", endLocal.hour, endLocal.minute)
    }

    val endStr = if (isCrossDay) {
        "${endLocal.date.monthNumber}/${endLocal.date.dayOfMonth} $endTimeStr"
    } else {
        endTimeStr
    }

    return "$startStr - $endStr"
}

/**
 * Parse hex color string to Color.
 */
private fun parseColor(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF4CAF50)
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun TimeEntryItemPreview() {
    val now = Clock.System.now()
    BlockwiseTheme {
        TimeEntryItem(
            entry = TimeEntry(
                id = 1,
                activity = ActivityType(1, "工作", "#4CAF50", null, null, 0, false),
                startTime = now - 2.hours,
                endTime = now,
                durationMinutes = 120,
                note = "完成了项目文档",
                tags = listOf(
                    Tag(1, "重要", "#F44336", false),
                    Tag(2, "项目A", "#2196F3", false)
                )
            ),
            isSelected = false,
            isSelectionMode = false,
            onClick = { _ -> },
            onLongClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimeEntryItemSelectedPreview() {
    val now = Clock.System.now()
    BlockwiseTheme {
        TimeEntryItem(
            entry = TimeEntry(
                id = 1,
                activity = ActivityType(1, "学习", "#2196F3", null, null, 0, false),
                startTime = now - 1.hours,
                endTime = now,
                durationMinutes = 60,
                note = null,
                tags = emptyList()
            ),
            isSelected = true,
            isSelectionMode = true,
            onClick = { _ -> },
            onLongClick = {}
        )
    }
}
