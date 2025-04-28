package com.aliumitalgan.remindup.utils

import com.aliumitalgan.remindup.models.Goal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ProgressUtils {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Hedefi güncelle
    suspend fun updateGoalProgress(goalId: String, newProgress: Int): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                db.collection("goals")
                    .document(goalId)
                    .update("progress", newProgress)
                    .await()

                // Hedef tamamlandıysa
                if (newProgress >= 100) {
                    // Hedef tamamlama sayısını arttır
                    val userRef = db.collection("users").document(currentUser.uid)
                    db.runTransaction { transaction ->
                        val userDoc = transaction.get(userRef)
                        val completedGoals = userDoc.getLong("completedGoals") ?: 0
                        transaction.update(userRef, "completedGoals", completedGoals + 1)
                    }.await()
                }

                Result.success(true)
            } else {
                Result.failure(Exception("Kullanıcı oturum açmamış"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kullanıcının tüm hedeflerini getir
    suspend fun getUserGoals(): Result<List<Pair<String, Goal>>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val querySnapshot = db.collection("goals")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                val goalsList = mutableListOf<Pair<String, Goal>>()
                for (document in querySnapshot.documents) {
                    val goal = document.toObject(Goal::class.java)
                    if (goal != null) {
                        goalsList.add(Pair(document.id, goal))
                    }
                }

                Result.success(goalsList)
            } else {
                Result.failure(Exception("Kullanıcı oturum açmamış"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kullanıcının tamamlanma oranını hesapla
    suspend fun getOverallProgress(): Result<Float> {
        return try {
            val goalsResult = getUserGoals()
            if (goalsResult.isSuccess) {
                val goals = goalsResult.getOrNull() ?: emptyList()
                if (goals.isEmpty()) return Result.success(0f)

                val totalProgress = goals.sumOf { it.second.progress }
                val overallProgress = totalProgress.toFloat() / goals.size

                Result.success(overallProgress / 100f) // 0.0f ile 1.0f arasında değer döndür
            } else {
                Result.failure(goalsResult.exceptionOrNull() ?: Exception("Hedefler alınamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Tamamlanan hedef sayısını getir
    suspend fun getCompletedGoalsCount(): Result<Int> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val querySnapshot = db.collection("goals")
                    .whereEqualTo("userId", currentUser.uid)
                    .whereGreaterThanOrEqualTo("progress", 100)
                    .get()
                    .await()

                Result.success(querySnapshot.size())
            } else {
                Result.failure(Exception("Kullanıcı oturum açmamış"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}