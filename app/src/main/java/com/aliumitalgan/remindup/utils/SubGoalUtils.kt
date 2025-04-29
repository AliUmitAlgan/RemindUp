package com.aliumitalgan.remindup.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Alt hedef veri sınıfı
data class SubGoal(
    val id: String = "",
    val title: String,
    val completed: Boolean = false,
    val parentGoalId: String = "",
    val userId: String = ""
)

object SubGoalUtils {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Alt hedef ekle
    suspend fun addSubGoal(subGoal: SubGoal): Result<String> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // SubGoal nesnesini kullanıcı ID'si ile güncelle
                val subGoalWithUser = subGoal.copy(
                    userId = currentUser.uid
                )

                // Firestore'a kaydet
                val docRef = db.collection("subgoals").add(subGoalWithUser).await()
                Result.success(docRef.id)
            } else {
                Result.failure(Exception("Kullanıcı oturum açmamış"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Bir hedefe ait alt hedefleri getir
    suspend fun getSubGoalsForParent(parentGoalId: String): Result<List<Pair<String, SubGoal>>> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val querySnapshot = db.collection("subgoals")
                    .whereEqualTo("parentGoalId", parentGoalId)
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()

                val subGoalsList = mutableListOf<Pair<String, SubGoal>>()
                for (document in querySnapshot.documents) {
                    val subGoal = document.toObject(SubGoal::class.java)
                    if (subGoal != null) {
                        subGoalsList.add(Pair(document.id, subGoal))
                    }
                }

                Result.success(subGoalsList)
            } else {
                Result.failure(Exception("Kullanıcı oturum açmamış"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Alt hedef güncelle (tamamlandı durumunu değiştirme)
    suspend fun updateSubGoalStatus(subGoalId: String, completed: Boolean): Result<Boolean> {
        return try {
            // Firestore'da subgoal belgesini güncelle
            db.collection("subgoals")
                .document(subGoalId)
                .update("completed", completed)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Alt hedef sil
    suspend fun deleteSubGoal(subGoalId: String): Result<Boolean> {
        return try {
            // Firestore'dan subgoal belgesini sil
            db.collection("subgoals")
                .document(subGoalId)
                .delete()
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Bir hedefe ait tüm alt hedefleri sil (hedef silindiğinde)
    suspend fun deleteAllSubGoalsForParent(parentGoalId: String): Result<Boolean> {
        return try {
            val batch = db.batch()

            // İlgili tüm alt hedefleri bul
            val querySnapshot = db.collection("subgoals")
                .whereEqualTo("parentGoalId", parentGoalId)
                .get()
                .await()

            // Batch işlemi ile hepsini sil
            for (document in querySnapshot.documents) {
                batch.delete(document.reference)
            }

            // Batch işlemini uygula
            batch.commit().await()

            Result.success(true)
        } catch (e: Exception) {
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