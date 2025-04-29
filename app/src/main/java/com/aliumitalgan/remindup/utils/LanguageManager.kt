package com.aliumitalgan.remindup.utils

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import android.util.Log
import java.util.Locale
import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper

object LanguageManager {
    // Language code constants
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_TURKISH = "tr"

    // Preferences keys
    private const val LANGUAGE_PREFS = "language_prefs"
    private const val LANGUAGE_CODE_KEY = "language_code"

    // Current language state
    private val _currentLanguage = mutableStateOf(LANGUAGE_TURKISH) // Default to Turkish
    val currentLanguage: State<String> = _currentLanguage

    // Set application language with complete UI refresh
    fun setLanguage(context: Context, languageCode: String) {
        Log.d("LanguageManager", "Setting language to: $languageCode")

        if (_currentLanguage.value == languageCode) {
            Log.d("LanguageManager", "Language is already set to $languageCode, skipping")
            return
        }

        // Update the state
        _currentLanguage.value = languageCode

        // Save to SharedPreferences
        saveLanguagePreference(context, languageCode)

        // Apply the language change to resources
        updateResources(context, languageCode)

        // Restart the app for complete refresh if context is activity
        if (context is Activity) {
            restartApp(context)
        }
    }

    // Properly restart the app with animation
    private fun restartApp(activity: Activity) {
        try {
            Log.d("LanguageManager", "Restarting app for language change")

            // Get the intent that started this activity
            val intent = activity.packageManager.getLaunchIntentForPackage(activity.packageName)
            if (intent != null) {
                // Add flags to start as a new task
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP)

                // Start the app with a delay to allow UI refresh
                Handler(Looper.getMainLooper()).postDelayed({
                    activity.finishAffinity() // Finish all activities in the stack
                    activity.startActivity(intent)

                    // Apply smooth transition animation
                    activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }, 100)
            } else {
                // Fallback to simple recreation if intent is null
                activity.recreate()
            }
        } catch (e: Exception) {
            Log.e("LanguageManager", "Error restarting app: ${e.message}", e)
            // Fallback to simple recreation
            activity.recreate()
        }
    }

    // Save language preference to SharedPreferences
    private fun saveLanguagePreference(context: Context, languageCode: String) {
        val sharedPrefs = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(LANGUAGE_CODE_KEY, languageCode).apply()
        Log.d("LanguageManager", "Saved language preference: $languageCode")
    }

    // Load saved language preference
    fun loadSavedLanguage(context: Context) {
        val sharedPrefs = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
        val savedLanguage = sharedPrefs.getString(LANGUAGE_CODE_KEY, getDeviceLanguage()) ?: LANGUAGE_TURKISH

        // Update state
        _currentLanguage.value = savedLanguage

        // Apply the language
        updateResources(context, savedLanguage)

        Log.d("LanguageManager", "Loaded saved language: $savedLanguage")
    }

    // Update resources with new locale
    private fun updateResources(context: Context, languageCode: String): Context {
        val locale = when (languageCode) {
            LANGUAGE_ENGLISH -> Locale.ENGLISH
            else -> Locale("tr")
        }

        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    // Get device default language
    private fun getDeviceLanguage(): String {
        val deviceLocale = Locale.getDefault().language
        return when (deviceLocale) {
            "tr" -> LANGUAGE_TURKISH
            "en" -> LANGUAGE_ENGLISH
            else -> LANGUAGE_TURKISH // Default fallback
        }
    }

    // Check if language is English
    fun isEnglish(): Boolean {
        return _currentLanguage.value == LANGUAGE_ENGLISH
    }

    // Toggle between Turkish and English
    fun toggleLanguage(context: Context) {
        val newLanguage = if (isEnglish()) LANGUAGE_TURKISH else LANGUAGE_ENGLISH
        setLanguage(context, newLanguage)
    }

    // Get language name for display
    fun getLanguageName(): String {
        return when (_currentLanguage.value) {
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_TURKISH -> "Türkçe"
            else -> "Türkçe"
        }
    }

}