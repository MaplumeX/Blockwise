package com.maplume.blockwise.feature.timeentry.presentation.timeblock

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.domain.model.TimeEntry
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

/**
 * Drag mode for time block adjustment.
 */
enum class DragMode {
    MOVE,       // Move entire block (change start time)
    RESIZE_TOP, // Resize from top (change start time)
    RESIZE_BOTTOM // Resize from bottom (change end time)
}

/**
 * State for draggable time block.
 */
data class DragState(
    val isDragging: Boolean = false,
    val dragMode: DragMode = DragMode.MOVE,
    val offsetY: Float = 0f,
    val previewStartMinutes: Int = 0,
    val previewEndMinutes: Int = 0
)

/**
 * Draggable time block wrapper that allows adjusting time by dragging.
 */
@Composable
fun DraggableTimeBlock(
    entry: TimeEntry,
    hourHeight: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDragEnd: (newStartMinutes: Int, newEndMinutes: Int) -> Unit,
    modifier: Modifier = Modifier,
    showDetails: Boolean = true
) {
    var dragState by remember { mutableStateOf(DragState()) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val startMinutes = remember(entry) {
        getMinutesFromInstant(entry.startTime)
    }
    val endMinutes = remember(entry) {
        getMinutesFromInstant(entry.endTime)
    }
    val durationMinutes = endMinutes - startMinutes

    // Calculate pixels per minute
    val pixelsPerMinute = hourHeight.value / 60f

    Box(
        modifier = modifier
            .offset { IntOffset(0, offsetY.roundToInt()) }
            .pointerInput(entry.id) {
                detectVerticalDragGestures(
                    onDragStart = {
                        dragState = dragState.copy(
                            isDragging = true,
                            dragMode = DragMode.MOVE,
                            previewStartMinutes = startMinutes,
                            previewEndMinutes = endMinutes
                        )
                    },
                    onDragEnd = {
                        if (dragState.isDragging) {
                            // Calculate new times based on drag offset
                            val minutesDelta = (offsetY / pixelsPerMinute).roundToInt()
                            val snappedDelta = snapToGrid(minutesDelta, 5) // Snap to 5-minute intervals

                            val newStartMinutes = (startMinutes + snappedDelta).coerceIn(0, 24 * 60 - durationMinutes)
                            val newEndMinutes = newStartMinutes + durationMinutes

                            if (newStartMinutes != startMinutes) {
                                onDragEnd(newStartMinutes, newEndMinutes)
                            }

                            offsetY = 0f
                            dragState = DragState()
                        }
                    },
                    onDragCancel = {
                        offsetY = 0f
                        dragState = DragState()
                    },
                    onVerticalDrag = { _, dragAmount ->
                        offsetY += dragAmount

                        // Update preview
                        val minutesDelta = (offsetY / pixelsPerMinute).roundToInt()
                        val snappedDelta = snapToGrid(minutesDelta, 5)
                        val newStartMinutes = (startMinutes + snappedDelta).coerceIn(0, 24 * 60 - durationMinutes)

                        dragState = dragState.copy(
                            previewStartMinutes = newStartMinutes,
                            previewEndMinutes = newStartMinutes + durationMinutes
                        )
                    }
                )
            }
    ) {
        TimeBlock(
            entry = entry,
            onClick = onClick,
            onLongClick = onLongClick,
            showDetails = showDetails,
            modifier = Modifier
        )
    }
}

/**
 * Resizable time block that allows adjusting duration by dragging edges.
 */
@Composable
fun ResizableTimeBlock(
    entry: TimeEntry,
    hourHeight: Dp,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onResizeEnd: (newStartMinutes: Int, newEndMinutes: Int) -> Unit,
    modifier: Modifier = Modifier,
    showDetails: Boolean = true,
    minDurationMinutes: Int = 5
) {
    var topOffsetY by remember { mutableFloatStateOf(0f) }
    var bottomOffsetY by remember { mutableFloatStateOf(0f) }
    var isResizing by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableStateOf<DragMode?>(null) }

    val startMinutes = remember(entry) {
        getMinutesFromInstant(entry.startTime)
    }
    val endMinutes = remember(entry) {
        getMinutesFromInstant(entry.endTime)
    }

    val pixelsPerMinute = hourHeight.value / 60f

    Box(modifier = modifier) {
        // Main block
        TimeBlock(
            entry = entry,
            onClick = onClick,
            onLongClick = onLongClick,
            showDetails = showDetails,
            modifier = Modifier
        )

        // Top resize handle (invisible, but captures drag)
        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(entry.id) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            // Determine if dragging from top or bottom edge
                            val blockHeight = size.height
                            val edgeThreshold = 20.dp.toPx()

                            resizeMode = when {
                                offset.y < edgeThreshold -> DragMode.RESIZE_TOP
                                offset.y > blockHeight - edgeThreshold -> DragMode.RESIZE_BOTTOM
                                else -> DragMode.MOVE
                            }
                            isResizing = true
                        },
                        onDragEnd = {
                            if (isResizing && resizeMode != null) {
                                val minutesDelta = when (resizeMode) {
                                    DragMode.RESIZE_TOP -> (topOffsetY / pixelsPerMinute).roundToInt()
                                    DragMode.RESIZE_BOTTOM -> (bottomOffsetY / pixelsPerMinute).roundToInt()
                                    else -> 0
                                }
                                val snappedDelta = snapToGrid(minutesDelta, 5)

                                val (newStart, newEnd) = when (resizeMode) {
                                    DragMode.RESIZE_TOP -> {
                                        val newStartMinutes = (startMinutes + snappedDelta)
                                            .coerceIn(0, endMinutes - minDurationMinutes)
                                        newStartMinutes to endMinutes
                                    }
                                    DragMode.RESIZE_BOTTOM -> {
                                        val newEndMinutes = (endMinutes + snappedDelta)
                                            .coerceIn(startMinutes + minDurationMinutes, 24 * 60)
                                        startMinutes to newEndMinutes
                                    }
                                    else -> startMinutes to endMinutes
                                }

                                if (newStart != startMinutes || newEnd != endMinutes) {
                                    onResizeEnd(newStart, newEnd)
                                }
                            }

                            topOffsetY = 0f
                            bottomOffsetY = 0f
                            isResizing = false
                            resizeMode = null
                        },
                        onDragCancel = {
                            topOffsetY = 0f
                            bottomOffsetY = 0f
                            isResizing = false
                            resizeMode = null
                        },
                        onDrag = { _, dragAmount ->
                            when (resizeMode) {
                                DragMode.RESIZE_TOP -> topOffsetY += dragAmount.y
                                DragMode.RESIZE_BOTTOM -> bottomOffsetY += dragAmount.y
                                else -> {}
                            }
                        }
                    )
                }
        )
    }
}

/**
 * Get minutes of day from an Instant.
 */
private fun getMinutesFromInstant(instant: Instant): Int {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return local.hour * 60 + local.minute
}

/**
 * Snap value to nearest grid interval.
 */
private fun snapToGrid(value: Int, gridSize: Int): Int {
    return ((value + gridSize / 2) / gridSize) * gridSize
}

/**
 * Convert minutes of day to LocalTime.
 */
fun minutesToLocalTime(minutes: Int): LocalTime {
    val hours = (minutes / 60).coerceIn(0, 23)
    val mins = (minutes % 60).coerceIn(0, 59)
    return LocalTime(hours, mins)
}
