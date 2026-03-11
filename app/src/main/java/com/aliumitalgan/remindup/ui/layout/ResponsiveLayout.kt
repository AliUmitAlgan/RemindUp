package com.aliumitalgan.remindup.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class WindowWidthBucket {
    Compact,
    Medium,
    Expanded
}

data class AppResponsiveSpec(
    val bucket: WindowWidthBucket,
    val horizontalPadding: Dp,
    val maxContentWidth: Dp
)

@Composable
fun rememberAppResponsiveSpec(): AppResponsiveSpec {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return remember(widthDp) {
        when {
            widthDp >= 840 -> AppResponsiveSpec(
                bucket = WindowWidthBucket.Expanded,
                horizontalPadding = 28.dp,
                maxContentWidth = 920.dp
            )
            widthDp >= 600 -> AppResponsiveSpec(
                bucket = WindowWidthBucket.Medium,
                horizontalPadding = 20.dp,
                maxContentWidth = 760.dp
            )
            else -> AppResponsiveSpec(
                bucket = WindowWidthBucket.Compact,
                horizontalPadding = 12.dp,
                maxContentWidth = 600.dp
            )
        }
    }
}

