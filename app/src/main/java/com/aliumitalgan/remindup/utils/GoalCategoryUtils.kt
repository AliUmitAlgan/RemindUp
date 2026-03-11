package com.aliumitalgan.remindup.utils

import com.aliumitalgan.remindup.domain.model.GoalCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

object GoalCategoryUtils {
    private const val GOAL_CATEGORIES_FIELD = "goalCategories"

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }

    private fun userPreferencesDocument() = firestore.collection("user_preferences")
        .document(requireUserId())

    suspend fun getGoalCategories(): Result<List<GoalCategory>> = runCatching {
        val snapshot = userPreferencesDocument().get().await()
        val rawCategories = snapshot.get(GOAL_CATEGORIES_FIELD) as? List<*> ?: emptyList<Any>()

        rawCategories.mapNotNull { raw ->
            val item = raw as? Map<*, *> ?: return@mapNotNull null
            GoalCategory(
                id = item["id"] as? String ?: return@mapNotNull null,
                name = item["name"] as? String ?: "",
                colorHex = item["colorHex"] as? String ?: "#FFDAB9",
                iconKey = item["iconKey"] as? String ?: "self_care",
                smartRemindersEnabled = item["smartRemindersEnabled"] as? Boolean ?: true,
                createdAt = (item["createdAt"] as? Number)?.toLong() ?: 0L
            )
        }.sortedBy { it.createdAt }
    }

    suspend fun getGoalCategoryById(categoryId: String): Result<GoalCategory?> = runCatching {
        getGoalCategories().getOrThrow().firstOrNull { it.id == categoryId }
    }

    suspend fun saveGoalCategory(category: GoalCategory): Result<Unit> = runCatching {
        val current = getGoalCategories().getOrElse { emptyList() }
        val normalized = category.copy(
            name = category.name.trim(),
            colorHex = normalizeHexColor(category.colorHex)
        )
        val updated = current.filterNot { it.id == normalized.id } + normalized

        userPreferencesDocument()
            .set(
                mapOf(
                    GOAL_CATEGORIES_FIELD to updated.map { it.toMap() }
                ),
                SetOptions.merge()
            )
            .await()
    }

    private fun GoalCategory.toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "colorHex" to normalizeHexColor(colorHex),
            "iconKey" to iconKey,
            "smartRemindersEnabled" to smartRemindersEnabled,
            "createdAt" to createdAt
        )
    }

    private fun normalizeHexColor(value: String): String {
        val cleaned = value.trim().removePrefix("#")
        return when (cleaned.length) {
            6 -> "#$cleaned"
            8 -> "#${cleaned.takeLast(6)}"
            else -> "#FFDAB9"
        }
    }
}

