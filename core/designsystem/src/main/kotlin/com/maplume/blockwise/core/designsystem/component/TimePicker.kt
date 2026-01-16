package com.maplume.blockwise.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.designsystem.theme.CornerRadius
import com.maplume.blockwise.core.designsystem.theme.Spacing
import kotlinx.datetime.LocalTime

/**
 * 时间选择器颜色配置 - 现代极简风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun timePickerColors(): TimePickerColors {
    return TimePickerDefaults.colors(
        clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
        clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
        selectorColor = MaterialTheme.colorScheme.primary,
        containerColor = MaterialTheme.colorScheme.surface,
        periodSelectorBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
        periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
        periodSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * 时间选择器对话框 - 现代极简风格
 * 大圆角 + 自定义按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseTimePickerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    initialTime: LocalTime? = null,
    is24Hour: Boolean = true
) {
    if (visible) {
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime?.hour ?: 12,
            initialMinute = initialTime?.minute ?: 0,
            is24Hour = is24Hour
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.small),
                    horizontalArrangement = Arrangement.End
                ) {
                    BlockwiseTextButton(
                        text = "取消",
                        onClick = onDismiss
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    BlockwisePrimaryButton(
                        text = "确认",
                        onClick = {
                            val time = LocalTime(timePickerState.hour, timePickerState.minute)
                            onTimeSelected(time)
                            onDismiss()
                        }
                    )
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = timePickerColors()
                )
            },
            shape = RoundedCornerShape(CornerRadius.extraLarge),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        )
    }
}

/**
 * 时长选择器对话框 - 现代极简风格
 * 用于选择小时和分钟
 */
@Composable
fun BlockwiseDurationPickerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onDurationSelected: (hours: Int, minutes: Int) -> Unit,
    initialHours: Int = 0,
    initialMinutes: Int = 0
) {
    if (visible) {
        var hours by remember { mutableIntStateOf(initialHours) }
        var minutes by remember { mutableIntStateOf(initialMinutes) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "选择时长",
                    style = MaterialTheme.typography.titleLarge
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.small),
                    horizontalArrangement = Arrangement.End
                ) {
                    BlockwiseTextButton(
                        text = "取消",
                        onClick = onDismiss
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    BlockwisePrimaryButton(
                        text = "确认",
                        onClick = {
                            onDurationSelected(hours, minutes)
                            onDismiss()
                        }
                    )
                }
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$hours",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "时",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Text(
                        text = " : ",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$minutes",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "分",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(CornerRadius.extraLarge),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        )
    }
}
