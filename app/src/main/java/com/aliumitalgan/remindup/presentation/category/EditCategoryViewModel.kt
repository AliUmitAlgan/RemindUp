package com.aliumitalgan.remindup.presentation.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliumitalgan.remindup.domain.model.GoalCategory
import com.aliumitalgan.remindup.domain.usecase.goalcategory.DeleteGoalCategoryUseCase
import com.aliumitalgan.remindup.domain.usecase.goalcategory.GetGoalCategoryByIdUseCase
import com.aliumitalgan.remindup.domain.usecase.goalcategory.SaveGoalCategoryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditCategoryUiState(
    val loadedCategory: GoalCategory? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val error: String? = null
)

class EditCategoryViewModel(
    private val getGoalCategoryByIdUseCase: GetGoalCategoryByIdUseCase,
    private val saveGoalCategoryUseCase: SaveGoalCategoryUseCase,
    private val deleteGoalCategoryUseCase: DeleteGoalCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditCategoryUiState())
    val uiState: StateFlow<EditCategoryUiState> = _uiState.asStateFlow()

    fun loadCategory(categoryId: String?) {
        if (categoryId.isNullOrBlank()) {
            _uiState.update { it.copy(loadedCategory = null, isLoading = false, error = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getGoalCategoryByIdUseCase(categoryId)
                .onSuccess { category ->
                    _uiState.update {
                        it.copy(
                            loadedCategory = category,
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to load category"
                        )
                    }
                }
        }
    }

    fun saveCategory(category: GoalCategory, onSaved: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            saveGoalCategoryUseCase(category)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, error = null) }
                    onSaved()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = error.message ?: "Failed to save category"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun deleteCategory(categoryId: String, onDeleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            deleteGoalCategoryUseCase(categoryId)
                .onSuccess {
                    _uiState.update { it.copy(isDeleting = false, error = null) }
                    onDeleted()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            error = error.message ?: "Failed to delete category"
                        )
                    }
                }
        }
    }
}
