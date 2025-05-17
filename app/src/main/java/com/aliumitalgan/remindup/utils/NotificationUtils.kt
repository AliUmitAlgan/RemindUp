package com.aliumitalgan.remindup.utils

import android.Manifest
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
import androidx.work.*
import com.aliumitalgan.remindup.MainActivity
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.models.ReminderType
import com.aliumitalgan.remindup.workers.ReminderWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object NotificationUtils {
    private const val NOTIFICATION_PREFS = "notification_prefs"
    private const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
    private const val CHANNEL_ID = "remindup_channel"
    private const val CHANNEL_ID_IMPORTANT = "remindup_important_channel"
    private const val TAG = "NotificationUtils"

    // Uygulama başlatma sayacı
    private var isAppJustLaunched = true

    // Bildirim izni durumu
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    // Uygulama başlangıç durumunu sıfırla
    fun resetAppLaunchState() {
        CoroutineScope(Dispatchers.Default).launch {
            kotlinx.coroutines.delay(5000)
            isAppJustLaunched = false
            Log.d(TAG, "App launch state reset. Normal notification behavior enabled.")
        }
    }

    // Bildirim kanallarını oluştur
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Normal bildirimlerin kanalı
            val normalChannel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                enableLights(true)
                lightColor = Color.BLUE
                enableVibration(true)
                setShowBadge(true)
            }

            // Önemli bildirimlerin kanalı
            val importantChannel = NotificationChannel(
                CHANNEL_ID_IMPORTANT,
                "Önemli Hatırlatıcılar",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Yüksek öncelikli hatırlatıcılar için bildirimler"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 100, 250)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(normalChannel)
            notificationManager.createNotificationChannel(importantChannel)

            Log.d(TAG, "Notification channels created successfully")
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
            true
        }
    }

    // Bildirim tercihini kaydet
    fun saveNotificationState(context: Context, enabled: Boolean) {
        _notificationsEnabled.value = enabled
        val sharedPrefs = context.getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, enabled).apply()
        Log.d(TAG, "Notification state saved: $enabled")

        CoroutineScope(Dispatchers.IO).launch {
            if (enabled) {
                restoreAllReminders(context)
                Log.d(TAG, "Reminders restored after enabling notifications")
            } else {
                cancelAllReminders(context)
                Log.d(TAG, "All reminders canceled after disabling notifications")
            }
        }
    }

    // Bildirim tercihini yükle
    fun loadNotificationState(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences(NOTIFICATION_PREFS, Context.MODE_PRIVATE)
        val isEnabled = sharedPrefs.getBoolean(NOTIFICATIONS_ENABLED_KEY, true)
        _notificationsEnabled.value = isEnabled
        Log.d(TAG, "Notification state loaded: $isEnabled")
        return isEnabled
    }

    // Anında bildirim göster
    fun showNotification(context: Context, title: String, message: String, notificationId: Int, isImportant: Boolean = false) {
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

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = if (isImportant) CHANNEL_ID_IMPORTANT else CHANNEL_ID

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(if (isImportant) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setColor(if (isImportant) Color.RED else Color.BLUE)
            .setColorized(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

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

    // WorkManager ile hatırlatıcı zamanla
    fun scheduleReminder(context: Context, reminder: Reminder, notificationId: Int) {
        if (!_notificationsEnabled.value || !reminder.isEnabled) {
            Log.d(TAG, "Notifications disabled or reminder inactive, not scheduling: ${reminder.title}")
            return
        }

        try {
            // Önce mevcut work'u iptal et (çakışmaları önlemek için)
            WorkManager.getInstance(context).cancelWorkById(UUID.fromString(reminder.id))

            // Reminder bilgilerini JSON formatına çevir
            val gson = Gson()
            val reminderJson = gson.toJson(reminder)

            // İşçi için giriş verileri
            val inputData = workDataOf(
                "REMINDER_DATA" to reminderJson,
                "NOTIFICATION_ID" to notificationId
            )

            // Zamanı parse et
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = timeFormat.parse(reminder.time)

            if (date != null) {
                val calendar = Calendar.getInstance()
                val now = Calendar.getInstance()

                // Zamanı ayarla
                calendar.set(Calendar.HOUR_OF_DAY, date.hours)
                calendar.set(Calendar.MINUTE, date.minutes)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)

                // Eğer belirtilen zaman geçmişse bir sonraki güne ayarla
                if (calendar.before(now)) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }

                val initialDelay = calendar.timeInMillis - now.timeInMillis
                Log.d(TAG, "Scheduling reminder '${reminder.title}' with initial delay: ${initialDelay/1000/60} mins")

                // Tekrarlama tipine göre WorkRequest oluştur
                val workRequest = when (reminder.type) {
                    ReminderType.SINGLE -> {
                        // Tek seferlik OneTimeWorkRequest
                        OneTimeWorkRequestBuilder<ReminderWorker>()
                            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                            .setInputData(inputData)
                            .setBackoffCriteria(
                                BackoffPolicy.LINEAR,
                                WorkRequest.MIN_BACKOFF_MILLIS,
                                TimeUnit.MILLISECONDS
                            )
                            .build()
                    }
                    ReminderType.DAILY -> {
                        // Günlük tekrarlanan PeriodicWorkRequest
                        PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
                            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                            .setInputData(inputData)
                            .build()
                    }
                    ReminderType.WEEKLY -> {
                        // Haftalık tekrarlanan PeriodicWorkRequest
                        PeriodicWorkRequestBuilder<ReminderWorker>(7, TimeUnit.DAYS)
                            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                            .setInputData(inputData)
                            .build()
                    }
                    ReminderType.MONTHLY -> {
                        // Aylık tekrarlanan PeriodicWorkRequest
                        // Not: WorkManager tam olarak ayda bir tekrarlamayı desteklemez
                        // Bu nedenle yaklaşık 30 gün kullanıyoruz
                        PeriodicWorkRequestBuilder<ReminderWorker>(30, TimeUnit.DAYS)
                            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                            .setInputData(inputData)
                            .build()
                    }
                }

                // Work'u zamanla
                WorkManager.getInstance(context)
                    .enqueueUniqueWork(
                        reminder.id,
                        ExistingWorkPolicy.REPLACE,
                        workRequest as OneTimeWorkRequest
                    )

                Log.d(TAG, "Reminder scheduled successfully with WorkManager: ${reminder.title} at ${reminder.time}")

                // Hatırlatıcıyı SharedPreferences'e kaydet (yedek olarak)
                saveReminderToPrefs(context, notificationId, reminder)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling reminder: ${e.message}", e)
        }
    }

    // SharedPreferences'e hatırlatıcı kaydet
    private fun saveReminderToPrefs(context: Context, notificationId: Int, reminder: Reminder) {
        val gson = Gson()
        val reminderJson = gson.toJson(reminder)

        val reminderPrefs = context.getSharedPreferences("reminders_prefs", Context.MODE_PRIVATE)
        with(reminderPrefs.edit()) {
            putString("reminder_$notificationId", reminderJson)
            apply()
        }
        Log.d(TAG, "Saved reminder to preferences: $notificationId - ${reminder.title}")
    }

    // SharedPreferences'dan hatırlatıcı yükle
    private fun loadReminderFromPrefs(context: Context, notificationId: Int): Reminder? {
        val reminderPrefs = context.getSharedPreferences("reminders_prefs", Context.MODE_PRIVATE)
        val reminderJson = reminderPrefs.getString("reminder_$notificationId", null) ?: return null

        return try {
            val gson = Gson()
            gson.fromJson(reminderJson, Reminder::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing reminder JSON: ${e.message}")
            null
        }
    }

    // Bir hatırlatıcıyı iptal et
    fun cancelReminder(context: Context, notificationId: Int) {
        try {
            // Work'u iptal et
            WorkManager.getInstance(context).cancelWorkById(UUID.fromString(notificationId.toString()))

            // SharedPreferences'dan sil
            val reminderPrefs = context.getSharedPreferences("reminders_prefs", Context.MODE_PRIVATE)
            with(reminderPrefs.edit()) {
                remove("reminder_$notificationId")
                apply()
            }

            Log.d(TAG, "Reminder canceled: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling reminder: ${e.message}", e)
        }
    }

    // Tüm hatırlatıcıları iptal et
    private fun cancelAllReminders(context: Context) {
        try {
            // Tüm Work'ları iptal et
            WorkManager.getInstance(context).cancelAllWork()

            // Tüm SharedPreferences kayıtlarını temizle
            val reminderPrefs = context.getSharedPreferences("reminders_prefs", Context.MODE_PRIVATE)
            reminderPrefs.edit().clear().apply()

            Log.d(TAG, "All reminders canceled")
        } catch (e: Exception) {
            Log.e(TAG, "Error canceling all reminders: ${e.message}", e)
        }
    }

    // Cihaz yeniden başlatıldıktan sonra hatırlatıcıları yükle
    fun restoreRemindersAfterReboot(context: Context) {
        if (!_notificationsEnabled.value) {
            Log.d(TAG, "Notifications disabled, not restoring reminders")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                restoreAllReminders(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring reminders after reboot", e)
            }
        }
    }

    // Tüm hatırlatıcıları geri yükle
    private suspend fun restoreAllReminders(context: Context) {
        try {
            Log.d(TAG, "Starting to restore all reminders")

            // 1. Önce SharedPreferences'dan hatırlatıcıları yükle (hızlı erişim için)
            val reminderPrefs = context.getSharedPreferences("reminders_prefs", Context.MODE_PRIVATE)
            val allKeys = reminderPrefs.all.keys

            for (key in allKeys) {
                if (key.startsWith("reminder_")) {
                    val notificationId = key.removePrefix("reminder_").toIntOrNull()
                    if (notificationId != null) {
                        val reminder = loadReminderFromPrefs(context, notificationId)
                        if (reminder != null && reminder.isEnabled) {
                            withContext(Dispatchers.Main) {
                                scheduleReminder(context, reminder, notificationId)
                                Log.d(TAG, "Restored reminder from preferences: ${reminder.title}")
                            }
                        }
                    }
                }
            }

            // 2. Ardından Firebase'den hatırlatıcıları yükle (yedek olarak)
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                val querySnapshot = db.collection("reminders")
                    .whereEqualTo("userId", currentUser.uid)
                    .whereEqualTo("isEnabled", true)
                    .get()
                    .await()

                for (document in querySnapshot.documents) {
                    val reminder = document.toObject(Reminder::class.java)
                    if (reminder != null) {
                        withContext(Dispatchers.Main) {
                            scheduleReminder(context, reminder, document.id.hashCode())
                            Log.d(TAG, "Restored reminder from Firebase: ${reminder.title}")
                        }
                    }
                }
            }

            Log.d(TAG, "All reminders restored successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring reminders: ${e.message}", e)
        }
    }
}