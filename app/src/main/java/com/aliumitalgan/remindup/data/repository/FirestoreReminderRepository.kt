package com.aliumitalgan.remindup.data.repository

import com.aliumitalgan.remindup.domain.model.ReminderRecord
import com.aliumitalgan.remindup.domain.repository.ReminderRepository
import com.aliumitalgan.remindup.domain.service.ReminderScheduler
import com.aliumitalgan.remindup.models.Reminder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreReminderRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val scheduler: ReminderScheduler
) : ReminderRepository {

    override suspend fun getUserReminders(): Result<List<ReminderRecord>> = runCatching {
        val userId = requireUserId()
        val querySnapshot = firestore.collection("reminders")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        querySnapshot.documents.mapNotNull { document ->
            document.toObject(Reminder::class.java)?.let { reminder ->
                ReminderRecord(id = document.id, reminder = reminder)
            }
        }.sortedBy { it.reminder.time }
    }

    override suspend fun addReminder(reminder: Reminder): Result<String> = runCatching {
        val userId = requireUserId()
        val reminderWithUser = reminder.copy(userId = userId)
        val ref = firestore.collection("reminders").add(reminderWithUser).await()

        if (reminderWithUser.isEnabled) {
            scheduler.schedule(ref.id, reminderWithUser)
        }

        ref.id
    }

    override suspend fun updateReminder(reminderId: String, reminder: Reminder): Result<Unit> = runCatching {
        val userId = requireUserId()
        ensureOwner(reminderId, userId)

        val validReminder = reminder.copy(userId = userId)
        firestore.collection("reminders")
            .document(reminderId)
            .set(validReminder)
            .await()

        if (validReminder.isEnabled) {
            scheduler.schedule(reminderId, validReminder)
        } else {
            scheduler.cancel(reminderId)
        }
    }

    override suspend fun deleteReminder(reminderId: String): Result<Unit> = runCatching {
        val userId = requireUserId()
        ensureOwner(reminderId, userId)

        scheduler.cancel(reminderId)
        firestore.collection("reminders")
            .document(reminderId)
            .delete()
            .await()
    }

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User is not authenticated.")
    }

    private suspend fun ensureOwner(reminderId: String, userId: String) {
        val snapshot = firestore.collection("reminders")
            .document(reminderId)
            .get()
            .await()

        if (!snapshot.exists()) {
            throw IllegalArgumentException("Reminder not found.")
        }
        if (snapshot.getString("userId") != userId) {
            throw IllegalStateException("Reminder does not belong to current user.")
        }
    }
}
