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

data class AppearancePreferences(
    val darkModeEnabled: Boolean? = null,
    val dynamicAccentsEnabled: Boolean? = null
)

object UserPreferenceUtils {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    private fun userDocument() = firestore.collection("users")
        .document(requireUserId())

    private fun legacyPreferencesDocument() = firestore.collection("user_preferences")
        .document(requireUserId())

    private suspend fun readCurrentSection(section: String): Map<*, *> {
        val snapshot = userDocument()
            .get()
            .await()

        val preferences = snapshot.get("preferences") as? Map<*, *> ?: emptyMap<Any, Any>()
        val nestedSection = preferences[section] as? Map<*, *>
        if (nestedSection != null) {
            return nestedSection
        }

        return snapshot.get(section) as? Map<*, *> ?: emptyMap<Any, Any>()
    }

    private suspend fun readLegacySection(section: String): Map<*, *> {
        val snapshot = legacyPreferencesDocument()
            .get()
            .await()

        return snapshot.get(section) as? Map<*, *> ?: emptyMap<Any, Any>()
    }

    private suspend fun readSection(section: String): Map<*, *> {
        val current = runCatching { readCurrentSection(section) }
            .getOrDefault(emptyMap<Any, Any>())

        if (current.isNotEmpty()) {
            return current
        }

        return runCatching { readLegacySection(section) }
            .getOrDefault(emptyMap<Any, Any>())
    }

    private suspend fun writeSectionField(section: String, field: String, value: Boolean) {
        userDocument()
            .set(
                mapOf(
                    "preferences" to mapOf(section to mapOf(field to value)),
                    "preferencesUpdatedAt" to FieldValue.serverTimestamp()
                ),
                SetOptions.merge()
            )
            .await()
    }

    suspend fun getNotificationPreferences(): Result<NotificationPreferences> = runCatching {
        val notifications = readSection("notifications")

        NotificationPreferences(
            dailyReminders = notifications["dailyReminders"] as? Boolean ?: true,
            goalAchievements = notifications["goalAchievements"] as? Boolean ?: true,
            taskUpdates = notifications["taskUpdates"] as? Boolean ?: false,
            systemAnnouncements = notifications["systemAnnouncements"] as? Boolean ?: true,
            quietModeEnabled = notifications["quietModeEnabled"] as? Boolean ?: false
        )
    }

    suspend fun updateNotificationPreference(field: String, value: Boolean): Result<Unit> = runCatching {
        writeSectionField("notifications", field, value)
    }

    suspend fun getSecurityPreferences(): Result<SecurityPreferences> = runCatching {
        val security = readSection("security")

        SecurityPreferences(
            twoFactorEnabled = security["twoFactorEnabled"] as? Boolean ?: true,
            biometricEnabled = security["biometricEnabled"] as? Boolean ?: false
        )
    }

    suspend fun updateSecurityPreference(field: String, value: Boolean): Result<Unit> = runCatching {
        writeSectionField("security", field, value)
    }

    suspend fun getAppearancePreferences(): Result<AppearancePreferences> = runCatching {
        val appearance = readSection("appearance")

        AppearancePreferences(
            darkModeEnabled = appearance["darkModeEnabled"] as? Boolean,
            dynamicAccentsEnabled = appearance["dynamicAccentsEnabled"] as? Boolean
        )
    }

    suspend fun updateAppearancePreference(field: String, value: Boolean): Result<Unit> = runCatching {
        writeSectionField("appearance", field, value)
    }
}
