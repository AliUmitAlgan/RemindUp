package com.aliumitalgan.remindup.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliumitalgan.remindup.domain.model.planner.DailyTask
import com.aliumitalgan.remindup.domain.model.planner.EnergyLevel
import com.aliumitalgan.remindup.domain.repository.AuthRepository
import com.aliumitalgan.remindup.domain.usecase.planner.ObserveTodayDailyTasksUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val userName: String = "Ali",
    val energyLevel: EnergyLevel = EnergyLevel.NORMAL,
    val todayTasks: List<DailyTask> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val observeTodayDailyTasksUseCase: ObserveTodayDailyTasksUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val selectedEnergy = MutableStateFlow(EnergyLevel.NORMAL)

    init {
        observeTodayTasks()
        loadUserName()
    }

    private fun observeTodayTasks() {
        viewModelScope.launch {
            selectedEnergy.flatMapLatest { energy ->
                observeTodayDailyTasksUseCase(
                    date = LocalDate.now(),
                    energyLevel = energy
                )
            }.collect { tasks ->
                _uiState.update {
                    it.copy(
                        energyLevel = selectedEnergy.value,
                        todayTasks = tasks,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    private fun loadUserName() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = authRepository.getCurrentUser()
            val userName = user?.name?.substringBefore(" ")?.ifBlank { null }
                ?: user?.email?.substringBefore("@")
                ?: "Ali"

            _uiState.update {
                it.copy(
                    userName = userName
                )
            }
        }
    }

    fun selectEnergy(level: EnergyLevel) {
        selectedEnergy.value = level
        _uiState.update { it.copy(energyLevel = level, isLoading = true) }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        selectedEnergy.value = selectedEnergy.value
    }
}
