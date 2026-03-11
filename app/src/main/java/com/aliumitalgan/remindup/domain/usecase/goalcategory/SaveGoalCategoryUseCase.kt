package com.aliumitalgan.remindup.domain.usecase.goalcategory

import com.aliumitalgan.remindup.domain.model.GoalCategory
import com.aliumitalgan.remindup.domain.repository.GoalCategoryRepository

class SaveGoalCategoryUseCase(
    private val repository: GoalCategoryRepository
) {
    suspend operator fun invoke(category: GoalCategory): Result<Unit> =
        repository.saveGoalCategory(category)
}

