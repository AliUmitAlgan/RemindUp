package com.aliumitalgan.remindup.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aliumitalgan.remindup.MainActivity
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.receiver.ReminderReceiver
import java.text.SimpleDateFormat
import java.util.*

object NotificationUtils {

    private const val CHANNEL_ID = "remindup_channel"
    private const val CHANNEL_NAME = "RemindUp Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for RemindUp app"

    // Bildirim kanalını oluştur
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Anında bildirim göster
    fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Notification ikonunu eklemelisiniz
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    // Zamanlanmış hatırlatıcı ayarla
    fun scheduleReminder(context: Context, reminder: Reminder, notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_TITLE", reminder.title)
            putExtra("NOTIFICATION_ID", notificationId)
        }

        // Time format: HH:mm
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = timeFormat.parse(reminder.time)
        val calendar = Calendar.getInstance().apply {
            time = date ?: Date()
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1) // Eğer zaman geçtiyse, ertesi güne ayarla
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Hatırlatıcıyı ayarla
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    // Zamanlanmış hatırlatıcıyı iptal et
    fun cancelReminder(context: Context, notificationId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, notificationId, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}