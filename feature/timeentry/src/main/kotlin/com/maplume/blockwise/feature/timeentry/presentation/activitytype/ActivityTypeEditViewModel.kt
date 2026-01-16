package com.maplume.blockwise.feature.timeentry.presentation.activitytype

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype.CreateActivityTypeUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype.GetActivityTypeByIdUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype.UpdateActivityTypeUseCase
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
 * ViewModel for activity type edit screen.
 * Handles both create and edit modes.
 */
@HiltViewModel
class ActivityTypeEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getActivityTypeById: GetActivityTypeByIdUseCase,
    private val createActivityType: CreateActivityTypeUseCase,
    private val updateActivityType: UpdateActivityTypeUseCase
) : ViewModel() {

    private val activityTypeId: Long? = savedStateHandle.get<Long>("activityTypeId")?.takeIf { it > 0 }
    val isEditMode: Boolean = activityTypeId != null

    private val _uiState = MutableStateFlow(ActivityTypeEditUiState())
    val uiState: StateFlow<ActivityTypeEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<ActivityTypeEditEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        if (isEditMode) {
            loadActivityType()
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Load existing activity type for editing.
     */
    private fun loadActivityType() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val activityType = getActivityTypeById(activityTypeId!!)
            if (activityType != null) {
                _uiState.update {
                    it.copy(
                        name = activityType.name,
                        colorHex = activityType.colorHex,
                        icon = activityType.icon ?: "",
                        isLoading = false
                    )
                }
            } else {
                _events.send(ActivityTypeEditEvent.Error("活动类型不存在"))
                _events.send(ActivityTypeEditEvent.NavigateBack)
            }
        }
    }

    /**
     * Update name field.
     */
    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = null
            )
        }
    }

    /**
     * Update color field.
     */
    fun onColorChange(colorHex: String) {
        _uiState.update {
            it.copy(
                colorHex = colorHex,
                colorError = null
            )
        }
    }

    /**
     * Update icon field.
     */
    fun onIconChange(icon: String) {
        _uiState.update { it.copy(icon = icon) }
    }

    /**
     * Save activity type (create or update).
     */
    fun save() {
        val state = _uiState.value

        // Validate
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "名称不能为空") }
            return
        }

        if (state.colorHex.isBlank()) {
            _uiState.update { it.copy(colorError = "请选择颜色") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val result = if (isEditMode) {
                updateActivityType(
                    id = activityTypeId!!,
                    name = state.name.trim(),
                    colorHex = state.colorHex,
                    icon = state.icon.takeIf { it.isNotBlank() }
                )
            } else {
                createActivityType(
                    name = state.name.trim(),
                    colorHex = state.colorHex,
                    icon = state.icon.takeIf { it.isNotBlank() }
                )
            }

            result
                .onSuccess {
                    _events.send(ActivityTypeEditEvent.SaveSuccess)
                    _events.send(ActivityTypeEditEvent.NavigateBack)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    when {
                        error.message?.contains("名称") == true -> {
                            _uiState.update { it.copy(nameError = error.message) }
                        }
                        error.message?.contains("颜色") == true -> {
                            _uiState.update { it.copy(colorError = error.message) }
                        }
                        else -> {
                            _events.send(ActivityTypeEditEvent.Error(error.message ?: "保存失败"))
                        }
                    }
                }
        }
    }
}

/**
 * UI state for activity type edit screen.
 */
data class ActivityTypeEditUiState(
    val name: String = "",
    val colorHex: String = "#4CAF50",
    val icon: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val nameError: String? = null,
    val colorError: String? = null
) {
    val isValid: Boolean get() = name.isNotBlank() && colorHex.isNotBlank()
    val canSave: Boolean get() = isValid && !isSaving
}

/**
 * One-time events for activity type edit screen.
 */
sealed class ActivityTypeEditEvent {
    data object SaveSuccess : ActivityTypeEditEvent()
    data object NavigateBack : ActivityTypeEditEvent()
    data class Error(val message: String) : ActivityTypeEditEvent()
}
