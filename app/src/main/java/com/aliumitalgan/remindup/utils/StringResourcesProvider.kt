package com.aliumitalgan.remindup.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.aliumitalgan.remindup.LocalLanguage

/**
 * String kaynaklarına her yerden tutarlı bir şekilde erişmek için yardımcı sınıf.
 * Bu sınıfı kullanarak uygulama genelinde dil değişikliklerini kolayca yönetebilirsiniz.
 */
object StringResourcesProvider {

    /**
     * Bir string kaynağını doğrudan contextden çeker.
     * Bu metot Compose dışındaki sınıflarda kullanılabilir.
     */
    fun getString(context: Context, resId: Int): String {
        return context.getString(resId)
    }

    /**
     * Bir string kaynağını doğrudan contextden çeker ve parametreleri doldurur.
     * Bu metot Compose dışındaki sınıflarda kullanılabilir.
     */
    fun getString(context: Context, resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }

    /**
     * Compose içerisinde kullanılacak olan ve dil değişikliklerini otomatik algılayan
     * string kaynağı alma fonksiyonu. Bu fonksiyon, LocalLanguage değişikliklerini takip eder
     * ve string değerini güncel dile göre sağlar.
     */
    @Composable
    @ReadOnlyComposable
    fun string(resId: Int): String {
        // Güncel dili al - bu değiştiğinde composable da yeniden oluşturulacak
        val currentLanguage = LocalLanguage.current.value
        val context = LocalContext.current

        // String kaynağını a
        return stringResource(id = resId)
    }

    /**
     * Compose içerisinde kullanılacak olan ve dil değişikliklerini otomatik algılayan
     * formatlı string kaynağı alma fonksiyonu. Bu fonksiyon, LocalLanguage değişikliklerini takip eder
     * ve string değerini güncel dile göre sağlar.
     */
    @Composable
    @ReadOnlyComposable
    fun string(resId: Int, vararg formatArgs: Any): String {
        // Güncel dili al - bu değiştiğinde composable da yeniden oluşturulacak
        val currentLanguage = LocalLanguage.current.value

        // String kaynağını formatlarla al
        return stringResource(id = resId, formatArgs = formatArgs)
    }
}