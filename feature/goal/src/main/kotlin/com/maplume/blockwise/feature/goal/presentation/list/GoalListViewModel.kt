package com.maplume.blockwise.feature.goal.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalProgress
import com.maplume.blockwise.feature.goal.domain.usecase.CalculateGoalProgressUseCase
import com.maplume.blockwise.feature.goal.domain.usecase.DeleteGoalUseCase
import com.maplume.blockwise.feature.goal.domain.usecase.GetGoalsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the goal list screen.
 */
data class GoalListUiState(
    val goalProgressList: List<GoalProgress> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showArchived: Boolean = false,
    val goalToDelete: Goal? = null
)

/**
 * One-time events from the goal list.
 */
sealed class GoalListEvent {
    data class NavigateToEdit(val goalId: Long) : GoalListEvent()
    data object NavigateToAdd : GoalListEvent()
    data class NavigateToDetail(val goalId: Long) : GoalListEvent()
    data class Error(val message: String) : GoalListEvent()
    data object DeleteSuccess : GoalListEvent()
    data object ArchiveSuccess : GoalListEvent()
}

/**
 * ViewModel for the goal list screen.
 */
@HiltViewModel
class GoalListViewModel @Inject constructor(
    private val getGoals: GetGoalsUseCase,
    private val calculateProgress: CalculateGoalProgressUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalListUiState())
    val uiState: StateFlow<GoalListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GoalListEvent>()
    val events: SharedFlow<GoalListEvent> = _events.asSharedFlow()

    init {
        loadGoals()
    }

    /**
     * Load goals based on current filter.
     */
    private fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val goalsFlow = if (_uiState.value.showArchived) {
                getGoals.getAllGoals()
            } else {
                getGoals.getActiveGoals()
            }

            goalsFlow.collect { goals ->
                try {
                    val progressList = calculateProgress.calculateAll(goals)
                    _uiState.update {
                        it.copy(
                            goalProgressList = progressList,
                            isLoading = false,
                            error = null
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "加载目标失败"
                        )
                    }
                }
            }
        }
    }

    /**
     * Refresh the goal list.
     */
    fun refresh() {
        loadGoals()
    }

    /**
     * Toggle showing archived goals.
     */
    fun toggleShowArchived() {
        _uiState.update { it.copy(showArchived = !it.showArchived) }
        loadGoals()
    }

    /**
     * Navigate to add goal screen.
     */
    fun onAddClick() {
        viewModelScope.launch {
            _events.emit(GoalListEvent.NavigateToAdd)
        }
    }

    /**
     * Navigate to goal detail screen.
     */
    fun onGoalClick(goal: Goal) {
        viewModelScope.launch {
            _events.emit(GoalListEvent.NavigateToDetail(goal.id))
        }
    }

    /**
     * Navigate to edit goal screen.
     */
    fun onEditClick(goal: Goal) {
        viewModelScope.launch {
            _events.emit(GoalListEvent.NavigateToEdit(goal.id))
        }
    }

    /**
     * Request to delete/archive a goal.
     */
    fun onDeleteRequest(goal: Goal) {
        _uiState.update { it.copy(goalToDelete = goal) }
    }

    /**
     * Cancel delete dialog.
     */
    fun onDeleteCancel() {
        _uiState.update { it.copy(goalToDelete = null) }
    }

    /**
     * Confirm archive (soft delete).
     */
    fun onArchiveConfirm() {
        val goal = _uiState.value.goalToDelete ?: return

        viewModelScope.launch {
            val result = deleteGoalUseCase.archive(goal.id)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(goalToDelete = null) }
                    _events.emit(GoalListEvent.ArchiveSuccess)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(goalToDelete = null) }
                    _events.emit(GoalListEvent.Error(error.message ?: "归档失败"))
                }
            )
        }
    }

    /**
     * Confirm permanent delete.
     */
    fun onDeleteConfirm() {
        val goal = _uiState.value.goalToDelete ?: return

        viewModelScope.launch {
            val result = deleteGoalUseCase.delete(goal.id)
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(goalToDelete = null) }
                    _events.emit(GoalListEvent.DeleteSuccess)
                },
                onFailure = { error ->
                    _uiState.update { it.copy(goalToDelete = null) }
                    _events.emit(GoalListEvent.Error(error.message ?: "删除失败"))
                }
            )
        }
    }

    /**
     * Restore an archived goal.
     */
    fun onRestoreClick(goal: Goal) {
        viewModelScope.launch {
            val result = deleteGoalUseCase.restore(goal.id)
            result.fold(
                onSuccess = {
                    // Goal list will auto-refresh via Flow
                },
                onFailure = { error ->
                    _events.emit(GoalListEvent.Error(error.message ?: "恢复失败"))
                }
            )
        }
    }

    /**
     * Clear error state.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
