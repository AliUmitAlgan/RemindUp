package com.aliumitalgan.remindup.data.local.room.mapper

import com.aliumitalgan.remindup.data.local.room.entity.DailyTaskEntity
import com.aliumitalgan.remindup.data.local.room.entity.GoalEntity
import com.aliumitalgan.remindup.data.local.room.model.TaskEffort as LocalTaskEffort
import com.aliumitalgan.remindup.data.local.room.relation.GoalTemplateWithTasks
import com.aliumitalgan.remindup.domain.model.planner.DailyTask
import com.aliumitalgan.remindup.domain.model.planner.Goal
import com.aliumitalgan.remindup.domain.model.planner.GoalTemplate
import com.aliumitalgan.remindup.domain.model.planner.GoalTemplateTask
import com.aliumitalgan.remindup.domain.model.planner.TaskEffort

fun GoalEntity.toDomain(): Goal = Goal(
    id = id,
    title = title,
    description = description,
    category = category,
    priority = priority,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun DailyTaskEntity.toDomain(): DailyTask = DailyTask(
    id = id,
    goalId = goalId,
    title = title,
    notes = notes,
    dateIso = date,
    effort = effort.toDomain(),
    estimatedMinutes = estimatedMinutes,
    isCompleted = isCompleted,
    completedAt = completedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun GoalTemplateWithTasks.toDomain(): GoalTemplate = GoalTemplate(
    id = template.id,
    name = template.name,
    description = template.description,
    category = template.category,
    iconEmoji = template.iconEmoji,
    tasks = tasks.map { task ->
        GoalTemplateTask(
            id = task.id,
            title = task.title,
            notes = task.notes,
            effort = task.effort.toDomain(),
            defaultDurationMinutes = task.defaultDurationMinutes,
            sortOrder = task.sortOrder
        )
    }
)

fun LocalTaskEffort.toDomain(): TaskEffort = when (this) {
    LocalTaskEffort.EASY -> TaskEffort.EASY
    LocalTaskEffort.MEDIUM -> TaskEffort.MEDIUM
    LocalTaskEffort.HARD -> TaskEffort.HARD
}

fun TaskEffort.toLocal(): LocalTaskEffort = when (this) {
    TaskEffort.EASY -> LocalTaskEffort.EASY
    TaskEffort.MEDIUM -> LocalTaskEffort.MEDIUM
    TaskEffort.HARD -> LocalTaskEffort.HARD
}
