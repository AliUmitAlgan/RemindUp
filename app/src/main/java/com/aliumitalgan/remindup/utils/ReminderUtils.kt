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

    // Hatırlatıcı ekle
    suspend fun addReminder(reminder: Reminder, context: Context): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
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

    // Sabit motivasyon mesajları - direk uygulama içinde tanımlı
    private val turkishMotivationalMessages = listOf(
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

    private val englishMotivationalMessages = listOf(
        "You're doing great, keep it up!",
        "Every small step is progress on the path to success!",
        "You're better today than yesterday, and tomorrow will be even better!",
        "Persistence and determination are the keys to success!",
        "Challenges make you stronger!",
        "You can do it!",
        "Every day is an opportunity to be better than the day before!",
        "Take a step towards your goals every day!",
        "Small steps lead to big changes!",
        "Growth is a journey, enjoy the moment!"
    )

    // Rastgele motivasyon mesajı al - açık ve net dil kontrolü ile
    fun getRandomMotivationalMessage(context: Context): String {
        // Mevcut dili kontrol et
        val currentLanguage = LanguageManager.currentLanguage.value
        Log.d(TAG, "Current language for motivational message: $currentLanguage")

        val messages = if (currentLanguage == LanguageManager.LANGUAGE_ENGLISH) {
            Log.d(TAG, "Using English motivational messages")
            englishMotivationalMessages
        } else {
            Log.d(TAG, "Using Turkish motivational messages")
            turkishMotivationalMessages
        }

        return messages[Random().nextInt(messages.size)]
    }
}