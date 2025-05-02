package com.aliumitalgan.remindup.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class SubGoal(
    val id: String = "",
    val title: String = "", // Boş varsayılan değer Firebase için
    val completed: Boolean = false,
    val parentGoalId: String = "",
    val userId: String = ""
) {
    // Firebase için gerekli boş yapıcı
    constructor() : this(
        id = "",
        title = "",
        completed = false,
        parentGoalId = "",
        userId = ""
    )
}

object SubGoalUtils {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private const val TAG = "SubGoalUtils"

    // Alt hedef ekle - Enhanced with security checks
    suspend fun addSubGoal(subGoal: SubGoal): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No user logged in")
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }

            // Always ensure userId is set to current user's ID
            val completeSubGoal = subGoal.copy(userId = currentUser.uid)

            // Verify the parent goal belongs to the current user
            if (completeSubGoal.parentGoalId.isNotEmpty()) {
                try {
                    val parentGoal = db.collection("goals")
                        .document(completeSubGoal.parentGoalId)
                        .get()
                        .await()

                    if (!parentGoal.exists()) {
                        return Result.failure(Exception("Parent goal not found"))
                    }

                    val parentGoalUserId = parentGoal.getString("userId")
                    if (parentGoalUserId != currentUser.uid) {
                        return Result.failure(Exception("Permission denied: The parent goal doesn't belong to you"))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error verifying parent goal: ${e.message}", e)
                    return Result.failure(Exception("Failed to verify parent goal ownership"))
                }
            }

            Log.d(TAG, "Adding subgoal with ID: ${completeSubGoal.id}, title: ${completeSubGoal.title}, parentId: ${completeSubGoal.parentGoalId}")

            // Doküman referansını oluştur ve ID'yi belirt
            val docRef = db.collection("subgoals").document(completeSubGoal.id)

            // Veriyi set et ve bekle
            docRef.set(completeSubGoal).await()

            Log.d(TAG, "Successfully added subgoal with ID: ${completeSubGoal.id}")
            Result.success(completeSubGoal.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error adding sub-goal: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Bir hedefe ait alt hedefleri getir - Enhanced with security checks
    suspend fun getSubGoalsForParent(parentGoalId: String): Result<List<Pair<String, SubGoal>>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No user logged in")
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }

            // First, verify the parent goal belongs to the current user
            try {
                val parentGoal = db.collection("goals")
                    .document(parentGoalId)
                    .get()
                    .await()

                if (!parentGoal.exists()) {
                    return Result.failure(Exception("Parent goal not found"))
                }

                val parentGoalUserId = parentGoal.getString("userId")
                if (parentGoalUserId != currentUser.uid) {
                    return Result.failure(Exception("Permission denied: The parent goal doesn't belong to you"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error verifying parent goal: ${e.message}", e)
                return Result.failure(Exception("Failed to verify parent goal ownership"))
            }

            Log.d(TAG, "Fetching subgoals for parentId: $parentGoalId, userId: ${currentUser.uid}")

            val querySnapshot = db.collection("subgoals")
                .whereEqualTo("parentGoalId", parentGoalId)
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .await()

            val subGoals = mutableListOf<Pair<String, SubGoal>>()

            for (document in querySnapshot.documents) {
                try {
                    val subGoal = document.toObject(SubGoal::class.java)
                    if (subGoal != null) {
                        // Double-check that the subgoal belongs to the current user
                        if (subGoal.userId == currentUser.uid) {
                            Log.d(TAG, "Found subgoal: ${document.id} -> $subGoal")
                            subGoals.add(Pair(document.id, subGoal))
                        }
                    } else {
                        Log.w(TAG, "Null subgoal data for document: ${document.id}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error converting document ${document.id}: ${e.message}")
                }
            }

            Log.d(TAG, "Total subgoals found: ${subGoals.size}")
            Result.success(subGoals)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching subgoals: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Alt hedef güncelle (tamamlandı durumunu değiştirme) - Enhanced with security checks
    suspend fun updateSubGoalStatus(subGoalId: String, completed: Boolean): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No user logged in")
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }

            // Verify the subgoal belongs to the current user
            val subGoalDoc = db.collection("subgoals")
                .document(subGoalId)
                .get()
                .await()

            if (!subGoalDoc.exists()) {
                return Result.failure(Exception("SubGoal not found"))
            }

            val subGoalUserId = subGoalDoc.getString("userId")
            if (subGoalUserId != currentUser.uid) {
                return Result.failure(Exception("Permission denied: This subgoal doesn't belong to you"))
            }

            Log.d(TAG, "Updating subgoal status: $subGoalId to completed=$completed")

            db.collection("subgoals")
                .document(subGoalId)
                .update("completed", completed)
                .await()

            Log.d(TAG, "Successfully updated subgoal: $subGoalId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating subgoal status: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Alt hedef sil - Enhanced with security checks
    suspend fun deleteSubGoal(subGoalId: String): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No user logged in")
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }

            // Verify the subgoal belongs to the current user
            val subGoalDoc = db.collection("subgoals")
                .document(subGoalId)
                .get()
                .await()

            if (!subGoalDoc.exists()) {
                return Result.failure(Exception("SubGoal not found"))
            }

            val subGoalUserId = subGoalDoc.getString("userId")
            if (subGoalUserId != currentUser.uid) {
                return Result.failure(Exception("Permission denied: This subgoal doesn't belong to you"))
            }

            Log.d(TAG, "Deleting subgoal: $subGoalId")

            db.collection("subgoals")
                .document(subGoalId)
                .delete()
                .await()

            Log.d(TAG, "Successfully deleted subgoal: $subGoalId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting subgoal: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Bir hedefe ait tüm alt hedefleri sil (hedef silindiğinde) - Enhanced with security checks
    suspend fun deleteAllSubGoalsForParent(parentGoalId: String): Result<Boolean> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No user logged in")
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }

            // First, verify the parent goal belongs to the current user
            try {
                val parentGoal = db.collection("goals")
                    .document(parentGoalId)
                    .get()
                    .await()

                if (!parentGoal.exists()) {
                    return Result.failure(Exception("Parent goal not found"))
                }

                val parentGoalUserId = parentGoal.getString("userId")
                if (parentGoalUserId != currentUser.uid) {
                    return Result.failure(Exception("Permission denied: The parent goal doesn't belong to you"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error verifying parent goal: ${e.message}", e)
                return Result.failure(Exception("Failed to verify parent goal ownership"))
            }

            Log.d(TAG, "Deleting all subgoals for parent: $parentGoalId")

            val batch = db.batch()
            val querySnapshot = db.collection("subgoals")
                .whereEqualTo("parentGoalId", parentGoalId)
                .whereEqualTo("userId", currentUser.uid) // Ensure we only delete user's own subgoals
                .get()
                .await()

            var count = 0
            for (document in querySnapshot.documents) {
                batch.delete(document.reference)
                count++
            }

            batch.commit().await()
            Log.d(TAG, "Successfully deleted $count subgoals for parent: $parentGoalId")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting subgoals for parent: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Alt hedeflerin tamamlanma durumuna göre ana hedefin ilerleme yüzdesini hesapla
    fun calculateProgressFromSubGoals(subGoals: List<SubGoal>): Int {
        if (subGoals.isEmpty()) return 0

        val completedCount = subGoals.count { it.completed }
        return ((completedCount.toFloat() / subGoals.size) * 100).toInt()
    }
}