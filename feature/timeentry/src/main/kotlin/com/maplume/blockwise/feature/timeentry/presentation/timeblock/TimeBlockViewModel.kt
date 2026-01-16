package com.maplume.blockwise.feature.timeentry.presentation.timeblock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.DeleteTimeEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
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
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * View mode for the time block display.
 */
enum class TimeBlockViewMode {
    DAY,
    WEEK
}

/**
 * UI state for the time block screen.
 */
data class TimeBlockUiState(
    val selectedDate: LocalDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault()).date,
    val viewMode: TimeBlockViewMode = TimeBlockViewMode.DAY,
    val entriesByDay: Map<LocalDate, List<TimeEntry>> = emptyMap(),
    val isLoading: Boolean = true,
    val totalMinutes: Int = 0,
    val entryCount: Int = 0,
    val entryToDelete: TimeEntry? = null,
    val showDatePicker: Boolean = false
) {
    /**
     * Get the week start date (Monday) for the selected date.
     */
    val weekStartDate: LocalDate
        get() {
            val dayOfWeek = selectedDate.dayOfWeek
            val daysFromMonday = dayOfWeek.ordinal
            return selectedDate.minus(daysFromMonday, DateTimeUnit.DAY)
        }

    /**
     * Get entries for the selected date (day view).
     */
    val selectedDayEntries: List<TimeEntry>
        get() = entriesByDay[selectedDate] ?: emptyList()

    /**
     * Format total duration for display.
     */
    val formattedTotalDuration: String
        get() {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return when {
                hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
                hours > 0 -> "${hours}小时"
                minutes > 0 -> "${minutes}分钟"
                else -> "0分钟"
            }
        }
}

/**
 * One-time events from the time block screen.
 */
sealed class TimeBlockEvent {
    data class NavigateToEdit(val entryId: Long) : TimeBlockEvent()
    data class NavigateToCreate(val date: LocalDate, val time: LocalTime?) : TimeBlockEvent()
    data class Error(val message: String) : TimeBlockEvent()
    data object DeleteSuccess : TimeBlockEvent()
}

/**
 * ViewModel for the time block screen.
 */
@HiltViewModel
class TimeBlockViewModel @Inject constructor(
    private val repository: TimeEntryRepository,
    private val deleteTimeEntry: DeleteTimeEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimeBlockUiState())
    val uiState: StateFlow<TimeBlockUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TimeBlockEvent>()
    val events: SharedFlow<TimeBlockEvent> = _events.asSharedFlow()

    init {
        loadEntries()
    }

    /**
     * Load entries for the current view (day or week).
     */
    private fun loadEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val state = _uiState.value
            val dates = when (state.viewMode) {
                TimeBlockViewMode.DAY -> listOf(state.selectedDate)
                TimeBlockViewMode.WEEK -> (0..6).map { state.weekStartDate.plus(it, DateTimeUnit.DAY) }
            }

            // Collect entries for all dates
            val entriesMap = mutableMapOf<LocalDate, List<TimeEntry>>()
            var totalMinutes = 0
            var entryCount = 0

            dates.forEach { date ->
                repository.getByDay(date).collect { entries ->
                    entriesMap[date] = entries.sortedBy { it.startTime }
                    totalMinutes += entries.sumOf { it.durationMinutes }
                    entryCount += entries.size
                }
            }

            _uiState.update {
                it.copy(
                    entriesByDay = entriesMap,
                    isLoading = false,
                    totalMinutes = totalMinutes,
                    entryCount = entryCount
                )
            }
        }
    }

    /**
     * Refresh data.
     */
    fun refresh() {
        loadEntries()
    }

    /**
     * Navigate to the previous day/week.
     */
    fun navigatePrevious() {
        val state = _uiState.value
        val newDate = when (state.viewMode) {
            TimeBlockViewMode.DAY -> state.selectedDate.minus(1, DateTimeUnit.DAY)
            TimeBlockViewMode.WEEK -> state.selectedDate.minus(7, DateTimeUnit.DAY)
        }
        _uiState.update { it.copy(selectedDate = newDate) }
        loadEntries()
    }

    /**
     * Navigate to the next day/week.
     */
    fun navigateNext() {
        val state = _uiState.value
        val newDate = when (state.viewMode) {
            TimeBlockViewMode.DAY -> state.selectedDate.plus(1, DateTimeUnit.DAY)
            TimeBlockViewMode.WEEK -> state.selectedDate.plus(7, DateTimeUnit.DAY)
        }
        _uiState.update { it.copy(selectedDate = newDate) }
        loadEntries()
    }

    /**
     * Navigate to today.
     */
    fun navigateToToday() {
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        _uiState.update { it.copy(selectedDate = today) }
        loadEntries()
    }

    /**
     * Select a specific date.
     */
    fun selectDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date, showDatePicker = false) }
        loadEntries()
    }

    /**
     * Toggle view mode between day and week.
     */
    fun toggleViewMode() {
        val newMode = when (_uiState.value.viewMode) {
            TimeBlockViewMode.DAY -> TimeBlockViewMode.WEEK
            TimeBlockViewMode.WEEK -> TimeBlockViewMode.DAY
        }
        _uiState.update { it.copy(viewMode = newMode) }
        loadEntries()
    }

    /**
     * Set view mode.
     */
    fun setViewMode(mode: TimeBlockViewMode) {
        if (_uiState.value.viewMode != mode) {
            _uiState.update { it.copy(viewMode = mode) }
            loadEntries()
        }
    }

    /**
     * Show date picker dialog.
     */
    fun showDatePicker() {
        _uiState.update { it.copy(showDatePicker = true) }
    }

    /**
     * Hide date picker dialog.
     */
    fun hideDatePicker() {
        _uiState.update { it.copy(showDatePicker = false) }
    }

    /**
     * Handle time block click - navigate to edit.
     */
    fun onEntryClick(entry: TimeEntry) {
        viewModelScope.launch {
            _events.emit(TimeBlockEvent.NavigateToEdit(entry.id))
        }
    }

    /**
     * Handle empty slot click - navigate to create with pre-filled time.
     */
    fun onEmptySlotClick(date: LocalDate, time: LocalTime) {
        viewModelScope.launch {
            _events.emit(TimeBlockEvent.NavigateToCreate(date, time))
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
                    _events.emit(TimeBlockEvent.DeleteSuccess)
                    refresh()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(entryToDelete = null) }
                    _events.emit(TimeBlockEvent.Error(error.message ?: "删除失败"))
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
}
