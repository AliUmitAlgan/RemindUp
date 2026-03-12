package com.aliumitalgan.remindup.presentation.analytics

data class AnalyticsUiState(
    val weeklyProgress: List<WeeklyProgressBarItem> = emptyList(),
    val completedThisWeek: List<CompletedTaskItem> = emptyList()
)

data class WeeklyProgressBarItem(
    val dayLabel: String,
    val completedCount: Int
)

data class CompletedTaskItem(
    val id: Long,
    val title: String,
    val completedAt: Long
)
