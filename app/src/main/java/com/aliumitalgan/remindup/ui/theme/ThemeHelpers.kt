package com.aliumitalgan.remindup.ui.theme

import androidx.compose.ui.graphics.Color
import com.aliumitalgan.remindup.utils.ThemeManager

val isAppDarkTheme: Boolean
    get() = ThemeManager.isDarkTheme.value

fun themedColor(light: Color, dark: Color): Color {
    return if (isAppDarkTheme) dark else light
}

val appCardColor: Color
    get() = themedColor(Color.White, Color(0xFF171D26))

val appCardSubtleColor: Color
    get() = themedColor(Color(0xFFF8FAFD), Color(0xFF1D2632))

val appTextPrimary: Color
    get() = themedColor(Color(0xFF1A1A1A), Color(0xFFE5E7EB))

val appTextSecondary: Color
    get() = themedColor(Color(0xFF6B7280), Color(0xFFAEB6C5))
