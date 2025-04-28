package com.aliumitalgan.remindup.utils

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object GoogleAuthHelper {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Google SignIn Client'ı oluştur
    fun getGoogleSignInClient(context: Context, webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    // Google ile giriş yap
    fun signIn(launcher: ActivityResultLauncher<Intent>, googleSignInClient: GoogleSignInClient) {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    // Google giriş sonucunu işle
    suspend fun handleSignInResult(task: Task<GoogleSignInAccount>): Result<FirebaseUser> {
        return try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                // Firebase ile kimlik doğrulama
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = firebaseAuthWithGoogle(credential)

                if (result.isSuccess) {
                    // Firestore'a kullanıcı bilgilerini kaydet
                    val firebaseUser = result.getOrNull()!!
                    saveUserToFirestore(firebaseUser, account)

                    Result.success(firebaseUser)
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Google ile giriş başarısız oldu"))
                }
            } else {
                Result.failure(Exception("Google ID Token alınamadı"))
            }
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Firebase ile Google kimlik doğrulama
    private suspend fun firebaseAuthWithGoogle(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                Result.success(firebaseUser)
            } else {
                Result.failure(Exception("Firebase ile kimlik doğrulama başarısız oldu"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Kullanıcı bilgilerini Firestore'a kaydet
    private suspend fun saveUserToFirestore(firebaseUser: FirebaseUser, account: GoogleSignInAccount) {
        val user = hashMapOf(
            "userId" to firebaseUser.uid,
            "email" to firebaseUser.email,
            "displayName" to account.displayName,
            "photoUrl" to (account.photoUrl?.toString() ?: ""),
            "lastLogin" to System.currentTimeMillis()
        )

        try {
            // Kullanıcı varsa güncelle, yoksa oluştur
            db.collection("users").document(firebaseUser.uid)
                .set(user).await()
        } catch (e: Exception) {
            // Hata durumunda sadece loglama yap, kritik bir işlem değil
            println("Kullanıcı Firestore'a kaydedilemedi: ${e.message}")
        }
    }

    // Google hesabını çıkış yap
    suspend fun signOut(context: Context, webClientId: String) {
        try {
            // Firebase'den çıkış
            auth.signOut()

            // Google'dan çıkış
            val googleSignInClient = getGoogleSignInClient(context, webClientId)
            googleSignInClient.signOut().await()
        } catch (e: Exception) {
            println("Google çıkış yapılamadı: ${e.message}")
        }
    }
}