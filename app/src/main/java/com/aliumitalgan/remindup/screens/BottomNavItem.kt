package com.aliumitalgan.remindup.screens

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Alt navigasyon çubuğu için öğe sınıfı.
 * Seçili ve seçili olmayan durumlar için ikonlar ve rota bilgisini içerir.
 */
data class BottomNavItem(
    val title: String,           // Navigasyon öğesinin başlığı
    val selectedIcon: ImageVector,   // Seçili durumdaki ikonu
    val unselectedIcon: ImageVector, // Seçili olmayan durumdaki ikonu
    val route: String            // Navigasyon rotası
)