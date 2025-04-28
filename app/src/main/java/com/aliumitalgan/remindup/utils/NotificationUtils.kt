package com.aliumitalgan.remindup.utils

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.aliumitalgan.remindup.MainActivity
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.receiver.ReminderReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.util.Log


object NotificationUtils {

    private const val CHANNEL_ID = "remindup_channel"
    private const val CHANNEL_NAME = "RemindUp Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for RemindUp app"

    // Bildirim izni durumunu tutan StateFlow
    private val _notificationsEnabled = MutableStateFlow(true) // Başlangıçta true
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    // Bildirim kanalını oluştur
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESCRIPTION
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Bildirim izinlerini kontrol et
    fun checkNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 13'ten önceki sürümlerde bildirim izni direkt olarak uygulamanın kendisine verildiğinden true dönebiliriz
            true
        }
    }

    // Bildirim tercihini ayarlar (SharedPreferences kullanarak çözüm)
    fun saveNotificationState(context: Context, enabled: Boolean) {
        val sharedPrefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _notificationsEnabled.value = enabled
        Log.d("NotificationUtils", "Bildirim durumu güncellendi: $enabled")
    }

    // Bildirim tercihini yükle
    fun loadNotificationState(context: Context) {
        val sharedPrefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        _notificationsEnabled.value = sharedPrefs.getBoolean("notifications_enabled", true)
        Log.d("NotificationUtils", "Bildirim durumu yüklendi: ${_notificationsEnabled.value}")
    }

    // Bildirim tercihi flow'u
    fun getNotificationStateFlow(context: Context) = _notificationsEnabled

    // Anında bildirim göster
    fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        if (!_notificationsEnabled.value) {
            // Bildirimler kapalıysa bir şey gösterme
            Log.d("NotificationUtils", "Bildirimler kapalı, gösterilmiyor")
            return
        }

        if (!checkNotificationPermission(context)) {
            // İzin yoksa bildirim gösterme
            Log.d("NotificationUtils", "Bildirim izni yok")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(Color.BLUE)
            .setColorized(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    notify(notificationId, builder.build())
                    Log.d("NotificationUtils", "Bildirim gösterildi: $title")
                }
            }
        } catch (e: Exception) {
            Log.e("NotificationUtils", "Bildirim gösterme hatası", e)
        }
    }

    // Zamanlanmış hatırlatıcı ayarla
    fun scheduleReminder(context: Context, reminder: Reminder, notificationId: Int) {
        if (!_notificationsEnabled.value) {
            // Bildirimler kapalıysa bir şey zamanlanmasın
            Log.d("NotificationUtils", "Bildirimler kapalı, zamanlama yapılmıyor")
            return
        }

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
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d("NotificationUtils", "Hatırlatıcı ayarlandı: ${reminder.title} (${reminder.time})")
            } catch (e: Exception) {
                Log.e("NotificationUtils", "Hatırlatıcı ayarlama hatası", e)
                // Hatırlatıcı ayarlanamazsa, fallback olarak setAlarmClock'u deneyelim
                val showIntent = Intent(context, MainActivity::class.java)
                val showPendingIntent = PendingIntent.getActivity(
                    context, notificationId, showIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(calendar.timeInMillis, showPendingIntent),
                    pendingIntent
                )
                Log.d("NotificationUtils", "Fallback ile hatırlatıcı ayarlandı: ${reminder.title}")
            }
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d("NotificationUtils", "Eski API ile hatırlatıcı ayarlandı: ${reminder.title}")
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
            Log.d("NotificationUtils", "Hatırlatıcı iptal edildi: $notificationId")
        }
    }

    // Günlük tekrarlanan bir hatırlatıcı ayarla
    fun scheduleRepeatingReminder(context: Context, reminder: Reminder, notificationId: Int) {
        if (!_notificationsEnabled.value) {
            Log.d("NotificationUtils", "Bildirimler kapalı, tekrarlayan zamanlama yapılmıyor")
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_TITLE", reminder.title)
            putExtra("NOTIFICATION_ID", notificationId)
            putExtra("IS_REPEATING", true)
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

        // Günlük tekrarlanan alarm
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        Log.d("NotificationUtils", "Tekrarlayan hatırlatıcı ayarlandı: ${reminder.title}")
    }
}