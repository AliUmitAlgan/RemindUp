package com.aliumitalgan.remindup.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliumitalgan.remindup.domain.model.Goal
import com.aliumitalgan.remindup.domain.usecase.goal.AddGoalUseCase
import com.aliumitalgan.remindup.domain.usecase.goal.DeleteGoalUseCase
import com.aliumitalgan.remindup.domain.usecase.goal.GetUserGoalsUseCase
import com.aliumitalgan.remindup.domain.usecase.goal.UpdateGoalProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GoalsUiState(
    val goals: List<Pair<String, Goal>> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilter: String = "All Goals"
)

class GoalsViewModel(
    private val getUserGoalsUseCase: GetUserGoalsUseCase,
    private val addGoalUseCase: AddGoalUseCase,
    private val updateGoalProgressUseCase: UpdateGoalProgressUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getUserGoalsUseCase()
                .onSuccess { goals ->
                    _uiState.update { it.copy(goals = goals, isLoading = false) }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load goals"
                        )
                    }
                }
        }
    }

    fun addGoal(title: String, description: String, category: Int) {
        viewModelScope.launch {
            val goal = Goal(
                title = title,
                description = description,
                progress = 0,
                category = category
            )
            addGoalUseCase(goal)
                .onSuccess { loadGoals() }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun updateProgress(goalId: String, newProgress: Int) {
        viewModelScope.launch {
            updateGoalProgressUseCase(goalId, newProgress)
                .onSuccess { loadGoals() }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            deleteGoalUseCase(goalId)
                .onSuccess { loadGoals() }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun setFilter(filter: String) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
