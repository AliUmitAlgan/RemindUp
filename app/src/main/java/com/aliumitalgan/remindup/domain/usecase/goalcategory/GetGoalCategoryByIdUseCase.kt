package com.aliumitalgan.remindup.domain.usecase.goalcategory

import com.aliumitalgan.remindup.domain.model.GoalCategory
import com.aliumitalgan.remindup.domain.repository.GoalCategoryRepository

class GetGoalCategoryByIdUseCase(
    private val repository: GoalCategoryRepository
) {
    suspend operator fun invoke(categoryId: String): Result<GoalCategory?> =
        repository.getGoalCategoryById(categoryId)
}

