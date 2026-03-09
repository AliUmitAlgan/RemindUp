package com.aliumitalgan.remindup.domain.repository

import com.aliumitalgan.remindup.domain.model.Goal

interface GoalRepository {
    suspend fun getUserGoals(): Result<List<Pair<String, Goal>>>
    suspend fun addGoal(goal: Goal): Result<String>
    suspend fun updateGoalProgress(goalId: String, newProgress: Int): Result<Boolean>
    suspend fun deleteGoal(goalId: String): Result<Unit>
    suspend fun getOverallProgress(): Result<Float>
}
