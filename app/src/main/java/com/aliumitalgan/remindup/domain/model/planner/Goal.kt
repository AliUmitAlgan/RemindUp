package com.aliumitalgan.remindup.domain.model.planner

data class Goal(
    val id: Long,
    val title: String,
    val description: String,
    val category: String,
    val priority: Int,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
