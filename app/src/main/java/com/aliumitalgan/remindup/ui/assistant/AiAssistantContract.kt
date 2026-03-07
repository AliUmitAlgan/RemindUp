package com.aliumitalgan.remindup.ui.assistant

import com.aliumitalgan.remindup.domain.model.AiResponseSource

data class AiAssistantUiState(
    val goalInput: String = "",
    val isLoading: Boolean = false,
    val subtasks: List<String> = emptyList(),
    val source: AiResponseSource? = null,
    val message: String? = null
)

sealed interface AiAssistantUiEvent {
    data class GoalChanged(val value: String) : AiAssistantUiEvent
    data object GenerateClicked : AiAssistantUiEvent
    data object ClearMessage : AiAssistantUiEvent
}
