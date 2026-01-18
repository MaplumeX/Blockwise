package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.DeleteTimeEntryUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.GetTimeEntriesUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.DayGroup
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.MergeTimeEntriesUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.SplitTimeEntryUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.TimelineItem
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.createDayGroup
import androidx.compose.ui.geometry.Offset
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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

/**
 * UI state for the timeline screen.
 */
data class TimelineUiState(
    val selectedDate: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val dayGroups: List<DayGroup> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedEntryIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val contextMenu: TimelineContextMenuState? = null,
    val entryToDelete: TimeEntry? = null,
    val entryToSplit: TimeEntry? = null,
    val showMergeConfirmation: Boolean = false,
    val showDatePicker: Boolean = false
) { 
    val weekStartDate: LocalDate
        get() {
            val dayOfWeek = selectedDate.dayOfWeek
            val daysFromMonday = dayOfWeek.ordinal
            return selectedDate.minus(daysFromMonday, DateTimeUnit.DAY)
        }
}

/**
 * One-time events from the timeline.
 */
sealed class TimelineEvent {
    data class NavigateToEdit(val entryId: Long) : TimelineEvent()
    data class Error(val message: String) : TimelineEvent()
    data object DeleteSuccess : TimelineEvent()
    data object SplitSuccess : TimelineEvent()
    data object MergeSuccess : TimelineEvent()
}

data class TimelineContextMenuState(
    val entryId: Long,
    val tapOffset: Offset
)

/**
 * ViewModel for the timeline screen.
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getTimeEntries: GetTimeEntriesUseCase,
    private val deleteTimeEntry: DeleteTimeEntryUseCase,
    private val splitTimeEntry: SplitTimeEntryUseCase,
    private val mergeTimeEntries: MergeTimeEntriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TimelineEvent>()
    val events: SharedFlow<TimelineEvent> = _events.asSharedFlow()

    private var loadJob: Job? = null

    init {
        loadEntries()
    }

    /**
     * Load entries for the current week.
     */
    private fun loadEntries() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val weekStart = _uiState.value.weekStartDate
            val weekEnd = weekStart.plus(7, DateTimeUnit.DAY) // Start of next week

            val tz = TimeZone.currentSystemDefault()
            val startInstant = weekStart.atTime(LocalTime(0, 0)).toInstant(tz)
            val endInstant = weekEnd.atTime(LocalTime(0, 0)).toInstant(tz)

            getTimeEntries(startInstant, endInstant)
                .collect { entries ->
                    val dayGroups = entries
                        .groupBy { it.startTime.toLocalDateTime(tz).date }
                        .map { (date, dayEntries) ->
                            createDayGroup(date = date, entries = dayEntries, timeZone = tz)
                        }
                        .sortedByDescending { it.date }

                    _uiState.update {
                        it.copy(
                            dayGroups = dayGroups,
                            isLoading = false
                        )
                    }
                }
        }
    }

    /**
     * Refresh the timeline.
     */
    fun refresh() {
        loadEntries()
    }

    /**
     * Set selected date.
     */
    fun setSelectedDate(date: LocalDate) {
        val currentWeekStart = _uiState.value.weekStartDate
        val newWeekStart = date.minus(date.dayOfWeek.ordinal, DateTimeUnit.DAY)
        
        _uiState.update { it.copy(selectedDate = date, showDatePicker = false) }
        
        // Only reload if week changed
        if (currentWeekStart != newWeekStart) {
            loadEntries()
        }
    }

    /**
     * Navigate to today.
     */
    fun navigateToToday() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        setSelectedDate(today)
    }

    /**
     * Navigate by weeks.
     */
    fun navigateWeek(deltaWeeks: Int) {
        val currentDate = _uiState.value.selectedDate
        val newDate = currentDate.plus(deltaWeeks * 7, DateTimeUnit.DAY)
        setSelectedDate(newDate)
    }
    
    /**
     * Show date picker.
     */
    fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    /**
     * Hide date picker.
     */
    fun hideDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }
    
    // Legacy loadMore removed as we are now week-based.

    /**
     * Handle entry click - navigate to edit.
     */
    fun onEntryClick(entry: TimeEntry, tapOffset: Offset) {
        if (_uiState.value.isSelectionMode) {
            toggleEntrySelection(entry.id)
            return
        }

        _uiState.update { it.copy(contextMenu = TimelineContextMenuState(entryId = entry.id, tapOffset = tapOffset)) }
    }

    fun dismissContextMenu() {
        _uiState.update { it.copy(contextMenu = null) }
    }

    fun onContextMenuEdit(entryId: Long) {
        dismissContextMenu()
        viewModelScope.launch {
            _events.emit(TimelineEvent.NavigateToEdit(entryId))
        }
    }

    fun onContextMenuDelete(entry: TimeEntry) {
        dismissContextMenu()
        onDeleteRequest(entry)
    }

    fun onContextMenuSplit(entry: TimeEntry) {
        dismissContextMenu()
        onSplitRequest(entry)
    }

    /**
     * Handle entry long press - enter selection mode or show context menu.
     */
    fun onEntryLongPress(entry: TimeEntry) {
        dismissContextMenu()
        _uiState.update {
            it.copy(
                isSelectionMode = true,
                selectedEntryIds = setOf(entry.id)
            )
        }
    }

    /**
     * Toggle entry selection.
     */
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

    /**
     * Exit selection mode.
     */
    fun exitSelectionMode() {
        dismissContextMenu()
        _uiState.update {
            it.copy(
                isSelectionMode = false,
                selectedEntryIds = emptySet()
            )
        }
    }

    /**
     * Request to delete an entry.
     */
    fun onDeleteRequest(entry: TimeEntry) {
        _uiState.update { it.copy(entryToDelete = entry) }
    }

    /**
     * Confirm deletion.
     */
    fun onDeleteConfirm() {
        val entry = _uiState.value.entryToDelete ?: return

        viewModelScope.launch {
            val result = deleteTimeEntry(entry.id)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(entryToDelete = null) }
                    _events.emit(TimelineEvent.DeleteSuccess)
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(entryToDelete = null) }
                    _events.emit(TimelineEvent.Error(error.message ?: "删除失败"))
                }
            )
        }
    }

    /**
     * Cancel deletion.
     */
    fun onDeleteCancel() {
        _uiState.update { it.copy(entryToDelete = null) }
    }

    /**
     * Request to split an entry.
     */
    fun onSplitRequest(entry: TimeEntry) {
        _uiState.update { it.copy(entryToSplit = entry) }
    }

    /**
     * Confirm split at the specified time.
     */
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

    /**
     * Cancel split.
     */
    fun onSplitCancel() {
        _uiState.update { it.copy(entryToSplit = null) }
    }

    /**
     * Request to merge selected entries.
     */
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

    /**
     * Confirm merge.
     */
    fun onMergeConfirm() {
        val selectedIds = _uiState.value.selectedEntryIds.toList()

        viewModelScope.launch {
            val result = mergeTimeEntries(selectedIds)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(
                        showMergeConfirmation = false,
                        isSelectionMode = false,
                        selectedEntryIds = emptySet()
                    )}
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

    /**
     * Cancel merge.
     */
    fun onMergeCancel() {
        _uiState.update { it.copy(showMergeConfirmation = false) }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
