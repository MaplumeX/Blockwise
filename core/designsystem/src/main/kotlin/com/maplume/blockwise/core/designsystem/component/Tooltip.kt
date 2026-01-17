package com.maplume.blockwise.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import kotlinx.coroutines.delay

/**
 * Anchor position for the tooltip arrow.
 */
enum class TooltipAnchor {
    Top,     // Arrow points up, tooltip below anchor
    Bottom,  // Arrow points down, tooltip above anchor
    Start,   // Arrow points left, tooltip to the right
    End      // Arrow points right, tooltip to the left
}

/**
 * A tooltip component that displays a floating hint message with an arrow.
 *
 * @param text The main text content of the tooltip.
 * @param visible Whether the tooltip is visible.
 * @param onDismiss Callback when the tooltip is dismissed.
 * @param modifier Modifier to apply to the container.
 * @param title Optional title for the tooltip.
 * @param anchor The position of the arrow relative to the tooltip.
 * @param arrowOffset Offset for the arrow position.
 * @param backgroundColor Background color of the tooltip.
 * @param contentColor Text color of the tooltip.
 * @param showCloseButton Whether to show a close button.
 * @param autoDismissDelay Auto-dismiss delay in milliseconds. 0 means no auto-dismiss.
 */
@Composable
fun BlockwiseTooltip(
    text: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    anchor: TooltipAnchor = TooltipAnchor.Top,
    arrowOffset: Dp = 0.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    showCloseButton: Boolean = true,
    autoDismissDelay: Long = 0L
) {
    val visibleState = remember { MutableTransitionState(false) }
    visibleState.targetState = visible

    // Auto-dismiss logic
    LaunchedEffect(visible) {
        if (visible && autoDismissDelay > 0) {
            delay(autoDismissDelay)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(animationSpec = tween(200)) +
                scaleIn(
                    initialScale = 0.8f,
                    transformOrigin = getTransformOrigin(anchor),
                    animationSpec = tween(200)
                ),
        exit = fadeOut(animationSpec = tween(150)) +
               scaleOut(
                   targetScale = 0.8f,
                   transformOrigin = getTransformOrigin(anchor),
                   animationSpec = tween(150)
               ),
        modifier = modifier
    ) {
        TooltipContent(
            text = text,
            title = title,
            anchor = anchor,
            arrowOffset = arrowOffset,
            backgroundColor = backgroundColor,
            contentColor = contentColor,
            showCloseButton = showCloseButton,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun TooltipContent(
    text: String,
    title: String?,
    anchor: TooltipAnchor,
    arrowOffset: Dp,
    backgroundColor: Color,
    contentColor: Color,
    showCloseButton: Boolean,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = when (anchor) {
            TooltipAnchor.Top -> Alignment.CenterHorizontally
            TooltipAnchor.Bottom -> Alignment.CenterHorizontally
            TooltipAnchor.Start -> Alignment.Start
            TooltipAnchor.End -> Alignment.End
        }
    ) {
        // Arrow at top
        if (anchor == TooltipAnchor.Top) {
            TooltipArrow(
                backgroundColor = backgroundColor,
                rotation = 0f,
                modifier = Modifier.offset(x = arrowOffset)
            )
        }

        Row(
            verticalAlignment = Alignment.Top
        ) {
            // Arrow at start
            if (anchor == TooltipAnchor.Start) {
                TooltipArrow(
                    backgroundColor = backgroundColor,
                    rotation = -90f,
                    modifier = Modifier.offset(y = arrowOffset + 8.dp)
                )
            }

            // Main content
            Surface(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .widthIn(max = 280.dp)
                        .padding(
                            start = 12.dp,
                            top = 8.dp,
                            end = if (showCloseButton) 4.dp else 12.dp,
                            bottom = 8.dp
                        ),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (title != null) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                color = contentColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.9f)
                        )
                    }

                    if (showCloseButton) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭提示",
                                tint = contentColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Arrow at end
            if (anchor == TooltipAnchor.End) {
                TooltipArrow(
                    backgroundColor = backgroundColor,
                    rotation = 90f,
                    modifier = Modifier.offset(y = arrowOffset + 8.dp)
                )
            }
        }

        // Arrow at bottom
        if (anchor == TooltipAnchor.Bottom) {
            TooltipArrow(
                backgroundColor = backgroundColor,
                rotation = 180f,
                modifier = Modifier.offset(x = arrowOffset)
            )
        }
    }
}

@Composable
private fun TooltipArrow(
    backgroundColor: Color,
    rotation: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val arrowWidth = 12.dp
    val arrowHeight = 8.dp

    val arrowShape = remember {
        GenericShape { size, _ ->
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
    }

    Box(
        modifier = modifier
            .size(arrowWidth, arrowHeight)
            .rotate(rotation)
            .clip(arrowShape)
            .background(backgroundColor)
    )
}

private fun getTransformOrigin(anchor: TooltipAnchor): TransformOrigin {
    return when (anchor) {
        TooltipAnchor.Top -> TransformOrigin(0.5f, 0f)
        TooltipAnchor.Bottom -> TransformOrigin(0.5f, 1f)
        TooltipAnchor.Start -> TransformOrigin(0f, 0.5f)
        TooltipAnchor.End -> TransformOrigin(1f, 0.5f)
    }
}

/**
 * A composable that wraps content and shows a tooltip on first use.
 *
 * @param tooltipId Unique identifier for this tooltip (used to track if shown before).
 * @param text Tooltip text content.
 * @param shownTooltips Set of tooltip IDs that have been shown.
 * @param onTooltipShown Callback when tooltip is shown (should persist the ID).
 * @param modifier Modifier for the container.
 * @param title Optional tooltip title.
 * @param anchor Arrow anchor position.
 * @param enabled Whether the tooltip feature is enabled.
 * @param content The content to wrap.
 */
@Composable
fun TooltipAnchorBox(
    tooltipId: String,
    text: String,
    shownTooltips: Set<String>,
    onTooltipShown: (String) -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    anchor: TooltipAnchor = TooltipAnchor.Bottom,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var showTooltip by remember { mutableStateOf(false) }
    val shouldShowTooltip = enabled && tooltipId !in shownTooltips

    LaunchedEffect(shouldShowTooltip) {
        if (shouldShowTooltip) {
            // Small delay to allow the UI to settle
            delay(500)
            showTooltip = true
        }
    }

    Box(modifier = modifier) {
        content()

        // Position tooltip based on anchor
        val tooltipModifier = when (anchor) {
            TooltipAnchor.Top -> Modifier.align(Alignment.TopCenter)
            TooltipAnchor.Bottom -> Modifier.align(Alignment.BottomCenter)
            TooltipAnchor.Start -> Modifier.align(Alignment.CenterStart)
            TooltipAnchor.End -> Modifier.align(Alignment.CenterEnd)
        }

        BlockwiseTooltip(
            text = text,
            title = title,
            visible = showTooltip,
            onDismiss = {
                showTooltip = false
                onTooltipShown(tooltipId)
            },
            anchor = anchor,
            modifier = tooltipModifier.offset(
                y = when (anchor) {
                    TooltipAnchor.Top -> (-8).dp
                    TooltipAnchor.Bottom -> 8.dp
                    else -> 0.dp
                },
                x = when (anchor) {
                    TooltipAnchor.Start -> (-8).dp
                    TooltipAnchor.End -> 8.dp
                    else -> 0.dp
                }
            )
        )
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun TooltipPreviewTop() {
    BlockwiseTheme {
        Box(
            modifier = Modifier.padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            BlockwiseTooltip(
                title = "计时提示",
                text = "点击开始按钮开始计时，再次点击结束",
                visible = true,
                onDismiss = {},
                anchor = TooltipAnchor.Top
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TooltipPreviewBottom() {
    BlockwiseTheme {
        Box(
            modifier = Modifier.padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            BlockwiseTooltip(
                text = "这是一个简单的提示信息",
                visible = true,
                onDismiss = {},
                anchor = TooltipAnchor.Bottom,
                showCloseButton = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TooltipPreviewWithTitle() {
    BlockwiseTheme {
        Box(
            modifier = Modifier.padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            BlockwiseTooltip(
                title = "新功能",
                text = "你可以通过长按时间条目来选择多个条目进行批量操作",
                visible = true,
                onDismiss = {},
                anchor = TooltipAnchor.Top
            )
        }
    }
}
