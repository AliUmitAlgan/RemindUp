package com.aliumitalgan.remindup.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import java.util.Locale

/**
 * Dil yönetimi için kapsamlı ve güvenilir bir sınıf.
 * Bu sınıf, uygulama genelinde dil değişimini yönetir ve
 * dil durumunu güncel tutar.
 */
object LanguageManager {
    private const val TAG = "LanguageManager"

    // Dil kodları
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_TURKISH = "tr"

    // Dil tercihi saklamak için kullanılan SharedPreferences
    private const val LANGUAGE_PREFS = "language_prefs"
    private const val LANGUAGE_CODE_KEY = "language_code"

    // Mevcut dil durumu
    private val _currentLanguage = mutableStateOf(LANGUAGE_TURKISH) // Varsayılan Türkçe
    val currentLanguage: State<String> = _currentLanguage

    /**
     * Uygulama dilini ayarlar ve gerekli değişiklikleri yapar.
     * Bu metot şunları yapar:
     * 1. Dil değişimini StateFlow'a kaydeder
     * 2. SharedPreferences'a kaydeder
     * 3. Uygulama kaynaklarını günceller
     */
    fun setLanguage(context: Context, languageCode: String) {
        Log.d(TAG, "Setting language to: $languageCode")

        // Zaten aynı dildeyse işlem yapmaya gerek yok
        if (_currentLanguage.value == languageCode) {
            Log.d(TAG, "Language is already set to $languageCode, skipping")
            return
        }

        // Dil durumunu güncelle
        _currentLanguage.value = languageCode
        Log.d(TAG, "Language state updated to: $languageCode")

        // SharedPreferences'a kaydet
        saveLanguagePreference(context, languageCode)

        // Kaynakları güncelle
        updateResources(context, languageCode)

        Log.d(TAG, "Language set process completed for: $languageCode")
    }

    /**
     * Dil tercihini SharedPreferences'a kaydeder
     */
    private fun saveLanguagePreference(context: Context, languageCode: String) {
        val sharedPrefs = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
        sharedPrefs.edit().putString(LANGUAGE_CODE_KEY, languageCode).apply()
        Log.d(TAG, "Language preference saved to SharedPreferences: $languageCode")
    }

    /**
     * Kaydedilmiş dil tercihini yükler
     */
    fun loadSavedLanguage(context: Context) {
        val sharedPrefs = context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
        val savedLanguage = sharedPrefs.getString(LANGUAGE_CODE_KEY, getDeviceLanguage()) ?: LANGUAGE_TURKISH

        // Dil durumunu güncelle
        _currentLanguage.value = savedLanguage
        Log.d(TAG, "Loaded saved language from preferences: $savedLanguage")

        // Kaynakları güncelle
        updateResources(context, savedLanguage)
    }

    /**
     * Kaynakları verilen dil koduna göre günceller
     */
    private fun updateResources(context: Context, languageCode: String): Context {
        Log.d(TAG, "Updating resources for language: $languageCode")

        // Locale oluştur
        val locale = when (languageCode) {
            LANGUAGE_ENGLISH -> Locale.ENGLISH
            else -> Locale("tr") // Varsayılan olarak Türkçe
        }

        // Varsayılan Locale'i ayarla
        Locale.setDefault(locale)
        Log.d(TAG, "Default Locale set to: ${locale.language}")

        // Configuration güncelleme
        val configuration = Configuration(context.resources.configuration)

        // API seviyesine göre doğru metodu çağır
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
        }

        Log.d(TAG, "Configuration updated for locale: ${locale.language}")

        // Yeni yapılandırmayla context oluştur
        val updatedContext = context.createConfigurationContext(configuration)

        // Kaynakları güncelle
        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

        Log.d(TAG, "Resources updated with new configuration")

        return updatedContext
    }

    /**
     * Cihazın varsayılan dilini alır
     */
    private fun getDeviceLanguage(): String {
        val deviceLocale = Locale.getDefault().language
        return when (deviceLocale) {
            "tr" -> LANGUAGE_TURKISH
            "en" -> LANGUAGE_ENGLISH
            else -> LANGUAGE_TURKISH // Varsayılan Türkçe
        }
    }

    /**
     * Dilin İngilizce olup olmadığını kontrol eder
     */
    fun isEnglish(): Boolean {
        val isEnglish = _currentLanguage.value == LANGUAGE_ENGLISH
        Log.d(TAG, "Checking if language is English: $isEnglish")
        return isEnglish
    }

    /**
     * Uygulama dilini Türkçe ve İngilizce arasında değiştirir
     */
    fun toggleLanguage(context: Context) {
        val currentLang = _currentLanguage.value
        val newLanguage = if (currentLang == LANGUAGE_ENGLISH) LANGUAGE_TURKISH else LANGUAGE_ENGLISH

        Log.d(TAG, "Toggling language from $currentLang to $newLanguage")

        setLanguage(context, newLanguage)
    }

    /**
     * Dil adını gösterim için döndürür
     */
    fun getLanguageName(): String {
        return when (_currentLanguage.value) {
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_TURKISH -> "Türkçe"
            else -> "Türkçe"
        }
    }
}