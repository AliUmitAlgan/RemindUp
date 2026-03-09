package com.aliumitalgan.remindup.data.repository

import com.aliumitalgan.remindup.domain.model.User
import com.aliumitalgan.remindup.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override suspend fun registerWithEmail(email: String, password: String, name: String): Result<User> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw IllegalStateException("Registration failed")
        val user = User(
            userId = firebaseUser.uid,
            email = email,
            name = name
        )
        firestore.collection("users").document(firebaseUser.uid)
            .set(mapOf("userId" to user.userId, "email" to user.email, "name" to user.name))
            .await()
        user
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<User> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw IllegalStateException("Login failed")
        User(
            userId = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            name = firebaseUser.displayName ?: ""
        )
    }

    override suspend fun handleGoogleSignIn(idToken: String): Result<User> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val firebaseUser = result.user ?: throw IllegalStateException("Google sign-in failed")
        val user = User(
            userId = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            name = firebaseUser.displayName ?: ""
        )
        firestore.collection("users").document(firebaseUser.uid)
            .set(mapOf("userId" to user.userId, "email" to user.email, "name" to user.name))
            .await()
        user
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    override fun logout() {
        auth.signOut()
    }

    override fun getCurrentUser(): User? {
        val fbUser = auth.currentUser ?: return null
        return User(
            userId = fbUser.uid,
            email = fbUser.email ?: "",
            name = fbUser.displayName ?: ""
        )
    }
}
