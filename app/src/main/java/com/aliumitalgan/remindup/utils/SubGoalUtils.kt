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

    // Alt hedef ekle
    suspend fun addSubGoal(subGoal: SubGoal): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No user logged in")
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
            }

            // Kullanıcı ID'sini kontrol et/güncelle
            val completeSubGoal = if (subGoal.userId.isEmpty()) {
                subGoal.copy(userId = currentUser.uid)
            } else {
                subGoal
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

    // Bir hedefe ait alt hedefleri getir
    suspend fun getSubGoalsForParent(parentGoalId: String): Result<List<Pair<String, SubGoal>>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e(TAG, "No user logged in")
                return Result.failure(Exception("Kullanıcı oturum açmamış"))
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
                        Log.d(TAG, "Found subgoal: ${document.id} -> $subGoal")
                        subGoals.add(Pair(document.id, subGoal))
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

    // Alt hedef güncelle (tamamlandı durumunu değiştirme)
    suspend fun updateSubGoalStatus(subGoalId: String, completed: Boolean): Result<Boolean> {
        return try {
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

    // Alt hedef sil
    suspend fun deleteSubGoal(subGoalId: String): Result<Boolean> {
        return try {
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

    // Bir hedefe ait tüm alt hedefleri sil (hedef silindiğinde)
    suspend fun deleteAllSubGoalsForParent(parentGoalId: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Deleting all subgoals for parent: $parentGoalId")

            val batch = db.batch()
            val querySnapshot = db.collection("subgoals")
                .whereEqualTo("parentGoalId", parentGoalId)
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