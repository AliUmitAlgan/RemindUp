package com.aliumitalgan.remindup.utils

import android.content.Context
import com.aliumitalgan.remindup.models.Reminder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.*

object ReminderUtils {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Hatırlatıcı ekle
    suspend fun addReminder(reminder: Reminder, context: Context): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Reminder nesnesini güncellenmiş haliyle oluştur
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
            } else {
                Result.failure(Exception("Kullanıcı oturum açmamış"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kullanıcının hatırlatıcılarını getir
    suspend fun getUserReminders(): Result<List<Pair<String, Reminder>>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val querySnapshot = db.collection("reminders")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                val remindersList = mutableListOf<Pair<String, Reminder>>()
                for (document in querySnapshot.documents) {
                    val reminder = document.toObject(Reminder::class.java)
                    if (reminder != null) {
                        remindersList.add(Pair(document.id, reminder))
                    }
                }

                // Zamanına göre sırala
                remindersList.sortWith { a, b ->
                    val timeA = a.second.time
                    val timeB = b.second.time
                    timeA.compareTo(timeB)
                }

                Result.success(remindersList)
            } else {
                Result.failure(Exception("Kullanıcı oturum açmamış"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hatırlatıcı güncelle
    suspend fun updateReminder(reminderId: String, reminder: Reminder, context: Context): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Önce eski hatırlatıcıyı iptal et
                NotificationUtils.cancelReminder(context, reminderId.hashCode())

                // Firestore'daki hatırlatıcıyı güncelle
                db.collection("reminders")
                    .document(reminderId)
                    .set(reminder)
                    .await()

                // Yeni hatırlatıcıyı zamanla
                NotificationUtils.scheduleReminder(
                    context,
                    reminder,
                    reminderId.hashCode()
                )

                Result.success(true)
            } else {
                Result.failure(Exception("Kullanıcı oturum açmamış"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Hatırlatıcı sil
    suspend fun deleteReminder(reminderId: String, context: Context): Result<Boolean> {
        return try {
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

    // Motivasyon mesajları listesi
    private val motivationalMessages = listOf(
        "Harika gidiyorsun, devam et!",
        "Her küçük adım, başarıya giden yolda bir ilerlemedir!",
        "Bugün dünden daha iyisin, yarın da bugünden daha iyi olacaksın!",
        "Azim ve kararlılık, başarının anahtarıdır!",
        "Zorluklar seni güçlendirir!",
        "Sen yapabilirsin!",
        "Her gün bir öncekinden daha iyi olmak için bir fırsattır!",
        "Hedeflerine ulaşmak için her gün bir adım at!",
        "Küçük adımlar büyük değişimler yaratır!",
        "Gelişim bir yolculuktur, anın tadını çıkar!"
    )

    // Rastgele motivasyon mesajı al
    fun getRandomMotivationalMessage(): String {
        return motivationalMessages[Random().nextInt(motivationalMessages.size)]
    }
}