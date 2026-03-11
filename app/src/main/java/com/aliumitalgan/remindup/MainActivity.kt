package com.aliumitalgan.remindup

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.compose.rememberNavController
import com.aliumitalgan.remindup.core.di.AppContainer
import com.aliumitalgan.remindup.core.di.LocalAppContainer
import com.aliumitalgan.remindup.ui.theme.RemindUpTheme
import com.aliumitalgan.remindup.utils.LanguageManager
import com.aliumitalgan.remindup.utils.NotificationNavigationState
import com.aliumitalgan.remindup.utils.NotificationUtils
import com.aliumitalgan.remindup.utils.ThemeManager
import java.util.Locale

val LocalLanguage = compositionLocalOf { mutableStateOf(LanguageManager.LANGUAGE_TURKISH) }

class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate started")
        hideStatusBar()

        NotificationUtils.createNotificationChannels(this)
        NotificationUtils.loadNotificationState(this)
        NotificationUtils.resetAppLaunchState()
        ThemeManager.setDarkTheme(isSystemDarkMode(configuration = resources.configuration))
        ThemeManager.loadDynamicAccentsState(this)
        LanguageManager.loadSavedLanguage(this)
        NotificationNavigationState.updateFromIntent(intent)

        setContent {
            val currentLanguage by LanguageManager.currentLanguage
            val languageState = remember { mutableStateOf(currentLanguage) }
            val appContainer = remember { AppContainer(applicationContext) }
            val dynamicAccentsEnabled by ThemeManager.dynamicAccentsEnabled
            val systemIsDarkTheme = isSystemInDarkTheme()

            LaunchedEffect(currentLanguage) {
                languageState.value = currentLanguage
            }

            LaunchedEffect(systemIsDarkTheme) {
                ThemeManager.setDarkTheme(systemIsDarkTheme)
            }

            CompositionLocalProvider(
                LocalLanguage provides languageState,
                LocalAppContainer provides appContainer
            ) {
                key(currentLanguage) {
                    RemindUpTheme(
                        darkTheme = systemIsDarkTheme,
                        dynamicColor = dynamicAccentsEnabled
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            RemindUpApp()
                        }
                    }
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideStatusBar()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        NotificationNavigationState.updateFromIntent(intent)
    }

    override fun attachBaseContext(newBase: Context) {
        val sharedPrefs = newBase.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("language_code", "tr") ?: "tr"

        val locale = when (languageCode) {
            "en" -> Locale.ENGLISH
            else -> Locale("tr")
        }

        Locale.setDefault(locale)

        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    private fun hideStatusBar() {
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.statusBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun isSystemDarkMode(configuration: Configuration): Boolean {
        return (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}

@Composable
fun RemindUpApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val goalCelebrationPayload by NotificationNavigationState.goalCelebrationPayload

    var hasNotificationPermission by remember {
        mutableStateOf(NotificationUtils.checkNotificationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            NotificationUtils.saveNotificationState(context, true)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(goalCelebrationPayload) {
        val payload = goalCelebrationPayload ?: return@LaunchedEffect
        val route = Screen.GoalCelebration.createRoute(
            goalId = payload.goalId,
            goalTitle = payload.goalTitle,
            bonusXp = payload.bonusXp
        )
        navController.navigate(route) {
            launchSingleTop = true
        }
        NotificationNavigationState.consumeGoalCelebrationPayload()
    }

    AppNavigation(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = Modifier.fillMaxSize()
    )
}
