package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.model.TimeEntryInput
import com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype.GetActivityTypesUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.tag.GetTagsUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.CreateTimeEntryUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.DeleteTimeEntryUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.GetTimeEntriesUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.UpdateTimeEntryUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.DayGroup
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.MergeTimeEntriesUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.SplitTimeEntryUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.TimelineItem
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.createDayGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

enum class TimelineEntrySheetMode {
    EDIT,
    CREATE
}

internal data class TimelineCreatePrefill(
    val baseDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
)

internal fun defaultPrefillForSelectedTimelineDate(
    selectedDate: LocalDate,
    now: Instant,
    timeZone: TimeZone
): TimelineCreatePrefill {
    val nowLocal = now.toLocalDateTime(timeZone)
    val today = nowLocal.date

    val baseDate = if (selectedDate == today) {
        today
    } else {
        selectedDate
    }

    val alignedTime = LocalTime(nowLocal.hour, nowLocal.minute)

    return TimelineCreatePrefill(
        baseDate = baseDate,
        startTime = alignedTime,
        endTime = alignedTime
    )
}

data class TimelineUiState(
    val selectedDate: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val dayGroups: List<DayGroup> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedEntryIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val entryToSplit: TimeEntry? = null,
    val showMergeConfirmation: Boolean = false,
    val showDatePicker: Boolean = false,
    val sheetDraft: TimeEntryDraft? = null,
    val sheetMode: TimelineEntrySheetMode = TimelineEntrySheetMode.EDIT,
    val activityTypes: List<ActivityType> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val hiddenEntryIds: Set<Long> = emptySet()
) { 
    val weekStartDate: LocalDate
        get() {
            val daysFromMonday = selectedDate.dayOfWeek.ordinal
            return selectedDate.minus(daysFromMonday, DateTimeUnit.DAY)
        }
}

data class TimeEntryDraft(
    val entryId: Long,
    val baseDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val activityId: Long,
    val tagIds: Set<Long>,
    val note: String,
    val adjacentUpEntryId: Long?,
    val adjacentDownEntryId: Long?
) {
    val durationMinutes: Int
        get() {
            val startMinutes = startTime.hour * 60 + startTime.minute
            var endMinutes = endTime.hour * 60 + endTime.minute
            if (endMinutes <= startMinutes) {
                endMinutes += 24 * 60
            }
            return endMinutes - startMinutes
        }
}

sealed class TimelineEvent {
    data class NavigateToEdit(val entryId: Long) : TimelineEvent()
    data class Error(val message: String) : TimelineEvent()

    data object SplitSuccess : TimelineEvent()
    data object MergeSuccess : TimelineEvent()
    data object SaveSuccess : TimelineEvent()

    data class ShowDeleteUndo(
        val token: Long,
        val message: String,
        val actionLabel: String = "撤销"
    ) : TimelineEvent()
}

private data class PendingDelete(
    val entryIds: Set<Long>
)

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getTimeEntries: GetTimeEntriesUseCase,
    private val createTimeEntry: CreateTimeEntryUseCase,
    private val deleteTimeEntry: DeleteTimeEntryUseCase,
    private val splitTimeEntry: SplitTimeEntryUseCase,
    private val mergeTimeEntries: MergeTimeEntriesUseCase,
    private val updateTimeEntry: UpdateTimeEntryUseCase,
    private val getActivityTypes: GetActivityTypesUseCase,
    private val getTags: GetTagsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TimelineEvent>()
    val events: SharedFlow<TimelineEvent> = _events.asSharedFlow()

    private var loadJob: Job? = null

    private var latestEntries: List<TimeEntry> = emptyList()

    private var nextDeleteToken: Long = 1
    private val pendingDeletesByToken = LinkedHashMap<Long, PendingDelete>()

    init {
        observeReferenceData()
        loadEntries()
    }

    private fun observeReferenceData() {
        viewModelScope.launch {
            combine(
                getActivityTypes(includeArchived = false),
                getTags(includeArchived = false)
            ) { activities, tags ->
                activities to tags
            }.collect { (activities, tags) ->
                _uiState.update { it.copy(activityTypes = activities, availableTags = tags) }
            }
        }
    }

    private fun loadEntries() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val weekStart = _uiState.value.weekStartDate
            val weekEnd = weekStart.plus(7, DateTimeUnit.DAY)

            val tz = TimeZone.currentSystemDefault()
            val startInstant = weekStart.atTime(LocalTime(0, 0)).toInstant(tz)
            val endInstant = weekEnd.atTime(LocalTime(0, 0)).toInstant(tz)

            getTimeEntries(startInstant, endInstant)
                .collect { entries ->
                    latestEntries = entries
                    updateDayGroups()
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun updateDayGroups() {
        val tz = TimeZone.currentSystemDefault()
        val hidden = _uiState.value.hiddenEntryIds
        val visibleEntries = latestEntries.filterNot { it.id in hidden }

        val dayGroups = visibleEntries
            .groupBy { it.startTime.toLocalDateTime(tz).date }
            .map { (date, dayEntries) ->
                createDayGroup(date = date, entries = dayEntries, timeZone = tz)
            }
            .sortedByDescending { it.date }

        _uiState.update { it.copy(dayGroups = dayGroups) }
    }

    fun refresh() {
        loadEntries()
    }

    fun setSelectedDate(date: LocalDate) {
        val currentWeekStart = _uiState.value.weekStartDate
        val newWeekStart = date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)

        _uiState.update { it.copy(selectedDate = date, showDatePicker = false) }
        if (currentWeekStart != newWeekStart) {
            loadEntries()
        }
    }

    fun navigateToToday() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        setSelectedDate(today)
    }

    fun navigateWeek(deltaWeeks: Int) {
        val currentDate = _uiState.value.selectedDate
        val newDate = currentDate.plus(deltaWeeks * 7, DateTimeUnit.DAY)
        setSelectedDate(newDate)
    }

    fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    fun hideDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }


    fun onEditEntry(entryId: Long) {
        viewModelScope.launch {
            _events.emit(TimelineEvent.NavigateToEdit(entryId))
        }
    }

    fun onEntryClick(entry: TimeEntry) {
        if (_uiState.value.isSelectionMode) {
            toggleEntrySelection(entry.id)
            return
        }
        openEntrySheet(entry.id)
    }

    fun onQuickCreate() {
        val state = _uiState.value
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()

        val prefill = defaultPrefillForSelectedTimelineDate(
            selectedDate = state.selectedDate,
            now = now,
            timeZone = tz
        )

        _uiState.update {
            it.copy(
                sheetMode = TimelineEntrySheetMode.CREATE,
                sheetDraft = TimeEntryDraft(
                    entryId = 0L,
                    baseDate = prefill.baseDate,
                    startTime = prefill.startTime,
                    endTime = prefill.endTime,
                    activityId = it.activityTypes.firstOrNull()?.id ?: 0L,
                    tagIds = emptySet(),
                    note = "",
                    adjacentUpEntryId = null,
                    adjacentDownEntryId = null
                )
            )
        }
    }

    fun onCreateFromSheet() {
        val draft = _uiState.value.sheetDraft ?: return
        if (_uiState.value.sheetMode != TimelineEntrySheetMode.CREATE) return

        if (draft.endTime <= draft.startTime) return

        val tz = TimeZone.currentSystemDefault()
        val startInstant = draft.baseDate.atTime(draft.startTime).toInstant(tz)
        var endInstant = draft.baseDate.atTime(draft.endTime).toInstant(tz)
        if (endInstant <= startInstant) {

            endInstant = endInstant.plus(1, DateTimeUnit.DAY, tz)
        }

        val input = TimeEntryInput(
            activityId = draft.activityId,
            startTime = startInstant,
            endTime = endInstant,
            note = draft.note.takeIf { it.isNotBlank() },
            tagIds = draft.tagIds.toList()
        )

        viewModelScope.launch {
            val result = createTimeEntry(input)
            result.fold(
                onSuccess = {
                    dismissEntrySheet()
                    refresh()
                },
                onFailure = { error ->
                    _events.emit(TimelineEvent.Error(error.message ?: "创建失败"))
                }
            )
        }
    }


    fun dismissEntrySheet() {
        _uiState.update { it.copy(sheetDraft = null, sheetMode = TimelineEntrySheetMode.EDIT) }
    }

    fun onEntryLongPress(entry: TimeEntry) {
        dismissEntrySheet()
        _uiState.update {
            it.copy(
                isSelectionMode = true,
                selectedEntryIds = setOf(entry.id)
            )
        }
    }

    private fun openEntrySheet(entryId: Long) {
        val entry = findEntryById(entryId) ?: return
        val tz = TimeZone.currentSystemDefault()
        val startDateTime = entry.startTime.toLocalDateTime(tz)
        val endDateTime = entry.endTime.toLocalDateTime(tz)

        val (upId, downId) = findAdjacentEntryIds(entry.id)

        _uiState.update {
            it.copy(
                sheetDraft = TimeEntryDraft(
                    entryId = entry.id,
                    baseDate = startDateTime.date,
                    startTime = startDateTime.time,
                    endTime = endDateTime.time,
                    activityId = entry.activityId,
                    tagIds = entry.tags.map { t -> t.id }.toSet(),
                    note = entry.note.orEmpty(),
                    adjacentUpEntryId = upId,
                    adjacentDownEntryId = downId
                )
            )
        }
    }

    private fun findEntryById(entryId: Long): TimeEntry? {
        return _uiState.value.dayGroups
            .asSequence()
            .flatMap { dayGroup -> dayGroup.items.asSequence() }
            .mapNotNull { it as? TimelineItem.Entry }
            .map { it.entry }
            .firstOrNull { it.id == entryId }
    }

    fun onDraftStartTimeChange(time: LocalTime) {
        _uiState.update { state ->
            val draft = state.sheetDraft ?: return
            state.copy(sheetDraft = draft.copy(startTime = time))
        }
    }

    fun onDraftEndTimeChange(time: LocalTime) {
        _uiState.update { state ->
            val draft = state.sheetDraft ?: return
            state.copy(sheetDraft = draft.copy(endTime = time))
        }
    }

    fun onDraftNoteChange(note: String) {
        _uiState.update { state ->
            val draft = state.sheetDraft ?: return
            state.copy(sheetDraft = draft.copy(note = note))
        }
    }

    fun onDraftActivitySelect(activityId: Long) {
        _uiState.update { state ->
            val draft = state.sheetDraft ?: return
            state.copy(sheetDraft = draft.copy(activityId = activityId))
        }
    }

    fun onDraftTagToggle(tagId: Long) {
        _uiState.update { state ->
            val draft = state.sheetDraft ?: return
            val newTagIds = if (tagId in draft.tagIds) {
                draft.tagIds - tagId
            } else {
                draft.tagIds + tagId
            }
            state.copy(sheetDraft = draft.copy(tagIds = newTagIds))
        }
    }

    fun onSaveDraft() {
        val draft = _uiState.value.sheetDraft ?: return
        val entry = findEntryById(draft.entryId) ?: return

        val tz = TimeZone.currentSystemDefault()
        val startInstant = draft.baseDate.atTime(draft.startTime).toInstant(tz)
        var endInstant = draft.baseDate.atTime(draft.endTime).toInstant(tz)
        if (endInstant <= startInstant) {
            endInstant = endInstant.plus(1, DateTimeUnit.DAY, tz)
        }

        val input = TimeEntryInput(
            activityId = draft.activityId,
            startTime = startInstant,
            endTime = endInstant,
            note = draft.note.takeIf { it.isNotBlank() },
            tagIds = draft.tagIds.toList()
        )

        viewModelScope.launch {
            val result = updateTimeEntry(entry.id, input)
            result.fold(
                onSuccess = {
                    dismissEntrySheet()
                    _events.emit(TimelineEvent.SaveSuccess)
                    refresh()
                },
                onFailure = { error ->
                    _events.emit(TimelineEvent.Error(error.message ?: "保存失败"))
                }
            )
        }
    }

    private fun findAdjacentEntryIds(entryId: Long): Pair<Long?, Long?> {
        for (dayGroup in _uiState.value.dayGroups) {
            val entries = dayGroup.items
                .mapNotNull { it as? TimelineItem.Entry }
                .map { it.entry }
                .sortedWith(compareBy<TimeEntry> { it.startTime }.thenBy { it.id })

            val idx = entries.indexOfFirst { it.id == entryId }
            if (idx >= 0) {
                val up = entries.getOrNull(idx - 1)?.id
                val down = entries.getOrNull(idx + 1)?.id
                return up to down
            }
        }
        return null to null
    }

    fun canMergeUp(entryId: Long): Boolean {
        return findAdjacentEntryIds(entryId).first != null
    }

    fun canMergeDown(entryId: Long): Boolean {
        return findAdjacentEntryIds(entryId).second != null
    }

    fun onMergeUp() {
        val draft = _uiState.value.sheetDraft ?: return
        val upId = draft.adjacentUpEntryId
        if (upId == null) {
            viewModelScope.launch { _events.emit(TimelineEvent.Error("没有可合并的上一条记录")) }
            return
        }

        viewModelScope.launch {
            val result = mergeTimeEntries(listOf(upId, draft.entryId))
            result.fold(
                onSuccess = {
                    dismissEntrySheet()
                    _events.emit(TimelineEvent.MergeSuccess)
                    refresh()
                },
                onFailure = { error ->
                    _events.emit(TimelineEvent.Error(error.message ?: "合并失败"))
                }
            )
        }
    }

    fun onMergeDown() {
        val draft = _uiState.value.sheetDraft ?: return
        val downId = draft.adjacentDownEntryId
        if (downId == null) {
            viewModelScope.launch { _events.emit(TimelineEvent.Error("没有可合并的下一条记录")) }
            return
        }

        viewModelScope.launch {
            val result = mergeTimeEntries(listOf(draft.entryId, downId))
            result.fold(
                onSuccess = {
                    dismissEntrySheet()
                    _events.emit(TimelineEvent.MergeSuccess)
                    refresh()
                },
                onFailure = { error ->
                    _events.emit(TimelineEvent.Error(error.message ?: "合并失败"))
                }
            )
        }
    }

    fun onSplitFromSheet() {
        val entryId = _uiState.value.sheetDraft?.entryId ?: return
        val entry = findEntryById(entryId) ?: return
        dismissEntrySheet()
        onSplitRequest(entry)
    }

    private fun toggleEntrySelection(entryId: Long) {
        _uiState.update { state ->
            val newSelection = if (entryId in state.selectedEntryIds) {
                state.selectedEntryIds - entryId
            } else {
                state.selectedEntryIds + entryId
            }

            state.copy(
                selectedEntryIds = newSelection,
                isSelectionMode = newSelection.isNotEmpty()
            )
        }
    }

    fun exitSelectionMode() {
        dismissEntrySheet()
        _uiState.update {
            it.copy(
                isSelectionMode = false,
                selectedEntryIds = emptySet()
            )
        }
    }

    fun onSplitRequest(entry: TimeEntry) {
        _uiState.update { it.copy(entryToSplit = entry) }
    }

    fun onSplitConfirm(splitTime: Instant) {
        val entry = _uiState.value.entryToSplit ?: return

        viewModelScope.launch {
            val result = splitTimeEntry(entry.id, splitTime)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(entryToSplit = null) }
                    _events.emit(TimelineEvent.SplitSuccess)
                    refresh()
                },
                onFailure = { error ->
                    _events.emit(TimelineEvent.Error(error.message ?: "拆分失败"))
                }
            )
        }
    }

    fun onSplitCancel() {
        _uiState.update { it.copy(entryToSplit = null) }
    }

    fun onMergeRequest() {
        val selectedIds = _uiState.value.selectedEntryIds
        if (selectedIds.size < 2) {
            viewModelScope.launch {
                _events.emit(TimelineEvent.Error("请至少选择两条记录进行合并"))
            }
            return
        }

        _uiState.update { it.copy(showMergeConfirmation = true) }
    }

    fun onMergeConfirm() {
        val selectedIds = _uiState.value.selectedEntryIds.toList()

        viewModelScope.launch {
            val result = mergeTimeEntries(selectedIds)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            showMergeConfirmation = false,
                            isSelectionMode = false,
                            selectedEntryIds = emptySet()
                        )
                    }
                    _events.emit(TimelineEvent.MergeSuccess)
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(showMergeConfirmation = false) }
                    _events.emit(TimelineEvent.Error(error.message ?: "合并失败"))
                }
            )
        }
    }

    fun onMergeCancel() {
        _uiState.update { it.copy(showMergeConfirmation = false) }
    }

    fun onBatchDeleteRequest() {
        val ids = _uiState.value.selectedEntryIds
        if (ids.isEmpty()) return

        exitSelectionMode()
        requestDelete(ids)
    }

    fun onDeleteFromSheet() {
        val entryId = _uiState.value.sheetDraft?.entryId ?: return
        dismissEntrySheet()
        requestDelete(setOf(entryId))
    }

    private fun requestDelete(entryIds: Set<Long>) {
        val token = nextDeleteToken++
        pendingDeletesByToken[token] = PendingDelete(entryIds = entryIds)

        _uiState.update { state ->
            state.copy(hiddenEntryIds = state.hiddenEntryIds + entryIds)
        }
        updateDayGroups()

        viewModelScope.launch {
            val message = if (entryIds.size == 1) {
                "已删除 1 条记录"
            } else {
                "已删除 ${entryIds.size} 条记录"
            }
            _events.emit(TimelineEvent.ShowDeleteUndo(token = token, message = message))
        }
    }

    fun onDeleteUndo(token: Long) {
        val pending = pendingDeletesByToken.remove(token) ?: return
        _uiState.update { state ->
            state.copy(hiddenEntryIds = state.hiddenEntryIds - pending.entryIds)
        }
        updateDayGroups()
    }

    fun onDeleteCommit(token: Long) {
        val pending = pendingDeletesByToken.remove(token) ?: return

        viewModelScope.launch {
            val failures = mutableListOf<Throwable>()
            pending.entryIds.forEach { id ->
                val result = deleteTimeEntry(id)
                result.exceptionOrNull()?.let { failures += it }
            }

            if (failures.isEmpty()) {
                refresh()
            } else {
                _uiState.update { state ->
                    state.copy(hiddenEntryIds = state.hiddenEntryIds - pending.entryIds)
                }
                updateDayGroups()
                _events.emit(TimelineEvent.Error(failures.first().message ?: "删除失败"))
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
