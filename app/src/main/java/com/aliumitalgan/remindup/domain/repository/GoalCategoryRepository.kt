package com.aliumitalgan.remindup.domain.repository

import com.aliumitalgan.remindup.domain.model.GoalCategory

interface GoalCategoryRepository {
    suspend fun getGoalCategories(): Result<List<GoalCategory>>
    suspend fun getGoalCategoryById(categoryId: String): Result<GoalCategory?>
    suspend fun saveGoalCategory(category: GoalCategory): Result<Unit>
    suspend fun deleteGoalCategory(categoryId: String): Result<Unit>
}
