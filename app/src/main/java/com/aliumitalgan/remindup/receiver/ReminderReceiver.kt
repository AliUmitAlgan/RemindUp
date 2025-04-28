package com.aliumitalgan.remindup.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.aliumitalgan.remindup.utils.NotificationUtils
import java.util.*

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderTitle = intent.getStringExtra("REMINDER_TITLE") ?: "Hatırlatıcı"
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val isRepeating = intent.getBooleanExtra("IS_REPEATING", false)

        // Bildirim içeriği ve ek bilgiler
        val reminderMessage = if (intent.hasExtra("REMINDER_MESSAGE")) {
            intent.getStringExtra("REMINDER_MESSAGE") ?: ""
        } else {
            "Zamanı geldiği için hatırlatıyoruz!"
        }

        Log.d("ReminderReceiver", "Hatırlatıcı alındı: $reminderTitle, ID: $notificationId")

        // Bildirimi göster
        NotificationUtils.showNotification(
            context,
            reminderTitle,
            reminderMessage,
            notificationId
        )

        // Eğer cihaz yeniden başlatılırsa tekrarlayan hatırlatıcıların da
        // yeniden ayarlanması için BOOT_COMPLETED broadcast'ini dinleyen bir
        // receiver eklemek için kontrolü burada yapalım
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("ReminderReceiver", "Cihaz yeniden başlatıldı, hatırlatıcılar yeniden ayarlanıyor")
            // TODO: Tüm hatırlatıcıları Firestore'dan alıp yeniden zamanla
        }
    }

    companion object {
        // Hatırlatıcıyı zamanla
        fun scheduleReminder(context: Context, reminderTitle: String, reminderTime: String, notificationId: Int) {
            val reminder = com.aliumitalgan.remindup.models.Reminder(
                title = reminderTitle,
                time = reminderTime
            )
            NotificationUtils.scheduleReminder(context, reminder, notificationId)
        }

        // Tekrarlayan hatırlatıcıyı zamanla
        fun scheduleRepeatingReminder(context: Context, reminderTitle: String, reminderTime: String, notificationId: Int) {
            val reminder = com.aliumitalgan.remindup.models.Reminder(
                title = reminderTitle,
                time = reminderTime
            )
            NotificationUtils.scheduleRepeatingReminder(context, reminder, notificationId)
        }
    }
}