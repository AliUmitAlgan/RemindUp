package com.aliumitalgan.remindup.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

data class FriendAchiever(
    val userId: String,
    val displayName: String,
    val photoUrl: String?,
    val progressPercent: Int,
    val status: String,
    val rank: Int?,
    val hasStreak: Boolean = false
)

data class SocialData(
    val avgGroupProgress: Int,
    val friendCount: Int,
    val achievers: List<FriendAchiever>
)

class SocialRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    fun getSocialData(): Flow<Result<SocialData>> = callbackFlow {
        val userId = auth.currentUser?.uid ?: run {
            trySend(Result.failure(Exception("Not authenticated")))
            close()
            return@callbackFlow
        }

        try {
            val userDoc = firestore.collection("users").document(userId).get(Source.SERVER).await()
            val friendIds = (userDoc.get("friendIds") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            val allUserIds = if (friendIds.isEmpty()) {
                listOf(userId)
            } else {
                friendIds + userId
            }

            val achievers = mutableListOf<FriendAchiever>()
            var totalProgress = 0
            var count = 0

            allUserIds.forEach { uid ->
                try {
                    val statsDoc = firestore.collection("user_stats").document(uid).get(Source.SERVER).await()
                    val userDocRef = firestore.collection("users").document(uid).get(Source.SERVER).await()
                    val name = statsDoc.getString("displayName")
                        ?: userDocRef.getString("name")
                        ?: userDocRef.getString("email")?.substringBefore("@")
                        ?: "User"
                    val progress = (statsDoc.getLong("progressPercent")?.toInt() ?: 0).coerceIn(0, 100)
                    val status = statsDoc.getString("status") ?: statusFromProgress(progress)
                    val rank = statsDoc.getLong("rank")?.toInt()
                    val hasStreak = statsDoc.getBoolean("hasStreak") ?: false

                    totalProgress += progress
                    count++

                    achievers.add(
                        FriendAchiever(
                            userId = uid,
                            displayName = name,
                            photoUrl = statsDoc.getString("photoUrl"),
                            progressPercent = progress,
                            status = status,
                            rank = rank,
                            hasStreak = hasStreak
                        )
                    )
                } catch (_: Exception) {}
            }

            val sorted = achievers.sortedByDescending { it.progressPercent }
            val ranked = sorted.mapIndexed { index, a ->
                a.copy(rank = if (index == 0) 1 else null)
            }

            val avgGroup = if (count > 0) totalProgress / count else 0
            trySend(
                Result.success(
                    SocialData(
                        avgGroupProgress = avgGroup,
                        friendCount = count,
                        achievers = ranked
                    )
                )
            )
        } catch (e: FirebaseFirestoreException) {
            val error = if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                Exception("Friends section requires an internet connection.")
            } else {
                e
            }
            trySend(Result.failure(error))
        } catch (e: Exception) {
            trySend(Result.failure(e))
        }

        awaitClose { }
    }.flowOn(Dispatchers.IO)

    suspend fun syncCurrentUserStats(progressPercent: Int) {
        val user = auth.currentUser ?: return
        try {
            firestore.collection("user_stats").document(user.uid).set(
                mapOf(
                    "userId" to user.uid,
                    "displayName" to (user.displayName ?: user.email?.substringBefore("@") ?: "User"),
                    "photoUrl" to (user.photoUrl?.toString()),
                    "progressPercent" to progressPercent,
                    "status" to statusFromProgress(progressPercent),
                    "updatedAt" to com.google.firebase.Timestamp.now()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()
        } catch (_: Exception) {}
    }

    private fun statusFromProgress(progress: Int): String = when {
        progress >= 90 -> "CRUSHING IT!"
        progress >= 70 -> "Almost there"
        progress >= 50 -> "Steady progress"
        else -> "Starting the day"
    }
}
