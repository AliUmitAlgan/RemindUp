package com.aliumitalgan.remindup.data.repository

import com.aliumitalgan.remindup.domain.model.GoalCategory
import com.aliumitalgan.remindup.domain.repository.GoalCategoryRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreGoalCategoryRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : GoalCategoryRepository {

    override suspend fun getGoalCategories(): Result<List<GoalCategory>> = runCatching {
        val userId = requireUserId()
        val fromUsers = runCatching {
            val usersSnapshot = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            parseCategoriesFromUsersDocument(usersSnapshot.data)
        }.getOrDefault(emptyList())

        if (fromUsers.isNotEmpty()) {
            return@runCatching fromUsers.sortedBy { it.createdAt }
        }

        val legacySnapshot = firestore.collection("user_preferences")
            .document(userId)
            .get()
            .await()

        parseCategories(legacySnapshot.get("goalCategories") as? List<*>)
            .sortedBy { it.createdAt }
    }

    override suspend fun getGoalCategoryById(categoryId: String): Result<GoalCategory?> = runCatching {
        getGoalCategories().getOrThrow().firstOrNull { it.id == categoryId }
    }

    override suspend fun saveGoalCategory(category: GoalCategory): Result<Unit> = runCatching {
        val userId = requireUserId()
        val normalized = category.copy(
            name = category.name.trim(),
            colorHex = normalizeHexColor(category.colorHex)
        )
        val current = getGoalCategories().getOrElse { emptyList() }
        val updated = (current.filterNot { it.id == normalized.id } + normalized)
            .sortedBy { it.createdAt }
        val serialized = updated.map { it.toMap() }

        persistGoalCategories(userId = userId, serializedCategories = serialized)
    }

    override suspend fun deleteGoalCategory(categoryId: String): Result<Unit> = runCatching {
        val userId = requireUserId()
        val current = getGoalCategories().getOrElse { emptyList() }
        val updated = current
            .filterNot { it.id == categoryId }
            .sortedBy { it.createdAt }
        val serialized = updated.map { it.toMap() }
        persistGoalCategories(userId = userId, serializedCategories = serialized)
    }

    private fun parseCategoriesFromUsersDocument(data: Map<String, Any>?): List<GoalCategory> {
        val preferences = data?.get("preferences") as? Map<*, *>
        val nestedRaw = preferences?.get("goalCategories") as? List<*>
        if (!nestedRaw.isNullOrEmpty()) {
            return parseCategories(nestedRaw)
        }
        return parseCategories(data?.get("goalCategories") as? List<*>)
    }

    private fun parseCategories(raw: List<*>?): List<GoalCategory> {
        return raw.orEmpty().mapNotNull { item ->
            val map = item as? Map<*, *> ?: return@mapNotNull null
            GoalCategory(
                id = map["id"] as? String ?: return@mapNotNull null,
                name = map["name"] as? String ?: "",
                colorHex = normalizeHexColor(map["colorHex"] as? String ?: "#FFDAB9"),
                iconKey = map["iconKey"] as? String ?: "self_care",
                smartRemindersEnabled = map["smartRemindersEnabled"] as? Boolean ?: true,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: 0L
            )
        }
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

    private suspend fun persistGoalCategories(
        userId: String,
        serializedCategories: List<Map<String, Any>>
    ) {
        val usersWriteSucceeded = runCatching {
            firestore.collection("users")
                .document(userId)
                .set(
                    mapOf(
                        "preferences" to mapOf("goalCategories" to serializedCategories),
                        "preferencesUpdatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    ),
                    SetOptions.merge()
                )
                .await()
        }.isSuccess

        if (!usersWriteSucceeded) {
            firestore.collection("user_preferences")
                .document(userId)
                .set(
                    mapOf(
                        "goalCategories" to serializedCategories,
                        "userId" to userId
                    ),
                    SetOptions.merge()
                )
                .await()
            return
        }

        runCatching {
            firestore.collection("user_preferences")
                .document(userId)
                .set(
                    mapOf(
                        "goalCategories" to serializedCategories,
                        "userId" to userId
                    ),
                    SetOptions.merge()
                )
                .await()
        }
    }

    private fun requireUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
    }
}
