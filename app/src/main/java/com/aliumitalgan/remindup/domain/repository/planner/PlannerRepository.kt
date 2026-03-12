package com.aliumitalgan.remindup.domain.repository.planner

import com.aliumitalgan.remindup.domain.model.planner.DailyTask
import com.aliumitalgan.remindup.domain.model.planner.Goal
import com.aliumitalgan.remindup.domain.model.planner.GoalTemplate
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

interface PlannerRepository {
    fun observeGoals(): Flow<List<Goal>>

    fun observeTodayDailyTasks(
        date: LocalDate,
        easyOnly: Boolean
    ): Flow<List<DailyTask>>

    fun observeDailyTasksInRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<DailyTask>>

    fun observeTemplates(): Flow<List<GoalTemplate>>

    suspend fun applyTemplate(templateId: Long, date: LocalDate)
}
