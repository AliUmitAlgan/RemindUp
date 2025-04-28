package com.aliumitalgan.remindup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aliumitalgan.remindup.ui.theme.RemindUpTheme
import com.aliumitalgan.remindup.utils.NotificationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bildirim kanalını oluştur
        NotificationUtils.createNotificationChannel(this)

        // Kullanıcı oturum durumunu kontrol et
        val currentUser = FirebaseAuth.getInstance().currentUser

        setContent {
            RemindUpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Kullanıcı oturum açtıysa ana ekrana, açmadıysa giriş ekranına yönlendir
                    RemindUpApp(currentUser)
                }
            }
        }
    }
}

@Composable
fun RemindUpApp(currentUser: FirebaseUser?) {
    val navController = rememberNavController()

    // Kullanıcı oturum açtıysa başlangıç sayfası ana sayfa, değilse giriş sayfası
    val startDestination = if (currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    AppNavigation(
        navController = navController,
        startDestination = startDestination
    )
}