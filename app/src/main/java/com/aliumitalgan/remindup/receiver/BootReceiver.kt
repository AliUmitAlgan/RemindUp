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
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Cihaz yeniden başlatıldı. Hatırlatıcılar yeniden zamanlanıyor.")

            // NotificationUtils'in başlatılması
            NotificationUtils.createNotificationChannel(context)

            // Bildirim durumunu hafızaya yükle
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Önce bildirim durumunu yükle
                    NotificationUtils.loadNotificationState(context)

                    // Eğer bildirimler etkinse, hatırlatıcıları yeniden zamanla
                    if (NotificationUtils.notificationsEnabled.value) {
                        val remindersResult = ReminderUtils.getUserReminders()

                        if (remindersResult.isSuccess) {
                            val reminders = remindersResult.getOrDefault(emptyList())

                            withContext(Dispatchers.Main) {
                                for ((id, reminder) in reminders) {
                                    NotificationUtils.scheduleReminder(
                                        context,
                                        reminder,
                                        id.hashCode()
                                    )

                                    Log.d("BootReceiver", "Hatırlatıcı yeniden zamanlandı: ${reminder.title}")
                                }
                            }
                        } else {
                            Log.e("BootReceiver", "Hatırlatıcılar alınamadı: ${remindersResult.exceptionOrNull()?.message}")
                        }
                    } else {
                        Log.d("BootReceiver", "Bildirimler kapalı olduğu için hatırlatıcılar zamanlanmadı.")
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Hatırlatıcılar yeniden zamanlanırken hata oluştu: ${e.message}")
                }
            }
        }
    }
}