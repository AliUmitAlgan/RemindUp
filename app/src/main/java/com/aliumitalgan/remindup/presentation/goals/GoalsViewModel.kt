package com.aliumitalgan.remindup.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliumitalgan.remindup.domain.model.Goal
import com.aliumitalgan.remindup.domain.model.GoalCategory
import com.aliumitalgan.remindup.domain.usecase.goalcategory.GetGoalCategoriesUseCase
import com.aliumitalgan.remindup.domain.usecase.goalcategory.DeleteGoalCategoryUseCase
import com.aliumitalgan.remindup.domain.usecase.goal.AddGoalUseCase
import com.aliumitalgan.remindup.domain.usecase.goal.DeleteGoalUseCase
import com.aliumitalgan.remindup.domain.usecase.goal.GetUserGoalsUseCase
import com.aliumitalgan.remindup.domain.usecase.goal.UpdateGoalProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

const val GOALS_FILTER_ALL = "all"

data class GoalCelebrationEvent(
    val goalId: String,
    val goalTitle: String,
    val bonusXp: Int = 25
)

data class GoalsUiState(
    val goals: List<Pair<String, Goal>> = emptyList(),
    val categories: List<GoalCategory> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedFilterId: String = GOALS_FILTER_ALL,
    val celebrationEvent: GoalCelebrationEvent? = null
)

class GoalsViewModel(
    private val getUserGoalsUseCase: GetUserGoalsUseCase,
    private val addGoalUseCase: AddGoalUseCase,
    private val updateGoalProgressUseCase: UpdateGoalProgressUseCase,
    private val deleteGoalUseCase: DeleteGoalUseCase,
    private val getGoalCategoriesUseCase: GetGoalCategoriesUseCase,
    private val deleteGoalCategoryUseCase: DeleteGoalCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState: StateFlow<GoalsUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
        loadCategories()
    }

    fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = false, error = null) }
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

    fun addGoal(
        title: String,
        description: String,
        categoryId: String = "",
        dueDate: String = "",
        reminderTime: String = "",
        smartReminderEnabled: Boolean = true,
        onGoalSaved: ((String, Goal) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val goal = Goal(
                title = title,
                description = description,
                progress = 0,
                categoryId = categoryId,
                category = 0,
                dueDate = dueDate,
                reminderTime = reminderTime,
                smartReminderEnabled = smartReminderEnabled
            )
            addGoalUseCase(goal)
                .onSuccess { goalId ->
                    val savedGoal = goal.copy(
                        id = goalId,
                        createdAt = System.currentTimeMillis()
                    )
                    _uiState.update { state ->
                        state.copy(
                            goals = listOf(goalId to savedGoal) + state.goals,
                            error = null
                        )
                    }
                    onGoalSaved?.invoke(goalId, savedGoal)
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            getGoalCategoriesUseCase()
                .onSuccess { categories ->
                    _uiState.update { state ->
                        val selected = if (
                            state.selectedFilterId == GOALS_FILTER_ALL ||
                            categories.any { it.id == state.selectedFilterId }
                        ) {
                            state.selectedFilterId
                        } else {
                            GOALS_FILTER_ALL
                        }
                        state.copy(categories = categories, selectedFilterId = selected)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to load categories") }
                }
        }
    }

    fun updateProgress(goalId: String, newProgress: Int) {
        val previousGoal = _uiState.value.goals.firstOrNull { it.first == goalId }?.second

        viewModelScope.launch {
            updateGoalProgressUseCase(goalId, newProgress)
                .onSuccess {
                    val celebrationEvent = if (
                        previousGoal != null &&
                        previousGoal.progress < 100 &&
                        newProgress >= 100 &&
                        isCompletedAheadOfDeadline(previousGoal)
                    ) {
                        GoalCelebrationEvent(
                            goalId = goalId,
                            goalTitle = previousGoal.title.ifBlank { "Goal" }
                        )
                    } else {
                        null
                    }

                    _uiState.update { state ->
                        state.copy(
                            goals = state.goals.map { (id, goal) ->
                                if (id == goalId) {
                                    id to goal.copy(progress = newProgress)
                                } else {
                                    id to goal
                                }
                            },
                            celebrationEvent = celebrationEvent ?: state.celebrationEvent,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            deleteGoalUseCase(goalId)
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            goals = state.goals.filterNot { it.first == goalId },
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            deleteGoalCategoryUseCase(categoryId)
                .onSuccess {
                    _uiState.update { state ->
                        val remainingCategories = state.categories.filterNot { it.id == categoryId }
                        val nextFilter = when {
                            state.selectedFilterId == GOALS_FILTER_ALL -> GOALS_FILTER_ALL
                            remainingCategories.any { it.id == state.selectedFilterId } -> state.selectedFilterId
                            else -> GOALS_FILTER_ALL
                        }
                        state.copy(
                            categories = remainingCategories,
                            selectedFilterId = nextFilter,
                            error = null
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to delete category") }
                }
        }
    }

    fun setFilter(filterId: String) {
        _uiState.update { it.copy(selectedFilterId = filterId) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun consumeCelebrationEvent() {
        _uiState.update { it.copy(celebrationEvent = null) }
    }

    private fun isCompletedAheadOfDeadline(goal: Goal): Boolean {
        val dueDateRaw = goal.dueDate.trim()
        if (dueDateRaw.isEmpty()) {
            return false
        }

        val dueDate = parseDate(dueDateRaw) ?: return false

        val dueTime = parseTime(goal.reminderTime) ?: LocalTime.of(23, 59)
        val deadline = LocalDateTime.of(dueDate, dueTime)
        return LocalDateTime.now().isBefore(deadline)
    }

    private fun parseDate(value: String): LocalDate? {
        val raw = value.trim()
        if (raw.isEmpty()) {
            return null
        }

        runCatching {
            LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrNull()?.let { return it }

        when (raw.lowercase(Locale.getDefault())) {
            "today" -> return LocalDate.now()
            "tomorrow" -> return LocalDate.now().plusDays(1)
        }

        val patterns = listOf("dd MMM yyyy", "d MMM yyyy")
        for (pattern in patterns) {
            val parsed = runCatching {
                LocalDate.parse(raw, DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))
            }.getOrNull()
            if (parsed != null) {
                return parsed
            }
        }
        return null
    }

    private fun parseTime(value: String): LocalTime? {
        val raw = value.trim()
        if (raw.isEmpty()) {
            return null
        }
        val formats = listOf("HH:mm", "hh:mm a")
        for (pattern in formats) {
            val parsed = runCatching {
                LocalTime.parse(raw, DateTimeFormatter.ofPattern(pattern))
            }.getOrNull()
            if (parsed != null) {
                return parsed
            }
        }
        return null
    }
}
