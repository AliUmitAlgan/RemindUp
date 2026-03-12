package com.aliumitalgan.remindup.domain.model.planner

data class GoalTemplate(
    val id: Long,
    val name: String,
    val description: String,
    val category: String,
    val iconEmoji: String,
    val tasks: List<GoalTemplateTask>
)

data class GoalTemplateTask(
    val id: Long,
    val title: String,
    val notes: String,
    val effort: TaskEffort,
    val defaultDurationMinutes: Int,
    val sortOrder: Int
)
