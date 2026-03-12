package com.aliumitalgan.remindup.data.local.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.aliumitalgan.remindup.data.local.room.model.TaskEffort

@Entity(
    tableName = "daily_tasks",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("goalId"),
        Index("date"),
        Index("effort"),
        Index("isCompleted")
    ]
)
data class DailyTaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val goalId: Long,
    val title: String,
    val notes: String = "",
    val date: String,
    val effort: TaskEffort = TaskEffort.MEDIUM,
    val estimatedMinutes: Int = 25,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
