package com.aliumitalgan.remindup.ui.theme

import androidx.compose.ui.graphics.Color

// Vibrant primary blues
val BluePrimary = Color(0xFF2979FF)           // Brighter blue
val BluePrimaryDark = Color(0xFF0D47A1)        // Deeper blue
val BluePrimaryLight = Color(0xFF82B1FF)      // Light blue

// Dynamic secondary greens
val GreenSecondary = Color(0xFF00E676)        // Bright green
val GreenSecondaryDark = Color(0xFF00C853)    // Deep green
val GreenSecondaryLight = Color(0xFF69F0AE)   // Light green

// Accent colors for categories
val AccentPink = Color(0xFFFF4081)            // Vibrant pink
val AccentPurple = Color(0xFFAA00FF)          // Deep purple
val AccentTeal = Color(0xFF1DE9B6)            // Teal
val AccentAmber = Color(0xFFFFAB00)           // Amber
val AccentOrange = Color(0xFFFF6E40)          // Orange

// Status colors
val SuccessGreen = Color(0xFF00C853)          // Success green
val WarningOrange = Color(0xFFFF9100)         // Warning orange
val ErrorRed = Color(0xFFFF1744)              // Error red

// Background & Surface variants
val BackgroundLight = Color(0xFFF8FDFF)       // Very light blue-white
val BackgroundDark = Color(0xFF0A192F)        // Deep blue-black
val SurfaceLight = Color(0xFFFFFFFF)          // Pure white
val SurfaceDark = Color(0xFF172A45)           // Dark blue-gray

// Text colors
val OnSurfaceLight = Color(0xFF212121)        // Near black
val OnSurfaceDark = Color(0xFFF5F5F5)         // Near white

// Gradients - store as color pairs for easy reference
val GradientBlueGreen = listOf(BluePrimary, GreenSecondary)
val GradientPurplePink = listOf(AccentPurple, AccentPink)
val GradientOrangeYellow = listOf(AccentOrange, WarningOrange)

// Legacy color references (keeping for compatibility)
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)
val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)