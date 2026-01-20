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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.Tag
import kotlinx.datetime.LocalTime
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import androidx.compose.runtime.snapshotFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimelineEntryBottomSheet(
    draft: TimeEntryDraft,
    activityTypes: List<ActivityType>,
    availableTags: List<Tag>,
    canMergeUp: Boolean,
    canMergeDown: Boolean,
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
                .testTag("timelineEntrySheet"),
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
                        durationMinutes = draft.durationMinutes,
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

        IconButton(onClick = onClose) {
            Icon(Icons.Default.Close, contentDescription = "关闭")
        }
    }
}

@Composable
private fun TimeEditorSection(
    startTime: LocalTime,
    endTime: LocalTime,
    durationMinutes: Int,
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
                modifier = Modifier.weight(1f)
            )
            TimeWheel(
                label = "结束",
                selected = endTime,
                onSelected = onEndTimeChange,
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "时长：${formatDurationMinutes(durationMinutes)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TimeWheel(
    label: String,
    selected: LocalTime,
    onSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMinutes = selected.hour * 60 + selected.minute
    val state = rememberLazyListState(initialFirstVisibleItemIndex = selectedMinutes)
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = state)

    val centeredIndex by remember {
        derivedStateOf {
            val layout = state.layoutInfo
            val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
            layout.visibleItemsInfo
                .minByOrNull { item ->
                    val itemCenter = item.offset + item.size / 2
                    kotlin.math.abs(itemCenter - viewportCenter)
                }
                ?.index
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { centeredIndex }
            .filterNotNull()
            .distinctUntilChanged()
            .map { idx ->
                val minutesOfDay = idx.coerceIn(0, 24 * 60 - 1)
                LocalTime(minutesOfDay / 60, minutesOfDay % 60)
            }
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                flingBehavior = flingBehavior,
                contentPadding = PaddingValues(vertical = 36.dp)
            ) {
                items(24 * 60) { idx ->
                    val h = idx / 60
                    val m = idx % 60
                    val isActive = idx == selectedMinutes
                    Text(
                        text = String.format("%02d:%02d", h, m),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        color = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("保存")
        }

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

private fun formatDurationMinutes(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "${h}小时${m}分钟"
        h > 0 -> "${h}小时"
        else -> "${m}分钟"
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
