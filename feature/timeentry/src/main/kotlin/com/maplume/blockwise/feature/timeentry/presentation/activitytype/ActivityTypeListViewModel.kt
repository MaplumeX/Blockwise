package com.maplume.blockwise.feature.timeentry.presentation.activitytype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype.DeleteActivityTypeUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype.GetActivityTypesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for activity type list screen.
 * Manages UI state and handles user interactions.
 */
@HiltViewModel
class ActivityTypeListViewModel @Inject constructor(
    private val getActivityTypes: GetActivityTypesUseCase,
    private val deleteActivityType: DeleteActivityTypeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityTypeListUiState())
    val uiState: StateFlow<ActivityTypeListUiState> = _uiState.asStateFlow()

    private val _events = Channel<ActivityTypeListEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadActivityTypes()
    }

    /**
     * Load activity types from repository.
     */
    private fun loadActivityTypes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getActivityTypes(includeArchived = false)
                .collect { types ->
                    _uiState.update {
                        it.copy(
                            activityTypes = types,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Refresh activity types list.
     */
    fun refresh() {
        loadActivityTypes()
    }

    /**
     * Request to delete an activity type.
     * Shows confirmation dialog first.
     */
    fun onDeleteRequest(activityType: ActivityType) {
        _uiState.update {
            it.copy(activityTypeToDelete = activityType)
        }
    }

    /**
     * Cancel delete operation.
     */
    fun onDeleteCancel() {
        _uiState.update {
            it.copy(activityTypeToDelete = null)
        }
    }

    /**
     * Confirm and execute delete operation.
     */
    fun onDeleteConfirm() {
        val activityType = _uiState.value.activityTypeToDelete ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(activityTypeToDelete = null) }

            deleteActivityType(activityType.id)
                .onSuccess {
                    _events.send(ActivityTypeListEvent.DeleteSuccess(activityType.name))
                }
                .onFailure { error ->
                    _events.send(
                        ActivityTypeListEvent.Error(error.message ?: "删除失败")
                    )
                }
        }
    }

    /**
     * Restore a deleted activity type.
     */
    fun onRestore(id: Long) {
        viewModelScope.launch {
            deleteActivityType.restore(id)
                .onSuccess {
                    _events.send(ActivityTypeListEvent.RestoreSuccess)
                }
                .onFailure { error ->
                    _events.send(
                        ActivityTypeListEvent.Error(error.message ?: "恢复失败")
                    )
                }
        }
    }
}

/**
 * UI state for activity type list screen.
 */
data class ActivityTypeListUiState(
    val activityTypes: List<ActivityType> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val activityTypeToDelete: ActivityType? = null
) {
    val isEmpty: Boolean get() = activityTypes.isEmpty() && !isLoading
    val showDeleteDialog: Boolean get() = activityTypeToDelete != null
}

/**
 * One-time events for activity type list screen.
 */
sealed class ActivityTypeListEvent {
    data class DeleteSuccess(val name: String) : ActivityTypeListEvent()
    data object RestoreSuccess : ActivityTypeListEvent()
    data class Error(val message: String) : ActivityTypeListEvent()
}
