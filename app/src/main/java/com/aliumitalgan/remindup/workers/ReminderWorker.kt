package com.aliumitalgan.remindup.workers

import android.content.Context
import android.util.Log
import androidx.work.*
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.models.ReminderType
import com.aliumitalgan.remindup.utils.NotificationUtils
import com.google.gson.Gson

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    private val TAG = "ReminderWorker"

    override fun doWork(): Result {
        Log.d(TAG, "ReminderWorker started executing")

        try {
            // Giriş verilerini al
            val reminderJson = inputData.getString("REMINDER_DATA")
            val notificationId = inputData.getInt("NOTIFICATION_ID", 0)

            if (reminderJson == null) {
                Log.e(TAG, "Missing reminder data in Worker")
                return Result.failure()
            }

            // Reminder nesnesini oluştur
            val gson = Gson()
            val reminder = gson.fromJson(reminderJson, Reminder::class.java)

            // Bildirimi göster
            NotificationUtils.showNotification(
                applicationContext,
                reminder.title,
                reminder.description.ifEmpty { "Hatırlatma zamanı!" },
                notificationId,
                isImportant = reminder.isImportant // Reminder sınıfına isImportant alanı ekleyin
            )

            Log.d(TAG, "Successfully showed notification for reminder: ${reminder.title}")

            // Eğer bu tek seferlik bir hatırlatıcı ise ve tekrarlanan değilse, işlem tamamlandı
            if (reminder.type == ReminderType.SINGLE) {
                Log.d(TAG, "Single reminder completed: ${reminder.title}")
                return Result.success()
            }

            // PeriodicWorkRequest ile zaten tekrarlama ayarlanmıştır, başka bir şey yapmaya gerek yok
            return Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Error in ReminderWorker: ${e.message}", e)
            return Result.retry()
        }
    }
}