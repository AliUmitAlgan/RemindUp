package com.aliumitalgan.remindup.data.local.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goal_templates",
    indices = [Index(value = ["name"], unique = true)]
)
data class GoalTemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val description: String,
    val category: String,
    val iconEmoji: String,
    val createdAt: Long = System.currentTimeMillis()
)
