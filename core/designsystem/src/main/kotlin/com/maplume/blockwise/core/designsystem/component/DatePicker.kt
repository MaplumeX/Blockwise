package com.maplume.blockwise.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.designsystem.theme.CornerRadius
import com.maplume.blockwise.core.designsystem.theme.Spacing
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/**
 * 日期选择器颜色配置 - 现代极简风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun datePickerColors(): DatePickerColors {
    return DatePickerDefaults.colors(
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        headlineContentColor = MaterialTheme.colorScheme.onSurface,
        weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        currentYearContentColor = MaterialTheme.colorScheme.primary,
        selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
        selectedYearContainerColor = MaterialTheme.colorScheme.primary,
        dayContentColor = MaterialTheme.colorScheme.onSurface,
        selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
        todayContentColor = MaterialTheme.colorScheme.primary,
        todayDateBorderColor = MaterialTheme.colorScheme.primary
    )
}

/**
 * 单日期选择器对话框 - 现代极简风格
 * 大圆角 + 自定义按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseDatePickerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate? = null
) {
    if (visible) {
        val initialMillis = initialDate?.atStartOfDayIn(TimeZone.currentSystemDefault())
            ?.toEpochMilliseconds()

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialMillis
        )

        DatePickerDialog(
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
                            datePickerState.selectedDateMillis?.let { millis ->
                                val instant = Instant.fromEpochMilliseconds(millis)
                                val date = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                                onDateSelected(date)
                            }
                            onDismiss()
                        }
                    )
                }
            },
            shape = RoundedCornerShape(CornerRadius.extraLarge),
            tonalElevation = 0.dp
        ) {
            DatePicker(
                state = datePickerState,
                colors = datePickerColors()
            )
        }
    }
}

/**
 * 日期范围选择器对话框 - 现代极简风格
 * 大圆角 + 自定义按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseDateRangePickerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onRangeSelected: (LocalDate, LocalDate) -> Unit
) {
    if (visible) {
        val dateRangePickerState = rememberDateRangePickerState()

        DatePickerDialog(
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
                            val startMillis = dateRangePickerState.selectedStartDateMillis
                            val endMillis = dateRangePickerState.selectedEndDateMillis
                            if (startMillis != null && endMillis != null) {
                                val tz = TimeZone.currentSystemDefault()
                                val startDate = Instant.fromEpochMilliseconds(startMillis)
                                    .toLocalDateTime(tz).date
                                val endDate = Instant.fromEpochMilliseconds(endMillis)
                                    .toLocalDateTime(tz).date
                                onRangeSelected(startDate, endDate)
                            }
                            onDismiss()
                        }
                    )
                }
            },
            shape = RoundedCornerShape(CornerRadius.extraLarge),
            tonalElevation = 0.dp
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                colors = datePickerColors()
            )
        }
    }
}
