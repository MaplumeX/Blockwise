package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.model.TimeEntryInput
import com.maplume.blockwise.feature.timeentry.domain.model.TimerState
import com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype.GetActivityTypesUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.tag.GetTagsUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.GetTimerStateUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.StartTimerUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.StopTimerUseCase
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
    val startDate: LocalDate,
    val endDate: LocalDate,
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

    val earliest = today.minus(29, DateTimeUnit.DAY)

    val startDate = when {
        selectedDate > today -> today
        selectedDate < earliest -> earliest
        else -> selectedDate
    }

    val alignedTime = LocalTime(nowLocal.hour, nowLocal.minute)

    return TimelineCreatePrefill(
        startDate = startDate,
        endDate = startDate,
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
    val timerState: TimerState = TimerState.Idle,
    val timerElapsedMillis: Long = 0L,
    val showTimerActivitySelector: Boolean = false,
    val hiddenEntryIds: Set<Long> = emptySet()
) {
    val weekStartDate: LocalDate
        get() {
            val daysFromMonday = selectedDate.dayOfWeek.ordinal
            return selectedDate.minus(daysFromMonday, DateTimeUnit.DAY)
        }

    val isSelectedDateToday: Boolean
        get() = selectedDate == Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
}

data class TimeEntryDraft(
    val entryId: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val activityId: Long,
    val tagIds: Set<Long>,
    val note: String,
    val adjacentUpEntryId: Long?,
    val adjacentDownEntryId: Long?
) {
    val startInstant
        get() = startDate.atTime(startTime).toInstant(TimeZone.currentSystemDefault())

    val endInstant
        get() = endDate.atTime(endTime).toInstant(TimeZone.currentSystemDefault())

    val isValid: Boolean
        get() = endInstant > startInstant

    val durationSeconds: Int
        get() {
            val start = startInstant
            val end = endInstant
            val seconds = ((end.toEpochMilliseconds() - start.toEpochMilliseconds()) / 1000).toInt()
            return seconds.coerceAtLeast(0)
        }
}

sealed class TimelineEvent {
    data class NavigateToEdit(val entryId: Long) : TimelineEvent()
    data class Error(val message: String) : TimelineEvent()

    data object SplitSuccess : TimelineEvent()
    data object MergeSuccess : TimelineEvent()
    data object SaveSuccess : TimelineEvent()

    data class DeleteSuccess(
        val message: String
    ) : TimelineEvent()
}

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getTimeEntries: GetTimeEntriesUseCase,
    private val createTimeEntry: CreateTimeEntryUseCase,
    private val deleteTimeEntry: DeleteTimeEntryUseCase,
    private val splitTimeEntry: SplitTimeEntryUseCase,
    private val mergeTimeEntries: MergeTimeEntriesUseCase,
    private val updateTimeEntry: UpdateTimeEntryUseCase,
    private val getActivityTypes: GetActivityTypesUseCase,
    private val getTags: GetTagsUseCase,
    private val getTimerState: GetTimerStateUseCase,
    private val startTimer: StartTimerUseCase,
    private val stopTimer: StopTimerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TimelineEvent>()
    val events: SharedFlow<TimelineEvent> = _events.asSharedFlow()

    private var loadJob: Job? = null

    private var latestEntries: List<TimeEntry> = emptyList()

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

        viewModelScope.launch {
            combine(
                getTimerState.state,
                getTimerState.elapsedMillis
            ) { state, elapsedMillis ->
                state to elapsedMillis
            }.collect { (state, elapsedMillis) ->
                _uiState.update {
                    it.copy(
                        timerState = state,
                        timerElapsedMillis = elapsedMillis
                    )
                }
            }
        }
    }

     private fun loadEntries() {
         _uiState.update { it.copy(isLoading = true) }
         loadJob?.cancel()
         loadJob = viewModelScope.launch {


            val weekStart = _uiState.value.weekStartDate
            val weekEnd = weekStart.plus(7, DateTimeUnit.DAY)

            val tz = TimeZone.currentSystemDefault()
            val startInstant = weekStart.atTime(LocalTime(0, 0)).toInstant(tz)
            val endInstant = weekEnd.atTime(LocalTime(0, 0)).toInstant(tz)

            getTimeEntries(startInstant, endInstant)
                .collect { entries ->
                    latestEntries = entries
                    updateDayGroups(weekStart = weekStart)
                    _uiState.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun updateDayGroups(weekStart: LocalDate = _uiState.value.weekStartDate) {
        val tz = TimeZone.currentSystemDefault()
        val hidden = _uiState.value.hiddenEntryIds
        val visibleEntries = latestEntries.filterNot { it.id in hidden }

        val dates = (0..6).map { weekStart.plus(it, DateTimeUnit.DAY) }

        val dayGroups = dates
            .mapNotNull { date ->
                val dayStart = date.atTime(LocalTime(0, 0)).toInstant(tz)
                val dayEnd = date.plus(1, DateTimeUnit.DAY).atTime(LocalTime(0, 0)).toInstant(tz)

                val dayEntries = visibleEntries.filter { entry ->
                    entry.startTime < dayEnd && entry.endTime > dayStart
                }

                dayEntries.takeIf { it.isNotEmpty() }?.let {
                    createDayGroup(date = date, entries = it, timeZone = tz)
                }
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
                    startDate = prefill.startDate,
                    endDate = prefill.endDate,
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

        if (!draft.isValid) return

        val startInstant = draft.startInstant
        val endInstant = draft.endInstant

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
                    startDate = startDateTime.date,
                    endDate = endDateTime.date,
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
            .map { it.slice.entry }
            .firstOrNull { it.id == entryId }
    }

    fun onDraftStartTimeChange(time: LocalTime) {
        _uiState.update { state ->
            val draft = state.sheetDraft ?: return

            val resolved = if (time.second == 0 && time.nanosecond == 0) {
                LocalTime(time.hour, time.minute, draft.startTime.second, draft.startTime.nanosecond)
            } else {
                time
            }

            state.copy(sheetDraft = draft.copy(startTime = resolved))
        }
    }

    fun onDraftStartDateChange(date: LocalDate) {
        _uiState.update { state ->
            val draft = state.sheetDraft ?: return
            state.copy(sheetDraft = draft.copy(startDate = date))
        }
    }

    fun onDraftEndDateChange(date: LocalDate) {
        _uiState.update { state ->
            val draft = state.sheetDraft ?: return
            state.copy(sheetDraft = draft.copy(endDate = date))
        }
    }


    fun onDraftEndTimeChange(time: LocalTime) {
        _uiState.update { state ->
            val draft = state.sheetDraft ?: return

            val resolved = if (time.second == 0 && time.nanosecond == 0) {
                LocalTime(time.hour, time.minute, draft.endTime.second, draft.endTime.nanosecond)
            } else {
                time
            }

            if (resolved == draft.endTime) {
                state
            } else {
                var nextEndDate = draft.endDate
                if (draft.endDate == draft.startDate && resolved <= draft.startTime) {
                    val tz = TimeZone.currentSystemDefault()
                    val today = Clock.System.now().toLocalDateTime(tz).date
                    val inferred = draft.startDate.plus(1, DateTimeUnit.DAY)
                    if (inferred <= today) {
                        nextEndDate = inferred
                    }
                }

                state.copy(sheetDraft = draft.copy(endDate = nextEndDate, endTime = resolved))
            }
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

        if (!draft.isValid) {
            viewModelScope.launch { _events.emit(TimelineEvent.Error("结束时间需晚于起始时间")) }
            return
        }

        val startInstant = draft.startInstant
        val endInstant = draft.endInstant

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
                .map { it.slice.entry }
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
        deleteEntries(ids)
    }

    fun onDeleteFromSheet() {
        val entryId = _uiState.value.sheetDraft?.entryId ?: return
        dismissEntrySheet()
        deleteEntries(setOf(entryId))
    }

    private fun deleteEntries(entryIds: Set<Long>) {
        if (entryIds.isEmpty()) return

        // Hide immediately to avoid a flicker while the DB deletion runs.
        _uiState.update { state ->
            state.copy(hiddenEntryIds = state.hiddenEntryIds + entryIds)
        }
        updateDayGroups()

        viewModelScope.launch {
            val failures = mutableListOf<Throwable>()
            entryIds.forEach { id ->
                val result = deleteTimeEntry(id)
                result.exceptionOrNull()?.let { failures += it }
            }

            if (failures.isEmpty()) {
                // Clear hidden state proactively to avoid unbounded growth.
                _uiState.update { state ->
                    state.copy(hiddenEntryIds = state.hiddenEntryIds - entryIds)
                }
                updateDayGroups()
                refresh()

                val message = if (entryIds.size == 1) {
                    "已删除 1 条记录"
                } else {
                    "已删除 ${entryIds.size} 条记录"
                }
                _events.emit(TimelineEvent.DeleteSuccess(message = message))
            } else {
                _uiState.update { state ->
                    state.copy(hiddenEntryIds = state.hiddenEntryIds - entryIds)
                }
                updateDayGroups()
                _events.emit(TimelineEvent.Error(failures.first().message ?: "删除失败"))
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun showTimerActivitySelector() {
        _uiState.update { it.copy(showTimerActivitySelector = true) }
    }

    fun hideTimerActivitySelector() {
        _uiState.update { it.copy(showTimerActivitySelector = false) }
    }

    fun onStartTimer(activityType: ActivityType) {
        if (_uiState.value.timerState.isActive) return

        startTimer(
            activityId = activityType.id,
            activityName = activityType.name,
            activityColorHex = activityType.colorHex,
            tagIds = emptyList()
        )
    }

    fun onStopTimer() {
        viewModelScope.launch {
            val result = stopTimer(createEntry = true)
            result.fold(
                onSuccess = { refresh() },
                onFailure = { error ->
                    _events.emit(TimelineEvent.Error(error.message ?: "保存失败"))
                }
            )
        }
    }
}
