package com.maplume.blockwise.feature.timeentry.presentation.timeblock

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Zoom level configuration for time block view.
 */
data class ZoomConfig(
    val minHourHeight: Dp = 30.dp,
    val maxHourHeight: Dp = 120.dp,
    val defaultHourHeight: Dp = 60.dp
)

/**
 * State for zoom gesture handling.
 */
data class ZoomState(
    val scale: Float = 1f,
    val hourHeight: Dp = 60.dp
) {
    fun withScale(newScale: Float, config: ZoomConfig): ZoomState {
        val clampedScale = newScale.coerceIn(
            config.minHourHeight.value / config.defaultHourHeight.value,
            config.maxHourHeight.value / config.defaultHourHeight.value
        )
        return copy(
            scale = clampedScale,
            hourHeight = (config.defaultHourHeight.value * clampedScale).dp
        )
    }
}

/**
 * Zoomable container for time block views.
 * Supports pinch-to-zoom gesture to adjust the hour height (view density).
 */
@Composable
fun ZoomableTimeBlockContainer(
    zoomState: ZoomState,
    onZoomChange: (ZoomState) -> Unit,
    modifier: Modifier = Modifier,
    config: ZoomConfig = ZoomConfig(),
    content: @Composable (hourHeight: Dp) -> Unit
) {
    var currentScale by remember { mutableFloatStateOf(zoomState.scale) }

    // Animate scale changes for smooth zooming
    val animatedScale by animateFloatAsState(
        targetValue = currentScale,
        animationSpec = spring(
            dampingRatio = 0.8f,
            stiffness = 300f
        ),
        label = "ZoomScale"
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    val newScale = currentScale * zoom
                    val clampedScale = newScale.coerceIn(
                        config.minHourHeight.value / config.defaultHourHeight.value,
                        config.maxHourHeight.value / config.defaultHourHeight.value
                    )

                    if (clampedScale != currentScale) {
                        currentScale = clampedScale
                        onZoomChange(zoomState.withScale(clampedScale, config))
                    }
                }
            }
    ) {
        val currentHourHeight = (config.defaultHourHeight.value * animatedScale).dp
        content(currentHourHeight)
    }
}

/**
 * Preset zoom levels for quick selection.
 */
enum class ZoomLevel(val scale: Float, val label: String) {
    COMPACT(0.5f, "紧凑"),
    NORMAL(1.0f, "标准"),
    COMFORTABLE(1.5f, "舒适"),
    DETAILED(2.0f, "详细");

    fun toHourHeight(defaultHeight: Dp = 60.dp): Dp {
        return (defaultHeight.value * scale).dp
    }

    companion object {
        fun fromScale(scale: Float): ZoomLevel {
            return entries.minByOrNull { kotlin.math.abs(it.scale - scale) } ?: NORMAL
        }
    }
}

/**
 * Helper composable for zoom level indicator.
 */
@Composable
fun rememberZoomState(
    initialLevel: ZoomLevel = ZoomLevel.NORMAL,
    config: ZoomConfig = ZoomConfig()
): Pair<ZoomState, (ZoomState) -> Unit> {
    var zoomState by remember {
        mutableStateOf(
            ZoomState(
                scale = initialLevel.scale,
                hourHeight = initialLevel.toHourHeight(config.defaultHourHeight)
            )
        )
    }

    return zoomState to { newState: ZoomState -> zoomState = newState }
}
