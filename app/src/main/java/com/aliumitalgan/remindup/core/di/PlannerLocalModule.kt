package com.aliumitalgan.remindup.core.di

import android.content.Context
import com.aliumitalgan.remindup.data.local.room.RemindUpDatabase
import com.aliumitalgan.remindup.data.local.room.seed.seedDefaultTemplates
import com.aliumitalgan.remindup.data.repository.local.RoomPlannerRepository
import com.aliumitalgan.remindup.domain.repository.planner.PlannerRepository
import com.aliumitalgan.remindup.domain.usecase.planner.ObserveTodayDailyTasksUseCase
import com.aliumitalgan.remindup.domain.usecase.planner.ObserveWeeklyAnalyticsUseCase

class PlannerLocalModule(
    context: Context
) {
    private val database = RemindUpDatabase.getInstance(context.applicationContext)

    val plannerRepository: PlannerRepository by lazy {
        RoomPlannerRepository(
            goalDao = database.goalDao(),
            dailyTaskDao = database.dailyTaskDao(),
            goalTemplateDao = database.goalTemplateDao()
        )
    }

    val observeTodayDailyTasksUseCase by lazy {
        ObserveTodayDailyTasksUseCase(plannerRepository)
    }

    val observeWeeklyAnalyticsUseCase by lazy {
        ObserveWeeklyAnalyticsUseCase(plannerRepository)
    }

    suspend fun seedTemplatesIfNeeded() {
        seedDefaultTemplates(database.goalTemplateDao())
    }
}
