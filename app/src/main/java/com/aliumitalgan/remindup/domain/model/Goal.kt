package com.aliumitalgan.remindup.domain.model

data class Goal(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val progress: Int = 0,
    val categoryId: String = "",
    val category: Int = 0,
    val dueDate: String = "",
    val reminderTime: String = "",
    val smartReminderEnabled: Boolean = true,
    val isImportant: Boolean = false,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
