package com.aliumitalgan.remindup.presentation.home

import com.aliumitalgan.remindup.domain.model.planner.EnergyLevel
import com.aliumitalgan.remindup.domain.model.planner.TaskEffort

data class PlannerHomeUiState(
    val greeting: String = "Hello, Ali! ✨",
    val energyLevel: EnergyLevel = EnergyLevel.NORMAL,
    val todayTasks: List<PlannerHomeTaskItem> = emptyList()
) {
    val visibleTasks: List<PlannerHomeTaskItem>
        get() = if (energyLevel == EnergyLevel.TIRED) {
            todayTasks.filter { it.effort.isEasy }
        } else {
            todayTasks
        }

    val recommendedFocusSprintMinutes: Int
        get() = energyLevel.focusSprintMinutes
}

data class PlannerHomeTaskItem(
    val id: Long,
    val title: String,
    val effort: TaskEffort,
    val estimatedMinutes: Int,
    val isCompleted: Boolean
)
