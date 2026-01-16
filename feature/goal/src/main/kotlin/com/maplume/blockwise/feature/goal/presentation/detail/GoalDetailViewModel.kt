package com.maplume.blockwise.feature.goal.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalProgress
import com.maplume.blockwise.core.domain.repository.StatisticsRepository
import com.maplume.blockwise.feature.goal.domain.usecase.CalculateGoalProgressUseCase
import com.maplume.blockwise.feature.goal.domain.usecase.DeleteGoalUseCase
import com.maplume.blockwise.feature.goal.domain.usecase.GetGoalsUseCase
import com.maplume.blockwise.feature.goal.domain.usecase.currentPeriodRange
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
import javax.inject.Inject

/**
 * UI state for the goal detail screen.
 */
data class GoalDetailUiState(
    val goal: Goal? = null,
    val progress: GoalProgress? = null,
    val dailyTrends: List<DailyTrend> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteDialog: Boolean = false
)

/**
 * One-time events from the goal detail screen.
 */
sealed class GoalDetailEvent {
    data object NavigateBack : GoalDetailEvent()
    data class NavigateToEdit(val goalId: Long) : GoalDetailEvent()
    data class Error(val message: String) : GoalDetailEvent()
    data object DeleteSuccess : GoalDetailEvent()
}

/**
 * ViewModel for the goal detail screen.
 */
@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getGoals: GetGoalsUseCase,
    private val calculateProgress: CalculateGoalProgressUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val goalId: Long = savedStateHandle.get<Long>("goalId") ?: 0L

    private val _uiState = MutableStateFlow(GoalDetailUiState())
    val uiState: StateFlow<GoalDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GoalDetailEvent>()
    val events: SharedFlow<GoalDetailEvent> = _events.asSharedFlow()

    init {
        loadGoalDetail()
    }

    /**
     * Load goal details and trends.
     */
    private fun loadGoalDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Load goal
                val goal = getGoals.getById(goalId)
                if (goal == null) {
                    _uiState.update { it.copy(isLoading = false, error = "目标不存在") }
                    return@launch
                }

                // Calculate progress
                val progress = calculateProgress(goal)

                // Load daily trends for the current period
                val (startTime, endTime) = goal.currentPeriodRange()
                val trends = statisticsRepository.getDailyTrendsForTag(
                    tagId = goal.tagId,
                    startTime = startTime,
                    endTime = endTime
                ).first()

                _uiState.update {
                    it.copy(
                        goal = goal,
                        progress = progress,
                        dailyTrends = trends,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "加载失败"
                    )
                }
            }
        }
    }

    /**
     * Refresh goal data.
     */
    fun refresh() {
        loadGoalDetail()
    }

    /**
     * Navigate to edit screen.
     */
    fun onEditClick() {
        viewModelScope.launch {
            _events.emit(GoalDetailEvent.NavigateToEdit(goalId))
        }
    }

    /**
     * Show delete confirmation dialog.
     */
    fun onDeleteClick() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    /**
     * Cancel delete dialog.
     */
    fun onDeleteCancel() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    /**
     * Confirm archive (for active goals).
     */
    fun onArchiveConfirm() {
        viewModelScope.launch {
            val result = deleteGoalUseCase.archive(goalId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(showDeleteDialog = false) }
                    _events.emit(GoalDetailEvent.DeleteSuccess)
                    _events.emit(GoalDetailEvent.NavigateBack)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(showDeleteDialog = false) }
                    _events.emit(GoalDetailEvent.Error(error.message ?: "归档失败"))
                }
            )
        }
    }

    /**
     * Confirm permanent delete (for archived goals).
     */
    fun onDeleteConfirm() {
        viewModelScope.launch {
            val result = deleteGoalUseCase.delete(goalId)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(showDeleteDialog = false) }
                    _events.emit(GoalDetailEvent.DeleteSuccess)
                    _events.emit(GoalDetailEvent.NavigateBack)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(showDeleteDialog = false) }
                    _events.emit(GoalDetailEvent.Error(error.message ?: "删除失败"))
                }
            )
        }
    }
}
