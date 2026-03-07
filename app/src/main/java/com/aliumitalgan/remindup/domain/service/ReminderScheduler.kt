package com.aliumitalgan.remindup.domain.service

import com.aliumitalgan.remindup.models.Reminder

interface ReminderScheduler {
    fun schedule(reminderId: String, reminder: Reminder)
    fun cancel(reminderId: String)
}
