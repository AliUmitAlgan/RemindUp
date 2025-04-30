package com.aliumitalgan.remindup.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aliumitalgan.remindup.utils.NotificationUtils
import com.aliumitalgan.remindup.utils.ReminderUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Cihaz yeniden başlatıldığında tüm hatırlatıcıları yeniden zamanlamak için kullanılan BroadcastReceiver
 * AndroidManifest.xml'de BOOT_COMPLETED action'ı ile kaydedilmelidir
 */
class BootReceiver : BroadcastReceiver() {
    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device rebooted. Rescheduling reminders.")

            // NotificationUtils'in başlatılması
            NotificationUtils.createNotificationChannel(context)

            // Bildirim durumunu hafızaya yükle
            val areNotificationsEnabled = NotificationUtils.loadNotificationState(context)
            if (!areNotificationsEnabled) {
                Log.d(TAG, "Notifications are disabled, not restoring reminders")
                return
            }

            // Hatırlatıcıları yeniden zamanla - sadece bildirimler etkinse
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val remindersResult = ReminderUtils.getUserReminders()

                    if (remindersResult.isSuccess) {
                        val reminders = remindersResult.getOrDefault(emptyList())
                        Log.d(TAG, "Found ${reminders.size} reminders to reschedule")

                        withContext(Dispatchers.Main) {
                            for ((id, reminder) in reminders) {
                                if (reminder.isEnabled) {
                                    NotificationUtils.scheduleReminder(
                                        context,
                                        reminder,
                                        id.hashCode()
                                    )
                                    Log.d(TAG, "Rescheduled reminder after reboot: ${reminder.title}")
                                } else {
                                    Log.d(TAG, "Skipped disabled reminder: ${reminder.title}")
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Failed to get reminders: ${remindersResult.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling reminders after reboot: ${e.message}", e)
                }
            }
        }
    }
}