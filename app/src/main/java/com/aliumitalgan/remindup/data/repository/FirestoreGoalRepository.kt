package com.aliumitalgan.remindup.data.repository

import com.aliumitalgan.remindup.domain.model.Goal
import com.aliumitalgan.remindup.domain.repository.GoalRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreGoalRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : GoalRepository {

    override suspend fun getUserGoals(): Result<List<Pair<String, Goal>>> = runCatching {
        val userId = requireUserId()
        val querySnapshot = firestore.collection("goals")
            .whereEqualTo("userId", userId)
            .get()
            .await()

        querySnapshot.documents.mapNotNull { document ->
            val data = document.data ?: return@mapNotNull null
            val goal = Goal(
                id = document.id,
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                progress = (data["progress"] as? Number)?.toInt() ?: 0,
                category = (data["category"] as? Number)?.toInt() ?: 0,
                isImportant = data["isImportant"] as? Boolean ?: false,
                userId = data["userId"] as? String ?: "",
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
            )
            Pair(document.id, goal)
        }
    }

    override suspend fun addGoal(goal: Goal): Result<String> = runCatching {
        val userId = requireUserId()
        val data = mapOf(
            "title" to goal.title,
            "description" to goal.description,
            "progress" to goal.progress,
            "category" to goal.category,
            "isImportant" to goal.isImportant,
            "userId" to userId,
            "createdAt" to System.currentTimeMillis()
        )
        val docRef = firestore.collection("goals").add(data).await()
        docRef.id
    }

    override suspend fun updateGoalProgress(goalId: String, newProgress: Int): Result<Boolean> = runCatching {
        val userId = requireUserId()
        val docRef = firestore.collection("goals").document(goalId)
        val snapshot = docRef.get().await()
        val userIdStored = snapshot.getString("userId")
            ?: throw IllegalArgumentException("Goal not found: $goalId")
        if (userIdStored != userId) throw SecurityException("Unauthorized")

        docRef.update("progress", newProgress).await()

        if (newProgress >= 100) {
            val userRef = firestore.collection("users").document(userId)
            firestore.runTransaction { transaction ->
                val userDoc = transaction.get(userRef)
                val completedGoals = (userDoc.getLong("completedGoals") ?: 0) + 1
                transaction.update(userRef, "completedGoals", completedGoals)
            }.await()
        }
        true
    }

    override suspend fun deleteGoal(goalId: String): Result<Unit> = runCatching {
        val userId = requireUserId()
        val docRef = firestore.collection("goals").document(goalId)
        val snapshot = docRef.get().await()
        val userIdStored = snapshot.getString("userId")
            ?: throw IllegalArgumentException("Goal not found: $goalId")
        if (userIdStored != userId) throw SecurityException("Unauthorized")

        docRef.delete().await()
    }

    override suspend fun getOverallProgress(): Result<Float> = runCatching {
        val goalsResult = getUserGoals()
        val goals = goalsResult.getOrThrow()
        if (goals.isEmpty()) return@runCatching 0f
        goals.sumOf { it.second.progress }.toFloat() / goals.size / 100f
    }

    private fun requireUserId(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated")
}
