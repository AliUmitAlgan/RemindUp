package com.aliumitalgan.remindup.domain.usecase.goal

import com.aliumitalgan.remindup.domain.repository.GoalRepository

class DeleteGoalUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goalId: String): Result<Unit> = repository.deleteGoal(goalId)
}
