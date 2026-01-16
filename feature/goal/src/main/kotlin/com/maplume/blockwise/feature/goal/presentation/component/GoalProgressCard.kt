package com.maplume.blockwise.feature.goal.presentation.component

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.designsystem.component.BlockwiseCircularProgress
import com.maplume.blockwise.core.designsystem.component.BlockwiseClickableCard
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.designsystem.theme.Spacing
import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalPeriod
import com.maplume.blockwise.core.domain.model.GoalProgress
import com.maplume.blockwise.core.domain.model.GoalType
import com.maplume.blockwise.core.domain.model.Tag

/**
 * Goal progress card component.
 * Displays goal information with circular progress indicator.
 */
@Composable
fun GoalProgressCard(
    progress: GoalProgress,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val tagColor = parseColor(progress.goal.tag.colorHex)

    BlockwiseClickableCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress circle
            BlockwiseCircularProgress(
                progress = progress.progress,
                size = 64.dp,
                strokeWidth = 6.dp,
                color = tagColor
            ) {
                Text(
                    text = "${progress.progressPercentage}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(Spacing.medium))

            // Goal info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tag color indicator
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(tagColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = progress.goal.tag.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${progress.goal.goalTypeLabel}${progress.goal.formattedTarget}/${progress.goal.periodLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "已完成 ${formatMinutes(progress.currentMinutes)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Status badge and menu
            Column(
                horizontalAlignment = Alignment.End
            ) {
                GoalStatusBadge(progress = progress)

                Spacer(modifier = Modifier.height(Spacing.small))

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MoreVert,
                            contentDescription = "更多选项",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑") },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("归档") },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Archived goal card with restore option.
 */
@Composable
fun ArchivedGoalCard(
    goal: Goal,
    onClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    val tagColor = parseColor(goal.tag.colorHex)

    BlockwiseClickableCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Archive icon
            Icon(
                imageVector = Icons.Outlined.Archive,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.width(Spacing.medium))

            // Goal info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(tagColor.copy(alpha = 0.5f), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = goal.tag.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${goal.goalTypeLabel}${goal.formattedTarget}/${goal.periodLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            // Menu
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "更多选项",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("恢复") },
                        onClick = {
                            showMenu = false
                            onRestoreClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("永久删除", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

/**
 * Goal status badge component.
 */
@Composable
private fun GoalStatusBadge(progress: GoalProgress) {
    val (text, backgroundColor, textColor) = when {
        progress.isCompleted -> Triple(
            "已完成",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary
        )
        progress.progress >= 0.8f -> Triple(
            "即将完成",
            Color(0xFFFFF3E0),
            Color(0xFFE65100)
        )
        progress.progress >= 0.5f -> Triple(
            "进行中",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.secondary
        )
        else -> Triple(
            "进行中",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

/**
 * Format minutes to human-readable string.
 */
fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return when {
        hours > 0 && mins > 0 -> "${hours}小时${mins}分钟"
        hours > 0 -> "${hours}小时"
        else -> "${mins}分钟"
    }
}

/**
 * Parse color hex string to Color.
 */
fun parseColor(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF135BEC) // Default blue
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun GoalProgressCardPreview() {
    val sampleGoal = Goal(
        id = 1,
        tag = Tag(id = 1, name = "学习", colorHex = "#4CAF50"),
        targetMinutes = 1200,
        goalType = GoalType.MIN,
        period = GoalPeriod.WEEKLY
    )
    val sampleProgress = GoalProgress(
        goal = sampleGoal,
        currentMinutes = 900,
        targetMinutes = 1200
    )

    BlockwiseTheme {
        Column(
            modifier = Modifier.padding(Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            GoalProgressCard(
                progress = sampleProgress,
                onClick = {},
                onEditClick = {},
                onDeleteClick = {}
            )

            GoalProgressCard(
                progress = sampleProgress.copy(currentMinutes = 1200),
                onClick = {},
                onEditClick = {},
                onDeleteClick = {}
            )

            GoalProgressCard(
                progress = sampleProgress.copy(currentMinutes = 300),
                onClick = {},
                onEditClick = {},
                onDeleteClick = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ArchivedGoalCardPreview() {
    val sampleGoal = Goal(
        id = 1,
        tag = Tag(id = 1, name = "运动", colorHex = "#FF5722"),
        targetMinutes = 60,
        goalType = GoalType.MIN,
        period = GoalPeriod.DAILY,
        isActive = false
    )

    BlockwiseTheme {
        ArchivedGoalCard(
            goal = sampleGoal,
            onClick = {},
            onRestoreClick = {},
            onDeleteClick = {},
            modifier = Modifier.padding(Spacing.medium)
        )
    }
}
