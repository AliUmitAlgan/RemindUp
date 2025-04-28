package com.aliumitalgan.remindup.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography // Bu import doğru olmalı
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Typography'yi doğru şekilde tanımlayın
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
    // Material3 Typography gerekli diğer alanları da ekleyin
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    )
)

private val LightColors = lightColorScheme(
    primary = BluePrimary,
    secondary = GreenSecondary,
    tertiary = LightGreenAccent,
    background = LightBlueBackground,
    surface = WhiteUI,
    onPrimary = WhiteUI,
    onSecondary = WhiteUI,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val DarkColors = darkColorScheme(
    primary = BluePrimary,
    secondary = GreenSecondary,
    tertiary = LightGreenAccent,
    background = MidnightBlue,
    surface = DarkBlueBackground,
    onPrimary = WhiteUI,
    onSecondary = WhiteUI,
    onBackground = WhiteUI,
    onSurface = WhiteUI
)

@Composable
fun RemindUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}