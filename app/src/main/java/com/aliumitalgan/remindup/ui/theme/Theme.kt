package com.aliumitalgan.remindup.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.utils.LanguageManager
import com.aliumitalgan.remindup.utils.LocaleWrapper
import com.aliumitalgan.remindup.utils.ThemeManager
import android.util.Log

// Typography'yi düzeltin
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    // Material3 Typography gerekli diğer alanlar
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    )
)

// Renk şemalarını güncelleyin
private val LightColors = lightColorScheme(
    primary = BluePrimaryLight,
    secondary = GreenSecondaryLight,
    tertiary = GreenSecondaryLight.copy(alpha = 0.7f),
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
    error = ErrorRed
)

private val DarkColors = darkColorScheme(
    primary = BluePrimaryLight,
    secondary = GreenSecondaryLight,
    tertiary = GreenSecondaryLight.copy(alpha = 0.7f),
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorRed
)

@Composable
fun RemindUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    forceDarkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val TAG = "RemindUpTheme"

    // forceDarkTheme veya darkTheme parametrelerini kullanarak tema durumunu belirleme
    val useDarkTheme = when {
        forceDarkTheme != null -> forceDarkTheme
        else -> {
            // ThemeManager'dan tema durumunu al
            // Eğer ThemeManager'da bir değişiklik olursa takip et
            val themeState by ThemeManager.isDarkTheme

            // Başlangıçta tema durumunu yükle
            LaunchedEffect(Unit) {
                ThemeManager.loadDarkThemeState(context)
            }

            themeState
        }
    }

    // Dil durumunu al
    val languageState by LanguageManager.currentLanguage
    Log.d(TAG, "Current language in theme: $languageState")

    // UI tutarlılığı için gecikmeli recomposition engelleme
    val colorScheme = if (useDarkTheme) DarkColors else LightColors
    val rememberedColorScheme = remember(colorScheme, useDarkTheme) { colorScheme }

    // Aktif dili günlüğe kaydet
    LaunchedEffect(languageState) {
        Log.d(TAG, "Language changed in theme: $languageState")
    }

    // Dil değişikliğinde LocaleWrapper ile komut verme - dil değişikliğini dinle
    LocaleWrapper.ProvideLocale(languageCode = languageState) {
        MaterialTheme(
            colorScheme = rememberedColorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}