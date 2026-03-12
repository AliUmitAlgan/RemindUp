package com.aliumitalgan.remindup.data.repository.local

import com.aliumitalgan.remindup.data.local.room.dao.DailyTaskDao
import com.aliumitalgan.remindup.data.local.room.dao.GoalDao
import com.aliumitalgan.remindup.data.local.room.dao.GoalTemplateDao
import com.aliumitalgan.remindup.data.local.room.entity.DailyTaskEntity
import com.aliumitalgan.remindup.data.local.room.entity.GoalEntity
import com.aliumitalgan.remindup.data.local.room.mapper.toDomain
import com.aliumitalgan.remindup.domain.model.planner.DailyTask
import com.aliumitalgan.remindup.domain.model.planner.Goal
import com.aliumitalgan.remindup.domain.model.planner.GoalTemplate
import com.aliumitalgan.remindup.domain.repository.planner.PlannerRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPlannerRepository(
    private val goalDao: GoalDao,
    private val dailyTaskDao: DailyTaskDao,
    private val goalTemplateDao: GoalTemplateDao
) : PlannerRepository {

    override fun observeGoals(): Flow<List<Goal>> {
        return goalDao.observeActiveGoals()
            .map { goals -> goals.map { it.toDomain() } }
    }

    override fun observeTodayDailyTasks(
        date: LocalDate,
        easyOnly: Boolean
    ): Flow<List<DailyTask>> {
        val source = if (easyOnly) {
            dailyTaskDao.observeCompassionModeTasksByDate(date.toString())
        } else {
            dailyTaskDao.observeTasksByDate(date.toString())
        }

        return source.map { tasks -> tasks.map { it.toDomain() } }
    }

    override fun observeDailyTasksInRange(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<DailyTask>> {
        return dailyTaskDao.observeTasksInRange(
            startDate = startDate.toString(),
            endDate = endDate.toString()
        ).map { tasks -> tasks.map { it.toDomain() } }
    }

    override fun observeTemplates(): Flow<List<GoalTemplate>> {
        return goalTemplateDao.observeTemplates()
            .map { templates ->
                templates.map { template ->
                    GoalTemplate(
                        id = template.id,
                        name = template.name,
                        description = template.description,
                        category = template.category,
                        iconEmoji = template.iconEmoji,
                        tasks = emptyList()
                    )
                }
            }
    }

    override suspend fun applyTemplate(templateId: Long, date: LocalDate) {
        val template = goalTemplateDao.getTemplateWithTasks(templateId) ?: return
        val timestamp = System.currentTimeMillis()
        val createdGoalId = goalDao.upsert(
            GoalEntity(
                title = template.template.name,
                description = template.template.description,
                category = template.template.category,
                createdAt = timestamp,
                updatedAt = timestamp
            )
        )

        val dailyTasks = template.tasks.map { templateTask ->
            DailyTaskEntity(
                goalId = createdGoalId,
                title = templateTask.title,
                notes = templateTask.notes,
                date = date.toString(),
                effort = templateTask.effort,
                estimatedMinutes = templateTask.defaultDurationMinutes,
                createdAt = timestamp,
                updatedAt = timestamp
            )
        }

        dailyTaskDao.insertAll(dailyTasks)
    }
}
