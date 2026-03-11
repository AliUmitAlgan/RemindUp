package com.aliumitalgan.remindup.domain.usecase.goalcategory

import com.aliumitalgan.remindup.domain.model.GoalCategory
import com.aliumitalgan.remindup.domain.repository.GoalCategoryRepository

class GetGoalCategoriesUseCase(
    private val repository: GoalCategoryRepository
) {
    suspend operator fun invoke(): Result<List<GoalCategory>> = repository.getGoalCategories()
}

