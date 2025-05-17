package com.aliumitalgan.remindup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.aliumitalgan.remindup.utils.LanguageManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import java.util.Locale

// Composition Local for language changes
val LocalLanguage = compositionLocalOf { mutableStateOf(LanguageManager.LANGUAGE_TURKISH) }

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate started")

        // Bildirim kanalını oluştur
        NotificationUtils.createNotificationChannels(this)

        // Uygulama başlatıldığında bildirim durumunu yükle
        NotificationUtils.loadNotificationState(this)

        // Uygulama başlangıcında otomatik bildirimleri engelleme
        NotificationUtils.resetAppLaunchState()

        // Tema durumunu yükle
        ThemeManager.loadDarkThemeState(this)

        // Dil ayarlarını yükle - güncel dilde çalışması için yapılandırılmış
        LanguageManager.loadSavedLanguage(this)
        Log.d(TAG, "Current language after load: ${LanguageManager.currentLanguage.value}")

        // Android 13+ için bildirim izni iste
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= 33) {
                    requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
                }
            }
        }

        setContent {
            // Dil durumunu dinle ve CompositionLocalProvider ile sağla
            val currentLanguage by LanguageManager.currentLanguage
            val languageState = remember { mutableStateOf(currentLanguage) }
            Log.d(TAG, "SetContent - Current language: $currentLanguage")

            // Tema durumunu doğrudan al
            val isDarkTheme by ThemeManager.isDarkTheme

            // Dil değişikliğini dinle ve state'i güncelle
            LaunchedEffect(currentLanguage) {
                Log.d(TAG, "Language changed in MainActivity: $currentLanguage")
                languageState.value = currentLanguage
            }

            // CompositionLocalProvider ile dil durumunu tüm child composable'lara ilet
            CompositionLocalProvider(LocalLanguage provides languageState) {
                // Dil değişikliğinde UI'ı yeniden oluşturmak için key kullan
                key(currentLanguage) {
                    RemindUpTheme(
                        darkTheme = isDarkTheme
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            // Burada artık FirebaseUser'ı geçmiyoruz, her zaman login ekranından başlayacak
                            RemindUpApp()
                        }
                    }
                }
            }
        }
    }

    // Dil ayarlarının uygulama genelinde tutarlı olmasını sağlayan ek fonksiyon
    override fun attachBaseContext(newBase: Context) {
        // Kaydedilmiş dil ayarını al
        val sharedPrefs = newBase.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("language_code", "tr") ?: "tr"
        Log.d(TAG, "attachBaseContext - Language from prefs: $languageCode")

        // Dile göre yeni bir context oluştur
        val locale = when(languageCode) {
            "en" -> Locale.ENGLISH
            else -> Locale("tr")
        }

        // Locale'i ayarla
        Locale.setDefault(locale)
        Log.d(TAG, "Default Locale set to: ${locale.language}")

        // Yeni configuration oluştur
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        Log.d(TAG, "Configuration locale set to: ${locale.language}")

        // Context oluştur ve parent'a gönder
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
        Log.d(TAG, "Super.attachBaseContext called with proper locale")
    }
}

@Composable
fun RemindUpApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val TAG = "RemindUpApp"

    // Dil durumunu dinle
    val currentLanguage by LanguageManager.currentLanguage
    Log.d(TAG, "RemindUpApp - Current language: $currentLanguage")

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

    // Dil değiştiğinde log
    LaunchedEffect(currentLanguage) {
        Log.d(TAG, "Language changed in RemindUpApp: $currentLanguage")
    }

    // Başlangıç ekranı olarak her zaman Login ekranını ayarla
    val startDestination = Screen.Login.route

    AppNavigation(
        navController = navController,
        startDestination = startDestination
    )
}