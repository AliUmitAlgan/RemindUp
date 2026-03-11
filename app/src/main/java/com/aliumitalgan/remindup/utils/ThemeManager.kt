package com.aliumitalgan.remindup.utils

import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_IS_DARK_THEME = "is_dark_theme"
    private const val KEY_DYNAMIC_ACCENTS = "dynamic_accents_enabled"

    private val _isDarkTheme = mutableStateOf(false)
    val isDarkTheme: State<Boolean> = _isDarkTheme

    private val _dynamicAccentsEnabled = mutableStateOf(false)
    val dynamicAccentsEnabled: State<Boolean> = _dynamicAccentsEnabled

    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        Log.d("ThemeManager", "Tema degistirildi: ${if (isDark) "Karanlik" else "Aydinlik"}")
    }

    fun saveDarkThemeState(context: Context, isDark: Boolean) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(KEY_IS_DARK_THEME, isDark).apply()
        setDarkTheme(isDark)
    }

    fun loadDarkThemeState(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDark = sharedPrefs.getBoolean(KEY_IS_DARK_THEME, false)
        _isDarkTheme.value = isDark
    }

    fun setDynamicAccentsEnabled(enabled: Boolean) {
        _dynamicAccentsEnabled.value = enabled
    }

    fun saveDynamicAccentsState(context: Context, enabled: Boolean) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean(KEY_DYNAMIC_ACCENTS, enabled).apply()
        setDynamicAccentsEnabled(enabled)
    }

    fun loadDynamicAccentsState(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val enabled = sharedPrefs.getBoolean(KEY_DYNAMIC_ACCENTS, false)
        _dynamicAccentsEnabled.value = enabled
    }

    fun loadThemeState(context: Context) {
        loadDarkThemeState(context)
        loadDynamicAccentsState(context)
    }

    fun getDarkThemeState(): Boolean {
        return _isDarkTheme.value
    }
}
