package com.aliumitalgan.remindup.utils

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import com.aliumitalgan.remindup.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AuthUtils {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Email ve şifre ile kayıt olma
    suspend fun registerWithEmail(email: String, password: String, name: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                // Firestore'a kullanıcı bilgilerini kaydet
                val user = User(
                    userId = firebaseUser.uid,
                    email = email,
                    name = name
                )
                db.collection("users").document(firebaseUser.uid).set(user).await()
                Result.success(firebaseUser)
            } else {
                Result.failure(Exception("Kullanıcı kaydı başarısız oldu"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Email ve şifre ile giriş yapma
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                Result.success(firebaseUser)
            } else {
                Result.failure(Exception("Giriş başarısız oldu"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Google ile giriş yapma
    fun getGoogleSignInClient(context: Context, webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // Google SignIn Intent başlatma
    fun signInWithGoogle(googleSignInClient: GoogleSignInClient, launcher: ActivityResultLauncher<Intent>) {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    // Google SignIn sonucunu işleme
    suspend fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>): Result<FirebaseUser> {
        return try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()
                val firebaseUser = authResult.user

                if (firebaseUser != null) {
                    // Firestore'a kullanıcı bilgilerini kaydet veya güncelle
                    val user = User(
                        userId = firebaseUser.uid,
                        email = firebaseUser.email ?: "",
                        name = firebaseUser.displayName ?: ""
                    )
                    db.collection("users").document(firebaseUser.uid).set(user).await()
                    Result.success(firebaseUser)
                } else {
                    Result.failure(Exception("Google ile giriş başarısız oldu"))
                }
            } else {
                Result.failure(Exception("Google ID Token alınamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Çıkış yapma
    fun logout() {
        auth.signOut()
    }

    // Mevcut kullanıcıyı kontrol etme
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}