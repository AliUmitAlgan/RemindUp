package com.aliumitalgan.remindup.models

import java.util.UUID

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val time: String = "",
    val category: ReminderCategory = ReminderCategory.GENERAL,
    val type: ReminderType = ReminderType.SINGLE,
    val description: String = "",
    val userId: String = "",
    val isEnabled: Boolean = true,
    val isImportant: Boolean = false // Yeni eklenen alan
)

enum class ReminderCategory {
    GENERAL, WORK, HEALTH, PERSONAL, STUDY, FITNESS
}

enum class ReminderType {
    SINGLE,       // Tek seferlik
    DAILY,        // Her gün
    WEEKLY,       // Haftada bir
    MONTHLY       // Ayda bir
}