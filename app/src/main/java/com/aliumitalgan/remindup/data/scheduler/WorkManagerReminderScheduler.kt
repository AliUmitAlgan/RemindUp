package com.aliumitalgan.remindup.data.scheduler

import android.content.Context
import com.aliumitalgan.remindup.domain.service.ReminderScheduler
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.utils.NotificationUtils

class WorkManagerReminderScheduler(
    private val context: Context
) : ReminderScheduler {
    override fun schedule(reminderId: String, reminder: Reminder) {
        NotificationUtils.scheduleReminder(
            context = context,
            reminder = reminder,
            notificationId = reminderId.hashCode()
        )
    }

    override fun cancel(reminderId: String) {
        NotificationUtils.cancelReminder(
            context = context,
            notificationId = reminderId.hashCode()
        )
    }
}
