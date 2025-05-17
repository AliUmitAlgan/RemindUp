package com.aliumitalgan.remindup.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.aliumitalgan.remindup.utils.NotificationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This BroadcastReceiver is triggered when the device boots up.
 * It ensures all scheduled notifications are restored after a device restart.
 */
class BootReceiver : BroadcastReceiver() {
    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "BootReceiver triggered, action: ${intent.action}")

        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON") {

            Log.d(TAG, "Device rebooted, restoring reminders")

            // First create notification channels
            NotificationUtils.createNotificationChannels(context)

            // Restore notifications in background
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // This will handle both notifications settings check and restoration
                    NotificationUtils.restoreRemindersAfterReboot(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring reminders after reboot", e)
                }
            }
        }
    }
}