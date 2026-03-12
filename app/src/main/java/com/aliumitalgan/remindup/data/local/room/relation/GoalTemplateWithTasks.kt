package com.aliumitalgan.remindup.data.local.room.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.aliumitalgan.remindup.data.local.room.entity.GoalTemplateEntity
import com.aliumitalgan.remindup.data.local.room.entity.GoalTemplateTaskEntity

data class GoalTemplateWithTasks(
    @Embedded
    val template: GoalTemplateEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "templateId"
    )
    val tasks: List<GoalTemplateTaskEntity>
)
