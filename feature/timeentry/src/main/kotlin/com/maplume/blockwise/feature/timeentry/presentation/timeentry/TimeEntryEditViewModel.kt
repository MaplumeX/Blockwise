package com.maplume.blockwise.feature.timeentry.presentation.timeentry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.core.domain.model.TimeEntryInput
import com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype.GetActivityTypesUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.tag.GetTagsUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.CreateTimeEntryUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.GetTimeEntryByIdUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry.UpdateTimeEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * ViewModel for time entry edit screen.
 * Handles both create and edit modes.
 */
@HiltViewModel
class TimeEntryEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTimeEntryById: GetTimeEntryByIdUseCase,
    private val createTimeEntry: CreateTimeEntryUseCase,
    private val updateTimeEntry: UpdateTimeEntryUseCase,
    private val getActivityTypes: GetActivityTypesUseCase,
    private val getTags: GetTagsUseCase
) : ViewModel() {

    private val entryId: Long? = savedStateHandle.get<Long>("entryId")?.takeIf { it > 0 }
    val isEditMode: Boolean = entryId != null

    private val _uiState = MutableStateFlow(TimeEntryEditUiState())
    val uiState: StateFlow<TimeEntryEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<TimeEntryEditEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadData()
    }

    /**
     * Load activity types, tags, and existing entry if editing.
     */
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load activity types and tags
            combine(
                getActivityTypes(includeArchived = false),
                getTags(includeArchived = false)
            ) { activities, tags ->
                Pair(activities, tags)
            }.collect { (activities, tags) ->
                _uiState.update {
                    it.copy(
                        activityTypes = activities,
                        availableTags = tags,
                        isLoading = false
                    )
                }
            }
        }

        // Load existing entry if editing
        if (entryId != null) {
            viewModelScope.launch {
                val entry = getTimeEntryById(entryId)
                if (entry != null) {
                    val tz = TimeZone.currentSystemDefault()
                    val startDateTime = entry.startTime.toLocalDateTime(tz)
                    val endDateTime = entry.endTime.toLocalDateTime(tz)

                    _uiState.update {
                        it.copy(
                            selectedActivityId = entry.activity.id,
                            selectedDate = startDateTime.date,
                            startTime = startDateTime.time,
                            endTime = endDateTime.time,
                            note = entry.note ?: "",
                            selectedTagIds = entry.tags.map { tag -> tag.id }.toSet()
                        )
                    }
                } else {
                    _events.send(TimeEntryEditEvent.Error("时间记录不存在"))
                    _events.send(TimeEntryEditEvent.NavigateBack)
                }
            }
        } else {
            // Set default values for new entry
            val now = Clock.System.now()
            val tz = TimeZone.currentSystemDefault()
            val nowDateTime = now.toLocalDateTime(tz)

            val alignedTime = LocalTime(nowDateTime.hour, nowDateTime.minute)

            _uiState.update {
                it.copy(
                    selectedDate = nowDateTime.date,
                    startTime = alignedTime,
                    endTime = alignedTime
                )
            }
        }
    }

    /**
     * Select an activity type.
     */
    fun onActivityTypeSelect(activityType: ActivityType) {
        _uiState.update {
            it.copy(
                selectedActivityId = activityType.id,
                activityTypeError = null
            )
        }
    }

    /**
     * Update selected date.
     */
    fun onDateChange(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                timeError = null
            )
        }
    }


    fun onStartTimeChange(time: LocalTime) {
        _uiState.update { state ->
            val existing = state.startTime
            val resolved = if (
                existing != null &&
                    time.second == 0 &&
                    time.nanosecond == 0
            ) {
                LocalTime(time.hour, time.minute, existing.second, existing.nanosecond)
            } else {
                time
            }

            state.copy(
                startTime = resolved,
                timeError = null
            )
        }
    }


    fun onEndTimeChange(time: LocalTime) {
        _uiState.update { state ->
            val existing = state.endTime
            val resolved = if (
                existing != null &&
                    time.second == 0 &&
                    time.nanosecond == 0
            ) {
                LocalTime(time.hour, time.minute, existing.second, existing.nanosecond)
            } else {
                time
            }

            state.copy(
                endTime = resolved,
                timeError = null
            )
        }
    }

    /**
     * Update note.
     */
    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    /**
     * Toggle tag selection.
     */
    fun onTagToggle(tagId: Long) {
        _uiState.update { state ->
            val newSelectedTags = if (tagId in state.selectedTagIds) {
                state.selectedTagIds - tagId
            } else {
                state.selectedTagIds + tagId
            }
            state.copy(selectedTagIds = newSelectedTags)
        }
    }

    /**
     * Save time entry (create or update).
     */
    fun save() {
        val state = _uiState.value

        // Validate activity type
        if (state.selectedActivityId == null) {
            _uiState.update { it.copy(activityTypeError = "请选择活动类型") }
            return
        }

        // Validate time
        if (state.selectedDate == null || state.startTime == null || state.endTime == null) {
            _uiState.update { it.copy(timeError = "请选择日期和时间") }
            return
        }

        val tz = TimeZone.currentSystemDefault()
        val startInstant = state.selectedDate.atTime(state.startTime).toInstant(tz)
        var endInstant = state.selectedDate.atTime(state.endTime).toInstant(tz)

        // Handle overnight entries (end time is before start time)
        if (endInstant <= startInstant) {
            // Assume end time is on the next day
            val nextDay = state.selectedDate.toEpochDays() + 1
            val nextDate = LocalDate.fromEpochDays(nextDay.toInt())
            endInstant = nextDate.atTime(state.endTime).toInstant(tz)
        }

        if (startInstant >= endInstant) {
            _uiState.update { it.copy(timeError = "结束时间必须晚于开始时间") }
            return
        }

        val input = TimeEntryInput(
            activityId = state.selectedActivityId,
            startTime = startInstant,
            endTime = endInstant,
            note = state.note.takeIf { it.isNotBlank() },
            tagIds = state.selectedTagIds.toList()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val result = if (isEditMode) {
                updateTimeEntry(entryId!!, input)
            } else {
                createTimeEntry(input)
            }

            result
                .onSuccess {
                    _events.send(TimeEntryEditEvent.SaveSuccess)
                    _events.send(TimeEntryEditEvent.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    when {
                        error.message?.contains("时间") == true ||
                        error.message?.contains("时长") == true -> {
                            _uiState.update { it.copy(timeError = error.message) }
                        }
                        error.message?.contains("活动") == true -> {
                            _uiState.update { it.copy(activityTypeError = error.message) }
                        }
                        else -> {
                            _events.send(TimeEntryEditEvent.Error(error.message ?: "保存失败"))
                        }
                    }
                }
        }
    }
}

/**
 * UI state for time entry edit screen.
 */
data class TimeEntryEditUiState(
    val activityTypes: List<ActivityType> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val selectedActivityId: Long? = null,
    val selectedDate: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val note: String = "",
    val selectedTagIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val activityTypeError: String? = null,
    val timeError: String? = null
) {
    val selectedActivityType: ActivityType?
        get() = activityTypes.find { it.id == selectedActivityId }

    val isValid: Boolean
        get() = selectedActivityId != null &&
                selectedDate != null &&
                startTime != null &&
                endTime != null

    val canSave: Boolean
        get() = isValid && !isSaving

    /**
     * Calculate duration in minutes.
     */
    val durationMinutes: Int?
        get() {
            if (startTime == null || endTime == null) return null
            val startMinutes = startTime.hour * 60 + startTime.minute
            var endMinutes = endTime.hour * 60 + endTime.minute
            if (endMinutes <= startMinutes) {
                endMinutes += 24 * 60 // Next day
            }
            return endMinutes - startMinutes
        }

    /**
     * Format duration for display.
     */
    val formattedDuration: String?
        get() {
            val minutes = durationMinutes ?: return null
            val hours = minutes / 60
            val mins = minutes % 60
            return when {
                hours > 0 && mins > 0 -> "${hours}小时${mins}分钟"
                hours > 0 -> "${hours}小时"
                else -> "${mins}分钟"
            }
        }
}

/**
 * One-time events for time entry edit screen.
 */
sealed class TimeEntryEditEvent {
    data object SaveSuccess : TimeEntryEditEvent()
    data object NavigateBack : TimeEntryEditEvent()
    data class Error(val message: String) : TimeEntryEditEvent()
}
