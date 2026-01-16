package com.maplume.blockwise.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.designsystem.theme.CornerRadius
import com.maplume.blockwise.core.designsystem.theme.Spacing

/**
 * 徽章组件 - 现代极简风格
 * 用于显示数字或状态标识
 */
@Composable
fun BlockwiseBadge(
    count: Int,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.error,
    contentColor: Color = MaterialTheme.colorScheme.onError,
    maxCount: Int = 99
) {
    val displayText = if (count > maxCount) "$maxCount+" else count.toString()

    Box(
        modifier = modifier
            .background(backgroundColor, CircleShape)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = displayText,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

/**
 * 圆点徽章 - 现代极简风格
 * 用于显示未读状态
 */
@Composable
fun BlockwiseDotBadge(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.error
) {
    Box(
        modifier = modifier
            .size(8.dp)
            .background(color, CircleShape)
    )
}

/**
 * 文本徽章 - 现代极简风格
 * 用于显示状态标签
 */
@Composable
fun BlockwiseTextBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(CornerRadius.small))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun BlockwiseBadgePreview() {
    BlockwiseTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BlockwiseBadge(count = 5)
            BlockwiseBadge(count = 99)
            BlockwiseBadge(count = 100)
            BlockwiseDotBadge()
            BlockwiseTextBadge(text = "新")
        }
    }
}
