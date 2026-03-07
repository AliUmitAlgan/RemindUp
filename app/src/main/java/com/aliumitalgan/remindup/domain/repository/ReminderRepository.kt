package com.aliumitalgan.remindup.domain.repository

import com.aliumitalgan.remindup.domain.model.ReminderRecord
import com.aliumitalgan.remindup.models.Reminder

interface ReminderRepository {
    suspend fun getUserReminders(): Result<List<ReminderRecord>>
    suspend fun addReminder(reminder: Reminder): Result<String>
    suspend fun updateReminder(reminderId: String, reminder: Reminder): Result<Unit>
    suspend fun deleteReminder(reminderId: String): Result<Unit>
}
