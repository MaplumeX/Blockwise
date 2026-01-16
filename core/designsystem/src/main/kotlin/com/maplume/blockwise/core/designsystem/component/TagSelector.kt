package com.maplume.blockwise.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.designsystem.theme.ActivityColors
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.designsystem.theme.Spacing

/**
 * Tag item data class
 */
data class TagItem(
    val id: Long,
    val name: String,
    val color: Color
)

/**
 * Tag selector with FlowRow layout
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
 * Single tag chip component
 */
@Composable
fun BlockwiseTagChip(
    tag: TagItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(tag.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(tag.name)
            }
        }
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
