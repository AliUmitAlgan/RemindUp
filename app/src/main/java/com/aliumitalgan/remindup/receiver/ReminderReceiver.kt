package com.aliumitalgan.remindup.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.aliumitalgan.remindup.models.ReminderType
import com.aliumitalgan.remindup.utils.NotificationUtils
import java.util.*

class ReminderReceiver : BroadcastReceiver() {
    private val TAG = "ReminderReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        // Gelen intent'ten hatırlatıcı bilgilerini al
        val reminderTitle = intent.getStringExtra("REMINDER_TITLE") ?: "Hatırlatıcı"
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val reminderTypeStr = intent.getStringExtra("REMINDER_TYPE")
        val reminderTime = intent.getStringExtra("REMINDER_TIME") ?: ""
        val reminderDescription = intent.getStringExtra("REMINDER_DESCRIPTION") ?: ""

        Log.d(TAG, "Reminder received: $reminderTitle, Type: $reminderTypeStr, Time: $reminderTime")

        try {
            // Reminder tipini doğru şekilde parse et
            val reminderType = try {
                if (reminderTypeStr != null) ReminderType.valueOf(reminderTypeStr)
                else ReminderType.SINGLE
            } catch (e: Exception) {
                Log.e(TAG, "Invalid reminder type: $reminderTypeStr, defaulting to SINGLE", e)
                ReminderType.SINGLE
            }

            // Bildirim mesajını oluştur
            val notificationMessage = if (reminderDescription.isNotEmpty()) {
                reminderDescription
            } else {
                "Zamanı geldiği için hatırlatıyoruz!"
            }

            // Bildirimi göster
            NotificationUtils.showNotification(
                context,
                reminderTitle,
                notificationMessage,
                notificationId
            )

            // Eğer tek seferlik bir hatırlatıcı ise, alarm'ı iptal et
            if (reminderType == ReminderType.SINGLE) {
                NotificationUtils.cancelReminder(context, notificationId)
                Log.d(TAG, "Single reminder canceled after triggering: $reminderTitle")
            }
            // Tekrarlanan hatırlatıcılar için güncelleme gerekmez, AlarmManager otomatik olarak tekrarlar

            // BOOT_COMPLETED gibi diğer actionlar için de kontrol ekle
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED -> {
                    Log.d(TAG, "Device rebooted, should restore reminders")
                    // Boot Receiver zaten bu işi yapıyor, burada ek bir işlem yapmaya gerek yok
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing reminder: ${e.message}", e)
        }
    }

    companion object {
        // Hatırlatıcıyı zamanla
        fun scheduleReminder(context: Context, reminderTitle: String, reminderTime: String, notificationId: Int, reminderType: ReminderType) {
            val reminder = com.aliumitalgan.remindup.models.Reminder(
                title = reminderTitle,
                time = reminderTime,
                type = reminderType
            )
            NotificationUtils.scheduleReminder(context, reminder, notificationId)
        }
    }
}