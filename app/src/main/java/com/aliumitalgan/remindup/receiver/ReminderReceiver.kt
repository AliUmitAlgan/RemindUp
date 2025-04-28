package com.aliumitalgan.remindup.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aliumitalgan.remindup.utils.NotificationUtils

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderTitle = intent.getStringExtra("REMINDER_TITLE") ?: "Hatırlatıcı"
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)

        // Bildirimi göster
        NotificationUtils.showNotification(
            context,
            "RemindUp Hatırlatıcı",
            reminderTitle,
            notificationId
        )
    }
}