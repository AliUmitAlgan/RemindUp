package com.aliumitalgan.remindup.domain.model

enum class AiResponseSource {
    MODEL,
    FALLBACK
}

data class TaskBreakdownResult(
    val subtasks: List<String>,
    val source: AiResponseSource,
    val message: String? = null
)

data class SnoozeCoachingResult(
    val message: String,
    val actions: List<String>,
    val source: AiResponseSource
)
