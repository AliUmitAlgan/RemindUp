package com.aliumitalgan.remindup.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliumitalgan.remindup.domain.model.Goal
import com.aliumitalgan.remindup.domain.model.ReminderRecord
import com.aliumitalgan.remindup.domain.repository.AuthRepository
import com.aliumitalgan.remindup.domain.repository.ReminderRepository
import com.aliumitalgan.remindup.domain.usecase.goal.GetUserGoalsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val goals: List<Pair<String, Goal>> = emptyList(),
    val reminders: List<ReminderRecord> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val userName: String = "Maya"
)

class HomeViewModel(
    private val getUserGoalsUseCase: GetUserGoalsUseCase,
    private val reminderRepository: ReminderRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val user = authRepository.getCurrentUser()
            val userName = user?.name?.substringBefore(" ")?.ifBlank { null }
                ?: user?.email?.substringBefore("@")
                ?: "Maya"
            val goalsResult = getUserGoalsUseCase()
            val remindersResult = reminderRepository.getUserReminders()

            _uiState.update {
                it.copy(
                    goals = goalsResult.getOrDefault(emptyList()),
                    reminders = remindersResult.getOrDefault(emptyList()),
                    isLoading = false,
                    userName = userName,
                    error = goalsResult.exceptionOrNull()?.message ?: remindersResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun refresh() = loadData()
}
