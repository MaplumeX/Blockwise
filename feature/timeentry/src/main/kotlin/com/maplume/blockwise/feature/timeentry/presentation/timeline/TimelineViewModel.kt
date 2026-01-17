package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.DeleteTimeEntryUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.DayGroup
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.GetTimelineEntriesUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.MergeTimeEntriesUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.SplitTimeEntryUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.TimelineItem
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.createDayGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * UI state for the timeline screen.
 */
data class TimelineUiState(
    val dayGroups: List<DayGroup> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val error: String? = null,
    val selectedEntryIds: Set<Long> = emptySet(),
    val isSelectionMode: Boolean = false,
    val entryToDelete: TimeEntry? = null,
    val entryToSplit: TimeEntry? = null,
    val showMergeConfirmation: Boolean = false
)

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

/**
 * ViewModel for the timeline screen.
 */
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getTimelineEntries: GetTimelineEntriesUseCase,
    private val deleteTimeEntry: DeleteTimeEntryUseCase,
    private val splitTimeEntry: SplitTimeEntryUseCase,
    private val mergeTimeEntries: MergeTimeEntriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TimelineEvent>()
    val events: SharedFlow<TimelineEvent> = _events.asSharedFlow()

    private var currentOffset = 0
    private val pageSize = 50

    init {
        loadEntries()
    }

    /**
     * Load initial entries.
     */
    private fun loadEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            currentOffset = 0

            getTimelineEntries(limit = pageSize, offset = 0)
                .collect { dayGroups ->
                    _uiState.update { it.copy(
                        dayGroups = dayGroups,
                        isLoading = false,
                        hasMore = dayGroups.sumOf { group -> group.entryCount } >= pageSize
                    )}
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
     * Load more entries for pagination.
     */
    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.hasMore) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            currentOffset += pageSize

            getTimelineEntries(limit = pageSize, offset = currentOffset)
                .collect { newDayGroups ->
                    _uiState.update { currentState ->
                        // Merge new day groups with existing ones
                        val mergedGroups = mergeDayGroups(currentState.dayGroups, newDayGroups)
                        currentState.copy(
                            dayGroups = mergedGroups,
                            isLoadingMore = false,
                            hasMore = newDayGroups.sumOf { it.entryCount } >= pageSize
                        )
                    }
                }
        }
    }

    /**
     * Merge day groups, combining entries for the same date.
     */
    private fun mergeDayGroups(existing: List<DayGroup>, new: List<DayGroup>): List<DayGroup> {
        val groupMap = existing.associateBy { it.date }.toMutableMap()

        new.forEach { newGroup ->
            val existingGroup = groupMap[newGroup.date]
            if (existingGroup != null) {
                val existingEntries = existingGroup.items
                    .mapNotNull { it as? TimelineItem.Entry }
                    .map { it.entry }

                val newEntries = newGroup.items
                    .mapNotNull { it as? TimelineItem.Entry }
                    .map { it.entry }

                val mergedEntries = (existingEntries + newEntries)
                    .distinctBy { it.id }
                    .sortedByDescending { it.startTime }

                groupMap[newGroup.date] = createDayGroup(
                    date = newGroup.date,
                    entries = mergedEntries
                )
            } else {
                groupMap[newGroup.date] = newGroup
            }
        }

        return groupMap.values.sortedByDescending { it.date }
    }

    /**
     * Handle entry click - navigate to edit.
     */
    fun onEntryClick(entry: TimeEntry) {
        if (_uiState.value.isSelectionMode) {
            toggleEntrySelection(entry.id)
        } else {
            viewModelScope.launch {
                _events.emit(TimelineEvent.NavigateToEdit(entry.id))
            }
        }
    }

    /**
     * Handle entry long press - enter selection mode or show context menu.
     */
    fun onEntryLongPress(entry: TimeEntry) {
        _uiState.update { it.copy(
            isSelectionMode = true,
            selectedEntryIds = setOf(entry.id)
        )}
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
        _uiState.update { it.copy(
            isSelectionMode = false,
            selectedEntryIds = emptySet()
        )}
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
