package com.aliumitalgan.remindup.data.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aliumitalgan.remindup.data.local.room.model.TaskEffort

@Entity(
    tableName = "goal_template_tasks",
    foreignKeys = [
        ForeignKey(
            entity = GoalTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("templateId"),
        Index("sortOrder")
    ]
)
data class GoalTemplateTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val templateId: Long,
    val title: String,
    val notes: String = "",
    val effort: TaskEffort = TaskEffort.MEDIUM,
    val defaultDurationMinutes: Int,
    val sortOrder: Int
)
