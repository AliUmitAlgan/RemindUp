package com.aliumitalgan.remindup.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.models.ReminderCategory
import com.aliumitalgan.remindup.models.ReminderType
import com.aliumitalgan.remindup.utils.NotificationUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * This BroadcastReceiver handles incoming alarm events triggered by AlarmManager
 * and shows notifications to the user.
 */
class ReminderReceiver : BroadcastReceiver() {
    private val TAG = "ReminderReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")

        try {
            // Check if this is a boot completed intent
            if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
                intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
                intent.action == "com.htc.intent.action.QUICKBOOT_POWERON") {

                Log.d(TAG, "Device rebooted, restoring reminders")
                NotificationUtils.restoreRemindersAfterReboot(context)
                return
            }

            // Otherwise handle regular reminder notification
            if (intent.action == "com.aliumitalgan.remindup.TRIGGER_REMINDER") {
                // Gelen intent'ten hatırlatıcı bilgilerini al
                val reminderTitle = intent.getStringExtra("REMINDER_TITLE") ?: "Hatırlatıcı"
                val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
                val reminderTypeStr = intent.getStringExtra("REMINDER_TYPE")
                val reminderTime = intent.getStringExtra("REMINDER_TIME") ?: ""
                val reminderDescription = intent.getStringExtra("REMINDER_DESCRIPTION") ?: ""
                val reminderId = intent.getStringExtra("REMINDER_ID") ?: notificationId.toString()

                Log.d(TAG, "Reminder received: $reminderTitle, Type: $reminderTypeStr, Time: $reminderTime")

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

                // If this is a recurring reminder, we need to reschedule the next one for recurring types
                if (reminderType != ReminderType.SINGLE) {
                    Log.d(TAG, "Rescheduling recurring reminder for next occurrence")

                    // Create a Reminder object from intent extras
                    val categoryStr = intent.getStringExtra("REMINDER_CATEGORY") ?: "GENERAL"
                    val category = try {
                        ReminderCategory.valueOf(categoryStr)
                    } catch (e: Exception) {
                        ReminderCategory.GENERAL
                    }

                    val reminder = Reminder(
                        id = reminderId,
                        title = reminderTitle,
                        time = reminderTime,
                        description = reminderDescription,
                        type = reminderType,
                        category = category,
                        userId = intent.getStringExtra("REMINDER_USER_ID") ?: "",
                        isEnabled = true
                    )

                    // Let the NotificationUtils handle the rescheduling
                    NotificationUtils.scheduleReminder(context, reminder, notificationId)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing received intent: ${e.message}", e)
        }
    }
}