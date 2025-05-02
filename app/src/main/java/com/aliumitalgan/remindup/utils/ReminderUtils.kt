package com.aliumitalgan.remindup.utils

import android.content.Context
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.models.Reminder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*
import android.util.Log

object ReminderUtils {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val TAG = "ReminderUtils"

    // Hatırlatıcı ekle - Enhanced with security checks
    suspend fun addReminder(reminder: Reminder, context: Context): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }

            // Önce mevcut hatırlatıcıları kontrol et
            val existingRemindersResult = getUserReminders()

            if (existingRemindersResult.isSuccess) {
                val existingReminders = existingRemindersResult.getOrDefault(emptyList())

                // Aynı başlık ve kategoride hatırlatıcı var mı kontrol et
                val duplicateReminder = existingReminders.find {
                    it.second.title.equals(reminder.title, ignoreCase = true) &&
                            it.second.category == reminder.category
                }

                if (duplicateReminder != null) {
                    // Zaten böyle bir hatırlatıcı varsa hata döndür
                    return Result.failure(Exception("Bu kategoride aynı isimde bir hatırlatıcı zaten var."))
                }
            }

            // Reminder nesnesini güncellenmiş haliyle oluştur - always set the userId
            val reminderWithUser = reminder.copy(userId = currentUser.uid)

            // Firestore'a kaydet
            val docRef = db.collection("reminders").add(reminderWithUser).await()
            val reminderId = docRef.id

            // Hatırlatıcıyı zamanla
            NotificationUtils.scheduleReminder(
                context,
                reminderWithUser,
                reminderId.hashCode()
            )

            Result.success(reminderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kullanıcının hatırlatıcılarını getir - Enhanced with security checks
    suspend fun getUserReminders(): Result<List<Pair<String, Reminder>>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }

            val querySnapshot = db.collection("reminders")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val remindersList = mutableListOf<Pair<String, Reminder>>()
            for (document in querySnapshot.documents) {
                val reminder = document.toObject(Reminder::class.java)
                if (reminder != null) {
                    // Double check that the reminder belongs to the current user
                    if (reminder.userId == currentUser.uid) {
                        remindersList.add(Pair(document.id, reminder))
                    } else {
                        Log.w(TAG, "Farklı kullanıcıya ait hatırlatıcı bulundu, atlanıyor: ${document.id}")
                    }
                }
            }

            // Zamanına göre sırala
            remindersList.sortWith { a, b ->
                val timeA = a.second.time
                val timeB = b.second.time
                timeA.compareTo(timeB)
            }

            Result.success(remindersList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hatırlatıcı güncelle - Enhanced with security checks
    // Hatırlatıcı güncelle - İyileştirilmiş versiyon
    // From ReminderUtils.kt
    suspend fun updateReminder(reminderId: String, reminder: Reminder, context: Context): Result<Boolean> {
        return try {
            Log.d(TAG, "updateReminder başlatıldı: $reminderId, başlık: ${reminder.title}, saat: ${reminder.time}")

            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "Kullanıcı oturum açmamış")
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }

            // Verify the reminder belongs to the current user
            val reminderDoc = db.collection("reminders")
                .document(reminderId)
                .get()
                .await()

            if (!reminderDoc.exists()) {
                Log.e(TAG, "Hatırlatıcı bulunamadı: $reminderId")
                return Result.failure(Exception("Hatırlatıcı bulunamadı"))
            }

            // Belgedeki userId değerini kontrol et
            val reminderUserId = reminderDoc.getString("userId")
            Log.d(TAG, "Hatırlatıcı sahibi: $reminderUserId, mevcut kullanıcı: ${currentUser.uid}")

            if (reminderUserId != currentUser.uid) {
                Log.e(TAG, "İzin reddedildi: Bu hatırlatıcı kullanıcıya ait değil")
                return Result.failure(Exception("İzin reddedildi: Bu hatırlatıcı size ait değil"))
            }

            // Kullanıcı ID'sini doğru bir şekilde ayarla
            val validReminder = reminder.copy(userId = currentUser.uid)
            Log.d(TAG, "Güncellenecek hatırlatıcı: ${validReminder.title}, saat: ${validReminder.time}, userId: ${validReminder.userId}")

            // Önce eski hatırlatıcıyı iptal et
            NotificationUtils.cancelReminder(context, reminderId.hashCode())
            Log.d(TAG, "Eski hatırlatıcı iptal edildi")

            // Firestore'daki hatırlatıcıyı güncelle
            db.collection("reminders")
                .document(reminderId)
                .set(validReminder)
                .await()
            Log.d(TAG, "Firestore belge güncellendi")

            // Yeni hatırlatıcıyı zamanla
            NotificationUtils.scheduleReminder(
                context,
                validReminder,
                reminderId.hashCode()
            )
            Log.d(TAG, "Yeni hatırlatıcı zamanlandı")

            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Hatırlatıcı güncelleme hatası: ${e.message}", e)
            Result.failure(e)
        }
    }
    // Hatırlatıcı sil - Enhanced with security checks
    suspend fun deleteReminder(reminderId: String, context: Context): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }

            // Verify the reminder belongs to the current user
            val reminderDoc = db.collection("reminders")
                .document(reminderId)
                .get()
                .await()

            if (!reminderDoc.exists()) {
                return Result.failure(Exception("Hatırlatıcı bulunamadı"))
            }

            val reminderUserId = reminderDoc.getString("userId")
            if (reminderUserId != currentUser.uid) {
                return Result.failure(Exception("İzin reddedildi: Bu hatırlatıcı size ait değil"))
            }

            // Hatırlatıcıyı iptal et
            NotificationUtils.cancelReminder(context, reminderId.hashCode())

            // Firestore'dan sil
            db.collection("reminders")
                .document(reminderId)
                .delete()
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Rastgele motivasyon mesajı al - string kaynakları kullanarak
    fun getRandomMotivationalMessage(context: Context): String {
        // Mevcut dili kontrol et
        val currentLanguage = LanguageManager.currentLanguage.value
        Log.d(TAG, "Current language for motivational message: $currentLanguage")

        // 1'den 10'a kadar rastgele bir sayı üret (string resource ID'leri için)
        val randomNumber = Random().nextInt(10) + 1

        // Rastgele sayıya göre mesaj ID'sini belirle
        val resourceId = context.resources.getIdentifier(
            "motivational_message$randomNumber",
            "string",
            context.packageName
        )

        // Bulunan resource ID ile mesajı getir
        return context.getString(resourceId)
    }

    // Kullanıcının hatırlatıcıyı değiştirme yetkisi var mı kontrol et
    private suspend fun canUserModifyReminder(reminderId: String): Boolean {
        val currentUser = auth.currentUser ?: return false

        try {
            val reminderDoc = db.collection("reminders")
                .document(reminderId)
                .get()
                .await()

            if (!reminderDoc.exists()) return false

            val userId = reminderDoc.getString("userId")
            return userId == currentUser.uid
        } catch (e: Exception) {
            Log.e(TAG, "Error checking reminder permission: ${e.message}", e)
            return false
        }
    }

    // Belirli bir kullanıcının tüm hatırlatıcılarını sil
    suspend fun deleteAllUserReminders(userId: String, context: Context): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null || currentUser.uid != userId) {
                return Result.failure(Exception("Bu işlemi gerçekleştirme yetkiniz yok"))
            }

            val querySnapshot = db.collection("reminders")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val batch = db.batch()
            var count = 0

            for (document in querySnapshot.documents) {
                // Her hatırlatıcı için bildirimi iptal et
                NotificationUtils.cancelReminder(context, document.id.hashCode())

                // Silme işlemini batch'e ekle
                batch.delete(document.reference)
                count++
            }

            if (count > 0) {
                batch.commit().await()
                Log.d(TAG, "Successfully deleted $count reminders for user: $userId")
            }

            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting user reminders: ${e.message}", e)
            Result.failure(e)
        }
    }
}