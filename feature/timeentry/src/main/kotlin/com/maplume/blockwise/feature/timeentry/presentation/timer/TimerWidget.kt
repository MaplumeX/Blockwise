package com.maplume.blockwise.feature.timeentry.presentation.timer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.feature.timeentry.domain.model.TimerState
import kotlinx.datetime.Clock

/**
 * Timer widget displaying the current timer state and controls.
 */
@Composable
fun TimerWidget(
    state: TimerState,
    elapsedMillis: Long,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer display
            TimerDisplay(
                elapsedMillis = elapsedMillis,
                isRunning = state is TimerState.Running
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Activity info
            AnimatedVisibility(
                visible = state.isActive,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                ActivityInfo(state = state)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Control buttons
            TimerControls(
                state = state,
                onStart = onStart,
                onPause = onPause,
                onResume = onResume,
                onStop = onStop
            )
        }
    }
}

/**
 * Large timer display showing elapsed time.
 */
@Composable
private fun TimerDisplay(
    elapsedMillis: Long,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val alpha by animateFloatAsState(
        targetValue = if (isRunning) 1f else 0.7f,
        animationSpec = tween(500),
        label = "timerAlpha"
    )

    Text(
        text = formatDuration(elapsedMillis),
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = 56.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 2.sp
        ),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
        modifier = modifier
    )
}

/**
 * Activity information display.
 */
@Composable
private fun ActivityInfo(
    state: TimerState,
    modifier: Modifier = Modifier
) {
    val (activityName, colorHex, isPaused) = when (state) {
        is TimerState.Running -> Triple(state.runningActivityName, state.activityColorHex, false)
        is TimerState.Paused -> Triple(state.pausedActivityName, state.activityColorHex, true)
        else -> Triple("", "#4CAF50", false)
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Activity color indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(parseColor(colorHex))
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Activity name
        Text(
            text = activityName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Paused indicator
        if (isPaused) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "(已暂停)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Timer control buttons.
 */
@Composable
private fun TimerControls(
    state: TimerState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                (fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.8f))
                    .togetherWith(fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.8f))
            },
            label = "timerControls"
        ) { currentState ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (currentState) {
                    is TimerState.Idle -> {
                        // Start button
                        FilledIconButton(
                            onClick = onStart,
                            modifier = Modifier.size(64.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "开始计时",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    is TimerState.Running -> {
                        // Pause button
                        FilledIconButton(
                            onClick = onPause,
                            modifier = Modifier.size(56.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "暂停",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        // Stop button
                        FilledIconButton(
                            onClick = onStop,
                            modifier = Modifier.size(56.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "停止",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    is TimerState.Paused -> {
                        // Resume button
                        FilledIconButton(
                            onClick = onResume,
                            modifier = Modifier.size(56.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "继续",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Stop button
                        FilledIconButton(
                            onClick = onStop,
                            modifier = Modifier.size(56.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "停止",
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Format duration in milliseconds to HH:MM:SS or MM:SS format.
 */
private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
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
private fun TimerWidgetIdlePreview() {
    BlockwiseTheme {
        TimerWidget(
            state = TimerState.Idle,
            elapsedMillis = 0,
            onStart = {},
            onPause = {},
            onResume = {},
            onStop = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerWidgetRunningPreview() {
    BlockwiseTheme {
        TimerWidget(
            state = TimerState.Running(
                runningActivityId = 1,
                runningActivityName = "工作",
                activityColorHex = "#4CAF50",
                startTime = Clock.System.now()
            ),
            elapsedMillis = 3661000, // 1:01:01
            onStart = {},
            onPause = {},
            onResume = {},
            onStop = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimerWidgetPausedPreview() {
    BlockwiseTheme {
        TimerWidget(
            state = TimerState.Paused(
                pausedActivityId = 1,
                pausedActivityName = "学习",
                activityColorHex = "#2196F3",
                startTime = Clock.System.now(),
                elapsedMillis = 1800000 // 30:00
            ),
            elapsedMillis = 1800000,
            onStart = {},
            onPause = {},
            onResume = {},
            onStop = {}
        )
    }
}
