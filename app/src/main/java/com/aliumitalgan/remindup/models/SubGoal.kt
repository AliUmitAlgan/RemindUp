package com.aliumitalgan.remindup.models

data class SubGoal(
    val id: String = "",
    val title: String,
    val completed: Boolean = false,
    val parentGoalId: String = ""
)