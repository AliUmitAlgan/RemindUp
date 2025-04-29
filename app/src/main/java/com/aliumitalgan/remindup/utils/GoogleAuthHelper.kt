package com.aliumitalgan.remindup.utils

import android.content.Context
import android.content.Intent
import android.util.Log
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
    private const val TAG = "GoogleAuthHelper"

    // Google SignIn Client'ı oluştur
    fun getGoogleSignInClient(context: Context, webClientId: String): GoogleSignInClient {
        Log.d(TAG, "Web Client ID: $webClientId")

        // Default_sign_in yerine daha fazla kapsam isteyerek daha fazla yetki alıyoruz
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()

        // Client'ı oluştururken, önceki oturumlarda sorun olmaması için önce sign out yapalım
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut() // Önceki oturumları temizle

        return client
    }

    // Google ile giriş yap
    fun signIn(launcher: ActivityResultLauncher<Intent>, googleSignInClient: GoogleSignInClient) {
        try {
            // Temiz bir başlangıç için önce sign out yapalım
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                Log.d(TAG, "SignIn başlatılıyor...")
                launcher.launch(signInIntent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "SignIn başlatma hatası: ${e.message}", e)
            throw e
        }
    }

    // Google giriş sonucunu işle
    suspend fun handleSignInResult(task: Task<GoogleSignInAccount>): Result<FirebaseUser> {
        return try {
            val account = task.getResult(ApiException::class.java)
            Log.d(TAG, "Google hesabı başarıyla alındı: ${account.email}")

            val idToken = account.idToken
            Log.d(TAG, "ID Token alındı: ${idToken?.take(10)}...")  // Token'ın ilk 10 karakterini göster

            if (idToken != null) {
                // Firebase ile kimlik doğrulama
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                Log.d(TAG, "Firebase kimlik doğrulama başlatılıyor...")
                val result = firebaseAuthWithGoogle(credential)

                if (result.isSuccess) {
                    // Firestore'a kullanıcı bilgilerini kaydet
                    val firebaseUser = result.getOrNull()!!
                    Log.d(TAG, "Firebase kimlik doğrulama başarılı: ${firebaseUser.email}")
                    saveUserToFirestore(firebaseUser, account)
                    Result.success(firebaseUser)
                } else {
                    Log.e(TAG, "Firebase kimlik doğrulama başarısız: ${result.exceptionOrNull()?.message}")
                    Result.failure(result.exceptionOrNull() ?: Exception("Google ile giriş başarısız oldu"))
                }
            } else {
                Log.e(TAG, "Google ID Token alınamadı")
                Result.failure(Exception("Google ID Token alınamadı"))
            }
        } catch (e: ApiException) {
            Log.e(TAG, "Google sign in ApiException: ${e.statusCode} - ${e.message}", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Google sign in Exception: ${e.message}", e)
            Result.failure(e)
        }
    }

    // Firebase ile Google kimlik doğrulama
    private suspend fun firebaseAuthWithGoogle(credential: AuthCredential): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                Log.d(TAG, "firebaseAuthWithGoogle:success")
                Result.success(firebaseUser)
            } else {
                Log.w(TAG, "firebaseAuthWithGoogle:failure - user null")
                Result.failure(Exception("Firebase ile kimlik doğrulama başarısız oldu"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "firebaseAuthWithGoogle:failure", e)
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
            Log.d(TAG, "Kullanıcı Firestore'a kaydedildi: ${firebaseUser.email}")
        } catch (e: Exception) {
            // Hata durumunda sadece loglama yap, kritik bir işlem değil
            Log.e(TAG, "Kullanıcı Firestore'a kaydedilemedi: ${e.message}", e)
        }
    }

    // Google hesabından çıkış yap
    suspend fun signOut(context: Context, webClientId: String) {
        try {
            // Firebase'den çıkış
            auth.signOut()
            Log.d(TAG, "Firebase'den çıkış yapıldı")

            // Google'dan çıkış
            val googleSignInClient = getGoogleSignInClient(context, webClientId)
            googleSignInClient.signOut().await()
            Log.d(TAG, "Google'dan çıkış yapıldı")
        } catch (e: Exception) {
            Log.e(TAG, "Google çıkış yapılamadı: ${e.message}", e)
        }
    }

    // Google oturum durumunu kontrol et
    fun isSignedIn(context: Context): Boolean {
        val lastSignedInAccount = GoogleSignIn.getLastSignedInAccount(context)
        return lastSignedInAccount != null
    }
}