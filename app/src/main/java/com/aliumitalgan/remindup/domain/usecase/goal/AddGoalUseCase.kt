package com.aliumitalgan.remindup.domain.usecase.goal

import com.aliumitalgan.remindup.domain.model.Goal
import com.aliumitalgan.remindup.domain.repository.GoalRepository

class AddGoalUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goal: Goal): Result<String> = repository.addGoal(goal)
}
