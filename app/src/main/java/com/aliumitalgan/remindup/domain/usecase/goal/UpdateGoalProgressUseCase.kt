package com.aliumitalgan.remindup.domain.usecase.goal

import com.aliumitalgan.remindup.domain.repository.GoalRepository

class UpdateGoalProgressUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goalId: String, newProgress: Int): Result<Boolean> =
        repository.updateGoalProgress(goalId, newProgress)
}
