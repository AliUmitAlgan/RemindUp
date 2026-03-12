package com.aliumitalgan.remindup.domain.model.planner

data class DailyTask(
    val id: Long,
    val goalId: Long,
    val title: String,
    val notes: String,
    val dateIso: String,
    val effort: TaskEffort,
    val estimatedMinutes: Int,
    val isCompleted: Boolean,
    val completedAt: Long?,
    val createdAt: Long,
    val updatedAt: Long
)
