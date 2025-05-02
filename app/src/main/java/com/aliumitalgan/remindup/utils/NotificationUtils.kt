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
import com.aliumitalgan.remindup.models.ReminderType
import com.aliumitalgan.remindup.receiver.ReminderReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*
import android.Manifest
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object NotificationUtils {
    private const val NOTIFICATION_PREFS = "notification_prefs"
    private const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
    private const val CHANNEL_ID = "remindup_channel"
    private const val CHANNEL_NAME = "RemindUp Notifications"
    private const val CHANNEL_DESCRIPTION = "Notifications for RemindUp app"
    private const val TAG = "NotificationUtils"

    // Uygulama başlatma sayacı, uygulama başladığında bildirimleri önlemek için
    private var isAppJustLaunched = true

    // Bildirim izni durumunu tutan StateFlow
    private val _notificationsEnabled = MutableStateFlow(true) // Başlangıçta true
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    // Uygulama başlangıç durumunu sıfırla
    fun resetAppLaunchState() {
        // 5 saniye sonra normal duruma geç
        CoroutineScope(Dispatchers.Default).launch {
            kotlinx.coroutines.delay(5000) // 5 saniyelik gecikme
            isAppJustLaunched = false
            Log.d(TAG, "App launch state reset. Normal notification behavior enabled.")
        }
    }

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
            Log.d(TAG, "Notification channel created")
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
        // Hemen state'i güncelle
        _notificationsEnabled.value = enabled

        // SharedPreferences'a kaydet
        val sharedPrefs = context.getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, enabled).apply()
        Log.d(TAG, "Notification state saved: $enabled")

        // Hatırlatıcıları güncellemeyi arka planda yap
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val remindersResult = ReminderUtils.getUserReminders()
                if (remindersResult.isSuccess) {
                    val reminders = remindersResult.getOrDefault(emptyList())

                    withContext(Dispatchers.Main) {
                        if (enabled) {
                            // Bildirimler açıldıysa tüm hatırlatıcıları yeniden zamanla
                            reminders.forEach { (id, reminder) ->
                                if (reminder.isEnabled) {
                                    scheduleReminder(
                                        context,
                                        reminder,
                                        id.hashCode()
                                    )
                                }
                            }
                            Log.d(TAG, "Reminders rescheduled after enabling notifications")
                        } else {
                            // Bildirimler kapatıldıysa tüm hatırlatıcıları iptal et
                            reminders.forEach { (id, _) ->
                                cancelReminder(context, id.hashCode())
                            }
                            Log.d(TAG, "Reminders canceled after disabling notifications")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Hatırlatıcıları güncelleme hatası", e)
            }
        }
    }

    // Bildirim tercihini yükle
    fun loadNotificationState(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE)
        val isEnabled = sharedPrefs.getBoolean(NOTIFICATIONS_ENABLED_KEY, true) // Varsayılan olarak açık
        _notificationsEnabled.value = isEnabled
        Log.d(TAG, "Notification state loaded: $isEnabled")
        return isEnabled
    }

    // Anında bildirim göster
    fun showNotification(context: Context, title: String, message: String, notificationId: Int) {
        // Uygulama yeni başlatıldıysa ve bu otomatik bir bildirimse, gösterme
        if (isAppJustLaunched) {
            Log.d(TAG, "App just launched, skipping notification: $title")
            return
        }

        if (!_notificationsEnabled.value) {
            Log.d(TAG, "Notifications disabled, not showing notification: $title")
            return
        }

        if (!checkNotificationPermission(context)) {
            Log.d(TAG, "No notification permission, not showing notification: $title")
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // PendingIntent oluştur - FLAG_IMMUTABLE önemli!
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
                    Log.d(TAG, "Notification shown: $title")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Notification display error", e)
        }
    }

    // Zamanlanmış hatırlatıcı ayarla - tam zamanında bildirim için iyileştirildi
    fun scheduleReminder(context: Context, reminder: Reminder, notificationId: Int) {
        // Bildirimler kapalıysa veya hatırlatıcı aktif değilse çalışma
        if (!_notificationsEnabled.value || !reminder.isEnabled) {
            Log.d(TAG, "Notifications disabled or reminder inactive, not scheduling: ${reminder.title}")
            return
        }

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            // İlk önce varsa eski alarmı iptal et (çift bildirim önlemek için)
            cancelReminder(context, notificationId)

            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra("REMINDER_TITLE", reminder.title)
                putExtra("NOTIFICATION_ID", notificationId)
                putExtra("REMINDER_TIME", reminder.time)
                putExtra("REMINDER_TYPE", reminder.type.name)
                putExtra("REMINDER_DESCRIPTION", reminder.description)
                // Reminder ID'sini de ekleyelim - yeniden çizelgeleme için
                putExtra("REMINDER_ID", notificationId.toString())
            }

            // Zamanı doğru parse et
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = timeFormat.parse(reminder.time)
            val calendar = Calendar.getInstance().apply {
                if (date != null) {
                    val now = Calendar.getInstance()

                    // Mevcut saat ve dakikayı koru, sadece hatırlatıcının saatini ayarla
                    set(Calendar.HOUR_OF_DAY, date.hours)
                    set(Calendar.MINUTE, date.minutes)
                    set(Calendar.SECOND, 0)

                    // Eğer ayarlanan zaman geçmişte kaldıysa, bir sonraki güne ayarla
                    if (before(now)) {
                        add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
            }

            val triggerTime = calendar.timeInMillis

            Log.d(TAG, "Scheduling reminder '${reminder.title}' for time: ${reminder.time}, actual time: ${Date(triggerTime)}")

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            // Tekrar türüne göre farklı alarm ayarlama
            when (reminder.type) {
                ReminderType.SINGLE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        // API 23+ için setExactAndAllowWhileIdle kullan (Doze modunda çalışabilmesi için)
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                        Log.d(TAG, "Single reminder scheduled with setExactAndAllowWhileIdle")
                    } else {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                        Log.d(TAG, "Single reminder scheduled with setExact")
                    }
                }
                ReminderType.DAILY -> {
                    // Günlük tekrar
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                    Log.d(TAG, "Daily reminder scheduled")
                }
                ReminderType.WEEKLY -> {
                    // Haftalık tekrar
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        AlarmManager.INTERVAL_DAY * 7,
                        pendingIntent
                    )
                    Log.d(TAG, "Weekly reminder scheduled")
                }
                ReminderType.MONTHLY -> {
                    // Aylık tekrar (yaklaşık 30 gün)
                    alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        AlarmManager.INTERVAL_DAY * 30,
                        pendingIntent
                    )
                    Log.d(TAG, "Monthly reminder scheduled")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder: ${e.message}", e)
        }
    }

    // Zamanlanmış hatırlatıcıyı iptal et
    fun cancelReminder(context: Context, notificationId: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
            )

            pendingIntent?.let {
                alarmManager.cancel(it)
                it.cancel()
                Log.d(TAG, "Reminder canceled: $notificationId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling reminder: ${e.message}", e)
        }
    }

    // Cihaz yeniden başlatıldıktan sonra hatırlatıcıları yükle
    fun restoreRemindersAfterReboot(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notificationsEnabled = loadNotificationState(context)

                if (!notificationsEnabled) {
                    Log.d(TAG, "Bildirimler devre dışı, hatırlatıcılar yeniden yüklenmeyecek")
                    return@launch
                }

                val remindersResult = ReminderUtils.getUserReminders()

                if (remindersResult.isSuccess) {
                    val reminders = remindersResult.getOrDefault(emptyList())
                    Log.d(TAG, "Toplam ${reminders.size} hatırlatıcı yeniden zamanlanacak")

                    for ((id, reminder) in reminders) {
                        if (reminder.isEnabled) {
                            scheduleReminder(
                                context,
                                reminder,
                                id.hashCode()
                            )
                            Log.d(TAG, "Reboot sonrası hatırlatıcı yeniden zamanlandı: ${reminder.title}")
                        }
                    }
                } else {
                    Log.e(TAG, "Reboot sonrası hatırlatıcılar alınamadı: ${remindersResult.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Reboot sonrası hatırlatıcı yükleme hatası: ${e.message}", e)
            }
        }
    }
}