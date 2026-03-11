package com.aliumitalgan.remindup.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

data class NotificationPreferences(
    val dailyReminders: Boolean = true,
    val goalAchievements: Boolean = true,
    val taskUpdates: Boolean = false,
    val systemAnnouncements: Boolean = true,
    val quietModeEnabled: Boolean = false
)

data class SecurityPreferences(
    val twoFactorEnabled: Boolean = true,
    val biometricEnabled: Boolean = false
)

object UserPreferenceUtils {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    suspend fun getNotificationPreferences(): Result<NotificationPreferences> = runCatching {
        val snapshot = firestore.collection("user_preferences")
            .document(requireUserId())
            .get()
            .await()

        val notifications = snapshot.get("notifications") as? Map<*, *> ?: emptyMap<Any, Any>()

        NotificationPreferences(
            dailyReminders = notifications["dailyReminders"] as? Boolean ?: true,
            goalAchievements = notifications["goalAchievements"] as? Boolean ?: true,
            taskUpdates = notifications["taskUpdates"] as? Boolean ?: false,
            systemAnnouncements = notifications["systemAnnouncements"] as? Boolean ?: true,
            quietModeEnabled = notifications["quietModeEnabled"] as? Boolean ?: false
        )
    }

    suspend fun updateNotificationPreference(field: String, value: Boolean): Result<Unit> = runCatching {
        firestore.collection("user_preferences")
            .document(requireUserId())
            .set(
                mapOf(
                    "notifications" to mapOf(field to value),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun getSecurityPreferences(): Result<SecurityPreferences> = runCatching {
        val snapshot = firestore.collection("user_preferences")
            .document(requireUserId())
            .get()
            .await()

        val security = snapshot.get("security") as? Map<*, *> ?: emptyMap<Any, Any>()

        SecurityPreferences(
            twoFactorEnabled = security["twoFactorEnabled"] as? Boolean ?: true,
            biometricEnabled = security["biometricEnabled"] as? Boolean ?: false
        )
    }

    suspend fun updateSecurityPreference(field: String, value: Boolean): Result<Unit> = runCatching {
        firestore.collection("user_preferences")
            .document(requireUserId())
            .set(
                mapOf(
                    "security" to mapOf(field to value),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }
}
