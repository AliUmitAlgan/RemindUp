package com.aliumitalgan.remindup.domain.usecase.goalcategory

import com.aliumitalgan.remindup.domain.repository.GoalCategoryRepository

class DeleteGoalCategoryUseCase(
    private val repository: GoalCategoryRepository
) {
    suspend operator fun invoke(categoryId: String): Result<Unit> =
        repository.deleteGoalCategory(categoryId)
}
