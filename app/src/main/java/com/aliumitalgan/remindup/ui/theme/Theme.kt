package com.aliumitalgan.remindup.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.utils.LocaleWrapper

// Modern renk paleti
private val LightColors = lightColorScheme(
    primary = Color(0xFF4D61FF),          // Modern mavi
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E4FF),
    onPrimaryContainer = Color(0xFF0019A9),

    secondary = Color(0xFF32D74B),        // Yeşil
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FFDC),
    onSecondaryContainer = Color(0xFF002109),

    tertiary = Color(0xFFFF9F0A),         // Turuncu
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0B7),
    onTertiaryContainer = Color(0xFF451C00),

    error = Color(0xFFEF4444),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFF9FAFC),
    onBackground = Color(0xFF121826),

    surface = Color.White,
    onSurface = Color(0xFF121826),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF44474F)
)

// Koyu tema
private val DarkColors = darkColorScheme(
    primary = Color(0xFF9EADFF),          // Açık mavi
    onPrimary = Color(0xFF002397),
    primaryContainer = Color(0xFF3B4DD7),
    onPrimaryContainer = Color(0xFFE0E4FF),

    secondary = Color(0xFF8DF79E),        // Açık yeşil
    onSecondary = Color(0xFF003915),
    secondaryContainer = Color(0xFF005321),
    onSecondaryContainer = Color(0xFFD1FFDC),

    tertiary = Color(0xFFFFBE63),         // Açık turuncu
    onTertiary = Color(0xFF452B00),
    tertiaryContainer = Color(0xFF624000),
    onTertiaryContainer = Color(0xFFFFDCBE),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF0B0F14),
    onBackground = Color(0xFFE2E8F0),

    surface = Color(0xFF131923),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFC5C6D0)
)

// Modern tipografi
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

// Modern şekiller ve köşe yuvarlaklıkları
val Shapes = Shapes(
    small = androidx.compose.material3.Shapes().small.copy(
        topStart = androidx.compose.foundation.shape.CornerSize(SweetBubblyThemeTokens.BubbleCorner),
        topEnd = androidx.compose.foundation.shape.CornerSize(SweetBubblyThemeTokens.BubbleCorner),
        bottomStart = androidx.compose.foundation.shape.CornerSize(SweetBubblyThemeTokens.BubbleCorner),
        bottomEnd = androidx.compose.foundation.shape.CornerSize(SweetBubblyThemeTokens.BubbleCorner)
    ),
    medium = androidx.compose.material3.Shapes().medium.copy(
        topStart = androidx.compose.foundation.shape.CornerSize(SweetBubblyThemeTokens.BubbleCorner),
        topEnd = androidx.compose.foundation.shape.CornerSize(SweetBubblyThemeTokens.BubbleCorner),
        bottomStart = androidx.compose.foundation.shape.CornerSize(SweetBubblyThemeTokens.BubbleCorner),
        bottomEnd = androidx.compose.foundation.shape.CornerSize(SweetBubblyThemeTokens.BubbleCorner)
    ),
    large = androidx.compose.material3.Shapes().large.copy(
        topStart = androidx.compose.foundation.shape.CornerSize(SweetBubblyThemeTokens.BubbleCorner),
        topEnd = androidx.compose.foundation.shape.CornerSize(SweetBubblyThemeTokens.BubbleCorner),
        bottomStart = androidx.compose.foundation.shape.CornerSize(SweetBubblyThemeTokens.BubbleCorner),
        bottomEnd = androidx.compose.foundation.shape.CornerSize(SweetBubblyThemeTokens.BubbleCorner)
    )
)

// Constants for common values
object AppDimensions {
    // Spacing
    val spacingXXXS = 2.dp
    val spacingXXS = 4.dp
    val spacingXS = 8.dp
    val spacingSM = 12.dp
    val spacingMD = 16.dp
    val spacingLG = 24.dp
    val spacingXL = 32.dp
    val spacingXXL = 48.dp
    val spacingXXXL = 64.dp

    // Component sizes
    val buttonHeight = 48.dp
    val inputHeight = 56.dp
    val iconSizeSmall = 16.dp
    val iconSizeMedium = 24.dp
    val iconSizeLarge = 32.dp

    // Elevations
    val elevationLow = 2.dp
    val elevationMedium = 4.dp
    val elevationHigh = 8.dp
    val elevationXHigh = 16.dp
}

// Common semantic colors
object AppColors {
    val success = Color(0xFF22C55E)
    val warning = Color(0xFFF59E0B)
    val error = Color(0xFFEF4444)
    val info = Color(0xFF3B82F6)
}

@Composable
fun RemindUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    forceDarkTheme: Boolean? = null,
    dynamicColor: Boolean = false, // Dynamic color özelliği Android 12+ için
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val useDarkTheme = when {
        forceDarkTheme != null -> forceDarkTheme
        else -> darkTheme
    }

    // Dynamic color - Android 12+ cihazlarda sistem renklerini kullan
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDarkTheme -> DarkColors
        else -> LightColors
    }

    val languageState by com.aliumitalgan.remindup.utils.LanguageManager.currentLanguage

    // Dil değişimlerini uygula
    LocaleWrapper.ProvideLocale(languageCode = languageState) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}

