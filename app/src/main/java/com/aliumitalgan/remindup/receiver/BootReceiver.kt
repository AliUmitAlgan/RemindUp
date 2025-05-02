package com.aliumitalgan.remindup.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
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
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON") {

            Log.d(TAG, "Cihaz yeniden başlatıldı. Hatırlatıcılar yeniden yükleniyor.")

            // NotificationUtils'in başlatılması
            NotificationUtils.createNotificationChannel(context)

            // Bildirim durumunu hafızaya yükle
            val areNotificationsEnabled = NotificationUtils.loadNotificationState(context)
            if (!areNotificationsEnabled) {
                Log.d(TAG, "Bildirimler devre dışı, hatırlatıcılar yüklenmeyecek")
                return
            }

            // Hatırlatıcıları yeniden zamanla - sadece bildirimler etkinse
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val remindersResult = ReminderUtils.getUserReminders()

                    if (remindersResult.isSuccess) {
                        val reminders = remindersResult.getOrDefault(emptyList())
                        Log.d(TAG, "Toplam ${reminders.size} hatırlatıcı yeniden yüklenecek")

                        // Dispatchers.Main yerine Dispatchers.Default kullan - Main thread UI'ye bağlı
                        // ve boot sırasında UI olmayabilir
                        withContext(Dispatchers.Default) {
                            for ((id, reminder) in reminders) {
                                if (reminder.isEnabled) {
                                    NotificationUtils.scheduleReminder(
                                        context,
                                        reminder,
                                        id.hashCode()
                                    )
                                    Log.d(TAG, "Hatırlatıcı yeniden zamanlandı: ${reminder.title}")
                                } else {
                                    Log.d(TAG, "Devre dışı hatırlatıcı atlandı: ${reminder.title}")
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Hatırlatıcılar alınamadı: ${remindersResult.exceptionOrNull()?.message}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Hatırlatıcıları yeniden yükleme hatası: ${e.message}", e)
                }
            }

            // Kullanıcının son girişini güncellemek için
            try {
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    // Kullanıcı giriş yapmışsa, bilgilerini güncelle
                    // Bu, uygulama ilk açıldığında Firebase Auth'un doğru durumda olmasını sağlar
                    Log.d(TAG, "Kullanıcı giriş durumu güncelleniyor: ${currentUser.uid}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Kullanıcı durumunu güncelleme hatası: ${e.message}", e)
            }
        }
    }
}