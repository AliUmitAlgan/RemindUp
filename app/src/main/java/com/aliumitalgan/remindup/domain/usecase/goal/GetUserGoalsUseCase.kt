package com.aliumitalgan.remindup.domain.usecase.goal

import com.aliumitalgan.remindup.domain.model.Goal
import com.aliumitalgan.remindup.domain.repository.GoalRepository

class GetUserGoalsUseCase(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(): Result<List<Pair<String, Goal>>> = repository.getUserGoals()
}
