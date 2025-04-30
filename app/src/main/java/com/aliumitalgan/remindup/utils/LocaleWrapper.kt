package com.aliumitalgan.remindup.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Uygulama genelinde dil değişikliklerini yönetmek için yardımcı sınıf.
 * Bu sınıf, bir Composable içinde yerel dil ayarlarını sağlar
 * ve dil değişikliklerinde tüm UI'ın yeniden oluşturulmasına yardımcı olur.
 */
object LocaleWrapper {
    private val TAG = "LocaleWrapper"

    // Güncel dil kodu için Composition Local
    val LocalAppLanguage = compositionLocalOf { Locale.getDefault().language }

    /**
     * Verilen dil koduyla yapılandırılmış bir Locale nesnesi döndürür.
     */
    fun createLocale(languageCode: String): Locale {
        return when (languageCode) {
            "en" -> Locale.ENGLISH
            "tr" -> Locale("tr")
            else -> Locale("tr") // Default to Turkish
        }
    }

    /**
     * Güncel dile göre yapılandırılmış bir Context döndürür.
     * Bu metot, mevcut context'i alır ve verilen dil koduna göre yapılandırır.
     */
    fun wrapContext(baseContext: Context, languageCode: String): Context {
        Log.d(TAG, "Wrapping context with language: $languageCode")

        val locale = createLocale(languageCode)
        Locale.setDefault(locale)

        val config = baseContext.resources.configuration.apply {
            setLocale(locale)
        }

        val newContext = baseContext.createConfigurationContext(config)
        Log.d(TAG, "Context wrapped successfully for language: $languageCode")
        return newContext
    }

    /**
     * İçeriği belirtilen dil ile saran bir Composable.
     * Bu fonksiyon, içerdiği tüm çocuk bileşenlerin doğru dille
     * görüntülenmesini sağlar.
     */
    @Composable
    fun ProvideLocale(
        languageCode: String,
        content: @Composable () -> Unit
    ) {
        // Dil durumunu takip et
        val languageState = remember { mutableStateOf(languageCode) }
        Log.d(TAG, "Providing locale for language: $languageCode")

        // Configuration değişikliğini dinle
        val configuration = LocalConfiguration.current
        val locale = configuration.locales[0]
        Log.d(TAG, "Current configuration locale: ${locale.language}")

        // Composition Local ile verilen içeriği sarma
        CompositionLocalProvider(
            LocalAppLanguage provides languageState.value
        ) {
            // Context'i doğru dille güncelle
            val context = LocalContext.current
            val wrappedContext = remember(languageCode) {
                wrapContext(context, languageCode)
            }

            // İçeriği render et
            content()
        }
    }

    /**
     * Güncel dil kodunu döndürür.
     */
    @Composable
    @ReadOnlyComposable
    fun currentLanguage(): String {
        return LocalAppLanguage.current
    }

    /**
     * Kullanıcının varsayılan sistem dilini algılar
     */
    fun getDeviceLanguage(): String {
        val deviceLocale = Locale.getDefault().language
        return when (deviceLocale) {
            "tr" -> "tr"
            "en" -> "en"
            else -> "tr" // Default to Turkish
        }
    }
}