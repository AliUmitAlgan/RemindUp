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
    override fun onReceive(context: Context, intent: Intent) {
        // Gelen intent'ten hatırlatıcı bilgilerini al
        val reminderTitle = intent.getStringExtra("REMINDER_TITLE") ?: "Hatırlatıcı"
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", 0)
        val reminderType = intent.getStringExtra("REMINDER_TYPE")
        val reminderTime = intent.getStringExtra("REMINDER_TIME") ?: ""

        Log.d("ReminderReceiver", "Hatırlatıcı alındı: $reminderTitle, Tip: $reminderType")

        // Bildirim içeriği
        val reminderMessage = intent.getStringExtra("REMINDER_MESSAGE")
            ?: "Zamanı geldiği için hatırlatıyoruz!"

        // Bildirimi göster
        NotificationUtils.showNotification(
            context,
            reminderTitle,
            reminderMessage,
            notificationId
        )

        // Eğer tek seferlik bir hatırlatıcı ise, alarm'ı iptal et
        if (reminderType == ReminderType.SINGLE.name) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val cancelIntent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                cancelIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )

            // Eğer pending intent varsa, alarm'ı iptal et
            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
                Log.d("ReminderReceiver", "Tek seferlik hatırlatıcı iptal edildi: $reminderTitle")
            }
        }

        // Cihaz yeniden başlatılırsa
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("ReminderReceiver", "Cihaz yeniden başlatıldı, hatırlatıcılar yeniden ayarlanıyor")
            // TODO: Tüm hatırlatıcıları Firestore'dan alıp yeniden zamanla
            // Bu kısım daha sonra BootReceiver içinde implemente edilebilir
        }
    }

    // Statik metodlar için companion object kullanabilirsiniz
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