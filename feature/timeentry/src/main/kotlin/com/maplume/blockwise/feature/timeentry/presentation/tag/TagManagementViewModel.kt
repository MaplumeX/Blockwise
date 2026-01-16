package com.maplume.blockwise.feature.timeentry.presentation.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.feature.timeentry.domain.usecase.tag.CreateTagUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.tag.DeleteTagUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.tag.GetTagsUseCase
import com.maplume.blockwise.feature.timeentry.domain.usecase.tag.UpdateTagUseCase
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
 * ViewModel for tag management screen.
 * Handles tag list display and CRUD operations.
 */
@HiltViewModel
class TagManagementViewModel @Inject constructor(
    private val getTags: GetTagsUseCase,
    private val createTag: CreateTagUseCase,
    private val updateTag: UpdateTagUseCase,
    private val deleteTag: DeleteTagUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagManagementUiState())
    val uiState: StateFlow<TagManagementUiState> = _uiState.asStateFlow()

    private val _events = Channel<TagManagementEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        loadTags()
    }

    /**
     * Load tags from repository.
     */
    private fun loadTags() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getTags(includeArchived = false)
                .collect { tags ->
                    _uiState.update {
                        it.copy(
                            tags = tags,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Show dialog to create a new tag.
     */
    fun onAddClick() {
        _uiState.update {
            it.copy(
                editingTag = null,
                dialogName = "",
                dialogColorHex = "#4CAF50",
                showEditDialog = true,
                dialogNameError = null
            )
        }
    }

    /**
     * Show dialog to edit an existing tag.
     */
    fun onEditClick(tag: Tag) {
        _uiState.update {
            it.copy(
                editingTag = tag,
                dialogName = tag.name,
                dialogColorHex = tag.colorHex,
                showEditDialog = true,
                dialogNameError = null
            )
        }
    }

    /**
     * Update dialog name field.
     */
    fun onDialogNameChange(name: String) {
        _uiState.update {
            it.copy(
                dialogName = name,
                dialogNameError = null
            )
        }
    }

    /**
     * Update dialog color field.
     */
    fun onDialogColorChange(colorHex: String) {
        _uiState.update { it.copy(dialogColorHex = colorHex) }
    }

    /**
     * Dismiss edit dialog.
     */
    fun onDialogDismiss() {
        _uiState.update {
            it.copy(
                showEditDialog = false,
                editingTag = null,
                dialogName = "",
                dialogColorHex = "#4CAF50",
                dialogNameError = null
            )
        }
    }

    /**
     * Save tag (create or update).
     */
    fun onDialogSave() {
        val state = _uiState.value

        if (state.dialogName.isBlank()) {
            _uiState.update { it.copy(dialogNameError = "名称不能为空") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            val result = if (state.editingTag != null) {
                updateTag(
                    id = state.editingTag.id,
                    name = state.dialogName.trim(),
                    colorHex = state.dialogColorHex
                )
            } else {
                createTag(
                    name = state.dialogName.trim(),
                    colorHex = state.dialogColorHex
                )
            }

            result
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showEditDialog = false,
                            editingTag = null,
                            dialogName = "",
                            dialogColorHex = "#4CAF50",
                            isSaving = false
                        )
                    }
                    _events.send(
                        if (state.editingTag != null) {
                            TagManagementEvent.UpdateSuccess
                        } else {
                            TagManagementEvent.CreateSuccess
                        }
                    )
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isSaving = false) }
                    if (error.message?.contains("名称") == true) {
                        _uiState.update { it.copy(dialogNameError = error.message) }
                    } else {
                        _events.send(TagManagementEvent.Error(error.message ?: "保存失败"))
                    }
                }
        }
    }

    /**
     * Request to delete a tag.
     */
    fun onDeleteRequest(tag: Tag) {
        _uiState.update { it.copy(tagToDelete = tag) }
    }

    /**
     * Cancel delete operation.
     */
    fun onDeleteCancel() {
        _uiState.update { it.copy(tagToDelete = null) }
    }

    /**
     * Confirm and execute delete operation.
     */
    fun onDeleteConfirm() {
        val tag = _uiState.value.tagToDelete ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(tagToDelete = null) }

            deleteTag(tag.id)
                .onSuccess {
                    _events.send(TagManagementEvent.DeleteSuccess(tag.name))
                }
                .onFailure { error ->
                    _events.send(TagManagementEvent.Error(error.message ?: "删除失败"))
                }
        }
    }
}

/**
 * UI state for tag management screen.
 */
data class TagManagementUiState(
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Edit dialog state
    val showEditDialog: Boolean = false,
    val editingTag: Tag? = null,
    val dialogName: String = "",
    val dialogColorHex: String = "#4CAF50",
    val dialogNameError: String? = null,
    val isSaving: Boolean = false,
    // Delete dialog state
    val tagToDelete: Tag? = null
) {
    val isEmpty: Boolean get() = tags.isEmpty() && !isLoading
    val isEditMode: Boolean get() = editingTag != null
    val showDeleteDialog: Boolean get() = tagToDelete != null
    val canSave: Boolean get() = dialogName.isNotBlank() && !isSaving
}

/**
 * One-time events for tag management screen.
 */
sealed class TagManagementEvent {
    data object CreateSuccess : TagManagementEvent()
    data object UpdateSuccess : TagManagementEvent()
    data class DeleteSuccess(val name: String) : TagManagementEvent()
    data class Error(val message: String) : TagManagementEvent()
}
