package com.aliumitalgan.remindup.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aliumitalgan.remindup.core.di.AppContainer
import com.aliumitalgan.remindup.domain.model.FeatureGate
import com.aliumitalgan.remindup.domain.model.TaskBreakdownResult
import com.aliumitalgan.remindup.domain.repository.AiAssistantRepository
import com.aliumitalgan.remindup.domain.service.RuleBasedFallbackService
import com.aliumitalgan.remindup.domain.usecase.EvaluateFeatureAccessUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class AiAssistantViewModel(
    private val aiAssistantRepository: AiAssistantRepository,
    private val accessUseCase: EvaluateFeatureAccessUseCase,
    private val fallbackService: RuleBasedFallbackService
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState.asStateFlow()

    fun onEvent(event: AiAssistantUiEvent) {
        when (event) {
            is AiAssistantUiEvent.GoalChanged -> {
                _uiState.value = _uiState.value.copy(goalInput = event.value)
            }

            AiAssistantUiEvent.GenerateClicked -> generate()
            AiAssistantUiEvent.ClearMessage -> {
                _uiState.value = _uiState.value.copy(message = null)
            }
        }
    }

    private fun generate() {
        val goal = _uiState.value.goalInput.trim()
        if (goal.isEmpty()) {
            _uiState.value = _uiState.value.copy(message = "Goal is required.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            val locale = Locale.getDefault().language
            val access = accessUseCase(FeatureGate.AI_ASSISTANT)

            val result: TaskBreakdownResult = if (access.allowed) {
                runCatching {
                    aiAssistantRepository.generateTaskBreakdown(goal, locale)
                }.getOrElse {
                    fallbackService.generateTaskBreakdown(goal)
                }
            } else {
                fallbackService.generateTaskBreakdown(goal)
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                subtasks = result.subtasks,
                source = result.source,
                message = access.reason ?: result.message
            )
        }
    }
}

class AiAssistantViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val entitlementRepository = appContainer.entitlementRepository
        val accessUseCase = EvaluateFeatureAccessUseCase(entitlementRepository)
        val viewModel = AiAssistantViewModel(
            aiAssistantRepository = appContainer.aiAssistantRepository,
            accessUseCase = accessUseCase,
            fallbackService = appContainer.fallbackService
        )
        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
