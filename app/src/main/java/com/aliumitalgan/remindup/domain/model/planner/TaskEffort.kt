package com.aliumitalgan.remindup.domain.model.planner

enum class TaskEffort {
    EASY,
    MEDIUM,
    HARD;

    val isEasy: Boolean
        get() = this == EASY
}
