package com.maplume.blockwise.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.designsystem.theme.ActivityColors
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.designsystem.theme.CornerRadius
import com.maplume.blockwise.core.designsystem.theme.Spacing

/**
 * 标签项数据类
 */
data class TagItem(
    val id: Long,
    val name: String,
    val color: Color
)

/**
 * 标签选择器 - 现代极简风格
 * FlowRow布局 + 细腻边框
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlockwiseTagSelector(
    tags: List<TagItem>,
    selectedTagIds: Set<Long>,
    onTagSelected: (Long) -> Unit,
    onTagDeselected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    multiSelect: Boolean = true
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        tags.forEach { tag ->
            val isSelected = selectedTagIds.contains(tag.id)
            BlockwiseTagChip(
                tag = tag,
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        onTagDeselected(tag.id)
                    } else {
                        if (!multiSelect) {
                            selectedTagIds.forEach { onTagDeselected(it) }
                        }
                        onTagSelected(tag.id)
                    }
                }
            )
        }
    }
}

/**
 * 单个标签芯片组件 - 现代极简风格
 * 缩放动画 + 细腻边框 + 大圆角
 */
@Composable
fun BlockwiseTagChip(
    tag: TagItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(),
        label = "chipScale"
    )

    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier.scale(scale),
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(tag.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = tag.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        shape = RoundedCornerShape(CornerRadius.medium),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.primary
        ),
        interactionSource = interactionSource
    )
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun BlockwiseTagSelectorPreview() {
    val sampleTags = listOf(
        TagItem(1, "工作", ActivityColors[0]),
        TagItem(2, "学习", ActivityColors[1]),
        TagItem(3, "运动", ActivityColors[2]),
        TagItem(4, "休息", ActivityColors[3])
    )
    BlockwiseTheme {
        BlockwiseTagSelector(
            tags = sampleTags,
            selectedTagIds = setOf(1L, 3L),
            onTagSelected = {},
            onTagDeselected = {}
        )
    }
}
