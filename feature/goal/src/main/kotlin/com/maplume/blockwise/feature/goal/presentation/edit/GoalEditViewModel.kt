package com.maplume.blockwise.feature.goal.presentation.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalInput
import com.maplume.blockwise.core.domain.model.GoalPeriod
import com.maplume.blockwise.core.domain.model.GoalType
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.core.domain.repository.TagRepository
import com.maplume.blockwise.feature.goal.domain.usecase.CreateGoalUseCase
import com.maplume.blockwise.feature.goal.domain.usecase.GetGoalsUseCase
import com.maplume.blockwise.feature.goal.domain.usecase.UpdateGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * UI state for the goal edit screen.
 */
data class GoalEditUiState(
    val isEditMode: Boolean = false,
    val goalId: Long? = null,
    val availableTags: List<Tag> = emptyList(),
    val selectedTagId: Long? = null,
    val targetHours: Int = 1,
    val targetMinutes: Int = 0,
    val goalType: GoalType = GoalType.MIN,
    val period: GoalPeriod = GoalPeriod.WEEKLY,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val validationError: String? = null
) {
    val totalTargetMinutes: Int
        get() = targetHours * 60 + targetMinutes

    val isValid: Boolean
        get() = selectedTagId != null &&
                totalTargetMinutes > 0 &&
                (period != GoalPeriod.CUSTOM || (startDate != null && endDate != null && startDate < endDate))

    val selectedTag: Tag?
        get() = availableTags.find { it.id == selectedTagId }
}

/**
 * One-time events from the goal edit screen.
 */
sealed class GoalEditEvent {
    data object SaveSuccess : GoalEditEvent()
    data class Error(val message: String) : GoalEditEvent()
}

/**
 * ViewModel for the goal edit screen.
 */
@HiltViewModel
class GoalEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tagRepository: TagRepository,
    private val getGoals: GetGoalsUseCase,
    private val createGoal: CreateGoalUseCase,
    private val updateGoal: UpdateGoalUseCase
) : ViewModel() {

    private val goalId: Long? = savedStateHandle.get<Long>("goalId")?.takeIf { it > 0 }

    private val _uiState = MutableStateFlow(GoalEditUiState(
        isEditMode = goalId != null,
        goalId = goalId
    ))
    val uiState: StateFlow<GoalEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GoalEditEvent>()
    val events: SharedFlow<GoalEditEvent> = _events.asSharedFlow()

    init {
        loadData()
    }

    /**
     * Load tags and existing goal data if editing.
     */
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Load available tags
                val tags = tagRepository.getAllActive().first()
                _uiState.update { it.copy(availableTags = tags) }

                // Load existing goal if editing
                if (goalId != null) {
                    val goal = getGoals.getById(goalId)
                    if (goal != null) {
                        _uiState.update {
                            it.copy(
                                selectedTagId = goal.tagId,
                                targetHours = goal.targetMinutes / 60,
                                targetMinutes = goal.targetMinutes % 60,
                                goalType = goal.goalType,
                                period = goal.period,
                                startDate = goal.startDate,
                                endDate = goal.endDate,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                        _events.emit(GoalEditEvent.Error("目标不存在"))
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                _events.emit(GoalEditEvent.Error(e.message ?: "加载数据失败"))
            }
        }
    }

    /**
     * Select a tag.
     */
    fun onTagSelected(tagId: Long) {
        _uiState.update {
            it.copy(
                selectedTagId = tagId,
                validationError = null
            )
        }
    }

    /**
     * Update target hours.
     */
    fun onTargetHoursChanged(hours: Int) {
        _uiState.update {
            it.copy(
                targetHours = hours.coerceIn(0, 99),
                validationError = null
            )
        }
    }

    /**
     * Update target minutes.
     */
    fun onTargetMinutesChanged(minutes: Int) {
        _uiState.update {
            it.copy(
                targetMinutes = minutes.coerceIn(0, 59),
                validationError = null
            )
        }
    }

    /**
     * Select goal type.
     */
    fun onGoalTypeSelected(type: GoalType) {
        _uiState.update {
            it.copy(
                goalType = type,
                validationError = null
            )
        }
    }

    /**
     * Select period.
     */
    fun onPeriodSelected(period: GoalPeriod) {
        _uiState.update {
            it.copy(
                period = period,
                startDate = if (period == GoalPeriod.CUSTOM) it.startDate else null,
                endDate = if (period == GoalPeriod.CUSTOM) it.endDate else null,
                validationError = null
            )
        }
    }

    /**
     * Set start date for custom period.
     */
    fun onStartDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(
                startDate = date,
                validationError = null
            )
        }
    }

    /**
     * Set end date for custom period.
     */
    fun onEndDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(
                endDate = date,
                validationError = null
            )
        }
    }

    /**
     * Save the goal.
     */
    fun onSave() {
        val state = _uiState.value

        // Validate
        if (state.selectedTagId == null) {
            _uiState.update { it.copy(validationError = "请选择标签") }
            return
        }

        if (state.totalTargetMinutes <= 0) {
            _uiState.update { it.copy(validationError = "目标时长必须大于0") }
            return
        }

        if (state.period == GoalPeriod.CUSTOM) {
            if (state.startDate == null || state.endDate == null) {
                _uiState.update { it.copy(validationError = "请设置起止日期") }
                return
            }
            if (state.startDate >= state.endDate) {
                _uiState.update { it.copy(validationError = "结束日期必须晚于开始日期") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, validationError = null) }

            val input = GoalInput(
                tagId = state.selectedTagId,
                targetMinutes = state.totalTargetMinutes,
                goalType = state.goalType,
                period = state.period,
                startDate = state.startDate,
                endDate = state.endDate
            )

            val result = if (state.isEditMode && state.goalId != null) {
                updateGoal(state.goalId, input).map { state.goalId }
            } else {
                createGoal(input)
            }

            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(GoalEditEvent.SaveSuccess)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(GoalEditEvent.Error(error.message ?: "保存失败"))
                }
            )
        }
    }

    /**
     * Clear validation error.
     */
    fun clearValidationError() {
        _uiState.update { it.copy(validationError = null) }
    }
}
