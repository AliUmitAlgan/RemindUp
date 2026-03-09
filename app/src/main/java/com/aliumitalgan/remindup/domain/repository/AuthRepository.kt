package com.aliumitalgan.remindup.domain.repository

import com.aliumitalgan.remindup.domain.model.User

interface AuthRepository {
    suspend fun registerWithEmail(email: String, password: String, name: String): Result<User>
    suspend fun loginWithEmail(email: String, password: String): Result<User>
    suspend fun handleGoogleSignIn(idToken: String): Result<User>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    fun logout()
    fun getCurrentUser(): User?
}
