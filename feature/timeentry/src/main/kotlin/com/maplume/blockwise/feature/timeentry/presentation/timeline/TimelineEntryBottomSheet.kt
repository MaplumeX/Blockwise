package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.Tag
import kotlinx.datetime.LocalTime
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import androidx.compose.runtime.snapshotFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimelineEntryBottomSheet(
    draft: TimeEntryDraft,
    activityTypes: List<ActivityType>,
    availableTags: List<Tag>,
    canMergeUp: Boolean,
    canMergeDown: Boolean,
    isCreateMode: Boolean = false,
    onDismiss: () -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onActivitySelect: (Long) -> Unit,
    onTagToggle: (Long) -> Unit,
    onNoteChange: (String) -> Unit,
    onMergeUp: () -> Unit,
    onMergeDown: () -> Unit,
    onDelete: () -> Unit,
    onSave: () -> Unit,
    onSplit: () -> Unit
) {
    var showActivitySelector by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    val halfHeight = (LocalConfiguration.current.screenHeightDp.dp * 0.5f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = halfHeight, max = halfHeight)
                .testTag(if (isCreateMode) "timelineCreateEntrySheet" else "timelineEntrySheet"),
            tonalElevation = 0.dp
        ) {
            if (showActivitySelector) {
                ActivitySelectorDialog(
                    activityTypes = activityTypes,
                    selectedActivityId = draft.activityId,
                    onSelect = { id ->
                        onActivitySelect(id)
                        showActivitySelector = false
                    },
                    onDismiss = { showActivitySelector = false }
                )
            }

            Column(modifier = Modifier.fillMaxSize()) {
                TopIconRow(
                    canMergeUp = canMergeUp,
                    canMergeDown = canMergeDown,
                    isCreateMode = isCreateMode,
                    onMergeUp = onMergeUp,
                    onMergeDown = onMergeDown,
                    onDelete = onDelete,
                    onClose = onDismiss
                )

                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TimeEditorSection(
                        startTime = draft.startTime,
                        endTime = draft.endTime,
                        durationSeconds = draft.durationSeconds,
                        isCreateMode = isCreateMode,
                        onStartTimeChange = onStartTimeChange,
                        onEndTimeChange = onEndTimeChange
                    )

                    ActivityQuickSelectSection(
                        activityTypes = activityTypes,
                        selectedActivityId = draft.activityId,
                        onSelect = onActivitySelect,
                        onMore = { showActivitySelector = true }
                    )

                    TagsSection(
                        availableTags = availableTags,
                        selectedTagIds = draft.tagIds,
                        onToggle = onTagToggle
                    )

                    NoteSection(
                        note = draft.note,
                        onNoteChange = onNoteChange
                    )
                }

                HorizontalDivider()

                BottomActionRow(
                    isCreateMode = isCreateMode,
                    isValid = if (isCreateMode) draft.endTime > draft.startTime else true,
                    onSave = onSave,
                    onSplit = onSplit
                )
            }
        }
    }
}

@Composable
private fun TopIconRow(
    canMergeUp: Boolean,
    canMergeDown: Boolean,
    isCreateMode: Boolean,
    onMergeUp: () -> Unit,
    onMergeDown: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isCreateMode) {
                IconButton(onClick = onMergeUp, enabled = canMergeUp) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "合并上一条")
                }
                IconButton(onClick = onMergeDown, enabled = canMergeDown) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "合并下一条")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "关闭")
        }
    }
}

@Composable
private fun TimeEditorSection(
    startTime: LocalTime,
    endTime: LocalTime,
    durationSeconds: Int,
    isCreateMode: Boolean,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("时间", style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeWheel(
                label = "开始",
                selected = startTime,
                onSelected = onStartTimeChange,
                testTagPrefix = "start",
                modifier = Modifier.weight(1f)
            )
            TimeWheel(
                label = "结束",
                selected = endTime,
                onSelected = onEndTimeChange,
                testTagPrefix = "end",
                modifier = Modifier.weight(1f)
            )
        }

        if (isCreateMode && endTime <= startTime) {
            Text(
                text = "结束时间需晚于起始时间",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Text(
                text = "时长：${formatDurationSeconds(durationSeconds)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimeWheel(
    label: String,
    selected: LocalTime,
    onSelected: (LocalTime) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    val hours = (0..23).toList()
    val minutes = (0..59).toList()

    val hourState = rememberLazyListState(initialFirstVisibleItemIndex = selected.hour)
    val minuteState = rememberLazyListState(initialFirstVisibleItemIndex = selected.minute)

    val hourFlingBehavior = rememberSnapFlingBehavior(lazyListState = hourState)
    val minuteFlingBehavior = rememberSnapFlingBehavior(lazyListState = minuteState)

    fun centeredIndex(state: LazyListState): Int? {
        val layout = state.layoutInfo
        val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
        return layout.visibleItemsInfo
            .minByOrNull { item ->
                val itemCenter = item.offset + item.size / 2
                kotlin.math.abs(itemCenter - viewportCenter)
            }
            ?.index
    }

    val centeredHourIndex by remember {
        derivedStateOf { centeredIndex(hourState) }
    }
    val centeredMinuteIndex by remember {
        derivedStateOf { centeredIndex(minuteState) }
    }

    LaunchedEffect(hourState, minuteState) {
        snapshotFlow { centeredHourIndex to centeredMinuteIndex }
            .mapNotNull { (hIdx, mIdx) ->
                val hour = hIdx?.coerceIn(0, 23) ?: return@mapNotNull null
                val minute = mIdx?.coerceIn(0, 59) ?: return@mapNotNull null
                LocalTime(hour, minute)
            }
            .distinctUntilChanged()
            .collect { onSelected(it) }
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                WheelColumn(
                    values = hours,
                    selectedValue = selected.hour,
                    state = hourState,
                    flingBehavior = hourFlingBehavior,
                    formatValue = { String.format("%02d", it) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${testTagPrefix}HourWheel")
                )

                Spacer(modifier = Modifier.width(8.dp))

                WheelColumn(
                    values = minutes,
                    selectedValue = selected.minute,
                    state = minuteState,
                    flingBehavior = minuteFlingBehavior,
                    formatValue = { String.format("%02d", it) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("${testTagPrefix}MinuteWheel")
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = String.format("%02d:%02d", selected.hour, selected.minute),
            modifier = Modifier.testTag("${testTagPrefix}SelectedTimeText"),
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumn(
    values: List<Int>,
    selectedValue: Int,
    state: LazyListState,
    flingBehavior: FlingBehavior,
    formatValue: (Int) -> String,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        flingBehavior = flingBehavior,
        contentPadding = PaddingValues(vertical = 42.dp)
    ) {
        items(values.size) { idx ->
            val value = values[idx]
            val isActive = value == selectedValue
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = formatValue(value),
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@Composable
private fun ActivityQuickSelectSection(
    activityTypes: List<ActivityType>,
    selectedActivityId: Long,
    onSelect: (Long) -> Unit,
    onMore: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("活动类型", style = MaterialTheme.typography.titleMedium)

        val quick = activityTypes.take(8)
        val row1 = quick.take(4)
        val row2 = quick.drop(4)

        ActivityRow(row1, selectedActivityId, onSelect)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            row2.forEach { a ->
                AssistChip(
                    onClick = { onSelect(a.id) },
                    label = { Text(a.name) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(parseColor(a.colorHex), RoundedCornerShape(99.dp))
                        )
                    },
                    colors = if (a.id == selectedActivityId) {
                        androidx.compose.material3.AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    } else {
                        androidx.compose.material3.AssistChipDefaults.assistChipColors()
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            AssistChip(
                onClick = onMore,
                label = { Text("更多") },
                leadingIcon = { Icon(Icons.Default.MoreHoriz, contentDescription = null) }
            )
        }
    }
}

@Composable
private fun ActivityRow(
    activities: List<ActivityType>,
    selectedActivityId: Long,
    onSelect: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        activities.forEach { a ->
            AssistChip(
                onClick = { onSelect(a.id) },
                label = { Text(a.name) },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(parseColor(a.colorHex), RoundedCornerShape(99.dp))
                    )
                },
                colors = if (a.id == selectedActivityId) {
                    androidx.compose.material3.AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                } else {
                    androidx.compose.material3.AssistChipDefaults.assistChipColors()
                }
            )
        }
    }
}

@Composable
private fun TagsSection(
    availableTags: List<Tag>,
    selectedTagIds: Set<Long>,
    onToggle: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("标签", style = MaterialTheme.typography.titleMedium)

            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "收起" else "展开")
            }
        }

        if (availableTags.isEmpty()) {
            Text(
                text = "暂无标签",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return
        }

        val selected = availableTags.filter { it.id in selectedTagIds }
        if (selected.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                selected.take(3).forEach { tag ->
                    AssistChip(
                        onClick = { onToggle(tag.id) },
                        label = { Text(tag.name) }
                    )
                }
                if (selected.size > 3) {
                    AssistChip(
                        onClick = { expanded = true },
                        label = { Text("+${selected.size - 3}") }
                    )
                }
            }
        }

        if (expanded) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableTags.forEach { tag ->
                    val isSelected = tag.id in selectedTagIds
                    AssistChip(
                        onClick = { onToggle(tag.id) },
                        label = { Text(tag.name) },
                        colors = if (isSelected) {
                            androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        } else {
                            androidx.compose.material3.AssistChipDefaults.assistChipColors()
                        }
                    )
                }
            }
        } else {
            TextButton(onClick = { expanded = true }) {
                Text("添加标签")
            }
        }
    }
}

@Composable
private fun NoteSection(
    note: String,
    onNoteChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("备注", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            placeholder = { Text("添加备注...") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun BottomActionRow(
    isCreateMode: Boolean,
    isValid: Boolean,
    onSave: () -> Unit,
    onSplit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onSave,
            enabled = isValid,
            modifier = Modifier.weight(1f)
        ) {
            Icon(if (isCreateMode) Icons.Default.Add else Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isCreateMode) "创建" else "保存")
        }

        if (!isCreateMode) {
            Button(
                onClick = onSplit,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.CallSplit, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("拆分")
            }
        }
    }
}

private fun formatDurationSeconds(seconds: Int): String {
    if (seconds <= 0) return "0秒"

    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60

    return when {
        h > 0 && m > 0 && s > 0 -> "${h}小时${m}分钟${s}秒"
        h > 0 && m > 0 -> "${h}小时${m}分钟"
        h > 0 && s > 0 -> "${h}小时${s}秒"
        h > 0 -> "${h}小时"
        m > 0 && s > 0 -> "${m}分钟${s}秒"
        m > 0 -> "${m}分钟"
        else -> "${s}秒"
    }
}

private fun parseColor(colorHex: String): androidx.compose.ui.graphics.Color {
    return try {
        androidx.compose.ui.graphics.Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        androidx.compose.ui.graphics.Color(0xFF4CAF50)
    }
}

@Composable
private fun ActivitySelectorDialog(
    activityTypes: List<ActivityType>,
    selectedActivityId: Long,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择活动类型") },
        text = {
            if (activityTypes.isEmpty()) {
                Text("暂无活动类型")
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 360.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activityTypes.size) { idx ->
                        val a = activityTypes[idx]
                        val isSelected = a.id == selectedActivityId
                        AssistChip(
                            onClick = { onSelect(a.id) },
                            label = { Text(a.name) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(parseColor(a.colorHex), RoundedCornerShape(99.dp))
                                )
                            },
                            colors = if (isSelected) {
                                androidx.compose.material3.AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            } else {
                                androidx.compose.material3.AssistChipDefaults.assistChipColors()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
