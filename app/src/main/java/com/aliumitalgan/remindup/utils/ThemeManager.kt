package com.aliumitalgan.remindup.utils

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import android.util.Log

object ThemeManager {
    // Tema durumunu tutan MutableState
    private val _isDarkTheme = mutableStateOf(false)
    val isDarkTheme: State<Boolean> = _isDarkTheme

    // Tema durumunu güncelle
    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        Log.d("ThemeManager", "Tema değiştirildi: ${if (isDark) "Karanlık" else "Aydınlık"}")
    }

    // Tema durumunu SharedPreferences'a kaydet
    fun saveDarkThemeState(context: Context, isDark: Boolean) {
        val sharedPrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("is_dark_theme", isDark).apply()
        setDarkTheme(isDark)
    }

    // Tema durumunu SharedPreferences'dan oku
    fun loadDarkThemeState(context: Context) {
        val sharedPrefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val isDark = sharedPrefs.getBoolean("is_dark_theme", false)
        _isDarkTheme.value = isDark
    }

    // State nesnesini doğrudan döndür (Flow yerine)
    fun getDarkThemeState(): Boolean {
        return _isDarkTheme.value
    }
}