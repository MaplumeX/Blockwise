package com.maplume.blockwise.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime

/**
 * Time picker dialog
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
                TextButton(onClick = {
                    val time = LocalTime(timePickerState.hour, timePickerState.minute)
                    onTimeSelected(time)
                    onDismiss()
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

/**
 * Duration picker dialog for selecting hours and minutes
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
            title = { Text("选择时长") },
            confirmButton = {
                TextButton(onClick = {
                    onDurationSelected(hours, minutes)
                    onDismiss()
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$hours")
                        Text("时", modifier = Modifier.padding(top = 4.dp))
                    }
                    Text(" : ", modifier = Modifier.padding(horizontal = 16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$minutes")
                        Text("分", modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        )
    }
}
