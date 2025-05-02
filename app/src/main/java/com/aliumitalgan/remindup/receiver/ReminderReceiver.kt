package com.aliumitalgan.remindup.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.models.ReminderType
import com.aliumitalgan.remindup.utils.NotificationUtils
import java.text.SimpleDateFormat
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
        val reminderId = intent.getStringExtra("REMINDER_ID") ?: notificationId.toString()

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

            // Tek seferlik hatırlatıcı için tekrar bir şey yapmaya gerek yok
            // Diğer tip hatırlatıcılar için AlarmManager kendisi tekrar zamanlar

            // Eğer cihaz yeni başlatıldıysa veya özel durumlarla karşılaşıldıysa
            // ACTION_BOOT_COMPLETED, ACTION_LOCKED_BOOT_COMPLETED gibi intent'ler için
            when (intent.action) {
                Intent.ACTION_BOOT_COMPLETED,
                "android.intent.action.QUICKBOOT_POWERON",
                "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                    Log.d(TAG, "Device rebooted, restoring reminders")
                    NotificationUtils.restoreRemindersAfterReboot(context)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing reminder: ${e.message}", e)
        }
    }
}