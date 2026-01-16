package com.maplume.blockwise.feature.timeentry.presentation.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.feature.timeentry.domain.model.TimerState
import com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype.GetActivityTypesUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.CheckTimerRecoveryUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.DiscardRecoverableTimerUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.DiscardTimerUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.GetTimerStateUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.PauseTimerUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.RecoverTimerUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.RecoverableTimer
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.ResumeTimerUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.StartTimerUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.StopTimerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the timer screen.
 */
data class TimerUiState(
    val activityTypes: List<ActivityType> = emptyList(),
    val selectedActivityId: Long? = null,
    val selectedTagIds: Set<Long> = emptySet(),
    val isLoading: Boolean = true,
    val showActivitySelector: Boolean = false,
    val recoverableTimer: RecoverableTimer? = null,
    val showRecoveryDialog: Boolean = false,
    val error: String? = null
)

/**
 * One-time events from the timer.
 */
sealed class TimerEvent {
    data class TimerStopped(val entryId: Long?) : TimerEvent()
    data class Error(val message: String) : TimerEvent()
    data object TimerDiscarded : TimerEvent()
}

/**
 * ViewModel for the timer functionality.
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val getActivityTypes: GetActivityTypesUseCase,
    private val getTimerState: GetTimerStateUseCase,
    private val startTimer: StartTimerUseCase,
    private val pauseTimer: PauseTimerUseCase,
    private val resumeTimer: ResumeTimerUseCase,
    private val stopTimer: StopTimerUseCase,
    private val discardTimer: DiscardTimerUseCase,
    private val checkTimerRecovery: CheckTimerRecoveryUseCase,
    private val recoverTimer: RecoverTimerUseCase,
    private val discardRecoverableTimer: DiscardRecoverableTimerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TimerEvent>()
    val events: SharedFlow<TimerEvent> = _events.asSharedFlow()

    /**
     * Current timer state from TimerManager.
     */
    val timerState: StateFlow<TimerState> = getTimerState.state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TimerState.Idle
        )

    /**
     * Elapsed time in milliseconds.
     */
    val elapsedMillis: StateFlow<Long> = getTimerState.elapsedMillis
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    init {
        loadActivityTypes()
        checkForRecovery()
    }

    private fun loadActivityTypes() {
        viewModelScope.launch {
            getActivityTypes(includeArchived = false)
                .collect { types ->
                    _uiState.update { it.copy(
                        activityTypes = types,
                        isLoading = false
                    )}
                }
        }
    }

    private fun checkForRecovery() {
        viewModelScope.launch {
            val recoverable = checkTimerRecovery()
            if (recoverable != null) {
                _uiState.update { it.copy(
                    recoverableTimer = recoverable,
                    showRecoveryDialog = true
                )}
            }
        }
    }

    /**
     * Select an activity type for the timer.
     */
    fun onActivitySelect(activityType: ActivityType) {
        _uiState.update { it.copy(
            selectedActivityId = activityType.id,
            showActivitySelector = false
        )}
    }

    /**
     * Toggle a tag selection.
     */
    fun onTagToggle(tagId: Long) {
        _uiState.update { state ->
            val newTagIds = if (tagId in state.selectedTagIds) {
                state.selectedTagIds - tagId
            } else {
                state.selectedTagIds + tagId
            }
            state.copy(selectedTagIds = newTagIds)
        }
    }

    /**
     * Show the activity selector.
     */
    fun showActivitySelector() {
        _uiState.update { it.copy(showActivitySelector = true) }
    }

    /**
     * Hide the activity selector.
     */
    fun hideActivitySelector() {
        _uiState.update { it.copy(showActivitySelector = false) }
    }

    /**
     * Start the timer with the selected activity.
     */
    fun onStartTimer() {
        val state = _uiState.value
        val activityId = state.selectedActivityId

        if (activityId == null) {
            _uiState.update { it.copy(showActivitySelector = true) }
            return
        }

        val activityType = state.activityTypes.find { it.id == activityId }
        if (activityType == null) {
            viewModelScope.launch {
                _events.emit(TimerEvent.Error("请选择活动类型"))
            }
            return
        }

        startTimer(
            activityId = activityType.id,
            activityName = activityType.name,
            activityColorHex = activityType.colorHex,
            tagIds = state.selectedTagIds.toList()
        )
    }

    /**
     * Start timer with a specific activity (quick start).
     */
    fun onQuickStart(activityType: ActivityType) {
        _uiState.update { it.copy(
            selectedActivityId = activityType.id,
            selectedTagIds = emptySet()
        )}

        startTimer(
            activityId = activityType.id,
            activityName = activityType.name,
            activityColorHex = activityType.colorHex,
            tagIds = emptyList()
        )
    }

    /**
     * Pause the timer.
     */
    fun onPauseTimer() {
        pauseTimer()
    }

    /**
     * Resume the timer.
     */
    fun onResumeTimer() {
        resumeTimer()
    }

    /**
     * Stop the timer and save the entry.
     */
    fun onStopTimer() {
        viewModelScope.launch {
            val result = stopTimer(createEntry = true)
            result.fold(
                onSuccess = { entryId ->
                    _uiState.update { it.copy(
                        selectedActivityId = null,
                        selectedTagIds = emptySet()
                    )}
                    _events.emit(TimerEvent.TimerStopped(entryId))
                },
                onFailure = { error ->
                    _events.emit(TimerEvent.Error(error.message ?: "保存失败"))
                }
            )
        }
    }

    /**
     * Discard the timer without saving.
     */
    fun onDiscardTimer() {
        discardTimer()
        _uiState.update { it.copy(
            selectedActivityId = null,
            selectedTagIds = emptySet()
        )}
        viewModelScope.launch {
            _events.emit(TimerEvent.TimerDiscarded)
        }
    }

    /**
     * Recover the timer from saved state.
     */
    fun onRecoverTimer() {
        recoverTimer()
        _uiState.update { it.copy(
            showRecoveryDialog = false,
            recoverableTimer = null
        )}
    }

    /**
     * Discard the recoverable timer.
     */
    fun onDiscardRecovery() {
        discardRecoverableTimer()
        _uiState.update { it.copy(
            showRecoveryDialog = false,
            recoverableTimer = null
        )}
    }

    /**
     * Dismiss the recovery dialog.
     */
    fun dismissRecoveryDialog() {
        _uiState.update { it.copy(showRecoveryDialog = false) }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
