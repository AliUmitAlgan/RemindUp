package com.aliumitalgan.remindup

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.aliumitalgan.remindup.ui.theme.RemindUpTheme
import com.aliumitalgan.remindup.utils.NotificationUtils
import com.aliumitalgan.remindup.utils.ThemeManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("RemindUp", "Main Activity onCreate çalıştı")

        // Bildirim kanalını oluştur
        NotificationUtils.createNotificationChannel(this)

        // Uygulama başlatıldığında bildirim durumunu yükle
        NotificationUtils.loadNotificationState(this)

        // Tema durumunu yükle
        ThemeManager.loadDarkThemeState(this)

        // Android 13+ için bildirim izni iste
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= 33) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
                }
            }
        }

        // Kullanıcı oturum durumunu kontrol et
        val currentUser = FirebaseAuth.getInstance().currentUser

        setContent {
            // Tema durumunu doğrudan al
            val isDarkTheme by ThemeManager.isDarkTheme

            RemindUpTheme(
                darkTheme = isDarkTheme
            ) {
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Bildirim izni kontrolü
    var hasNotificationPermission by remember {
        mutableStateOf(NotificationUtils.checkNotificationPermission(context))
    }

    // Bildirim izni isteme launcher'ı
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            NotificationUtils.saveNotificationState(context, true)
        }
    }

    // Android 13+ için bildirim izni kontrolü
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Kullanıcı oturum açtıysa başlangıç sayfası ana sayfa, değilse giriş sayfası
    val startDestination = if (currentUser != null) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    LaunchedEffect(currentUser) {
        Log.d("RemindUp", "Kullanıcı durumu: ${currentUser != null}")
    }

    AppNavigation(
        navController = navController,
        startDestination = startDestination
    )
}