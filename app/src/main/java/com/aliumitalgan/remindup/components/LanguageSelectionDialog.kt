package com.aliumitalgan.remindup.components

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.aliumitalgan.remindup.LocalLanguage
import com.aliumitalgan.remindup.utils.LanguageManager
import com.aliumitalgan.remindup.R

/**
 * Dil seçimi için tamamen güncellenmiş dialog bileşeni.
 * Statik metin yerine R.string kullanır ve güvenilir dil değişim desteği sağlar.
 */
@Composable
fun LanguageSelectionDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val TAG = "LanguageSelectionDialog"
    Log.d(TAG, "Dialog composable started")

    // Context'i al
    val context = LocalContext.current

    // Mevcut dili takip et
    val currentLanguage by LanguageManager.currentLanguage
    Log.d(TAG, "Current language: $currentLanguage")

    // Dialog içeriği
    Dialog(
        onDismissRequest = {
            Log.d(TAG, "Dialog dismissed via outside click")
            onDismiss()
        }
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Başlık kısmı
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        // Başlık için string resource kullanılıyor
                        text = stringResource(R.string.language_select),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(modifier = Modifier.padding(bottom = 16.dp))

                // Türkçe Seçeneği
                LanguageOption(
                    languageName = "Türkçe",
                    languageCode = LanguageManager.LANGUAGE_TURKISH,
                    isSelected = currentLanguage == LanguageManager.LANGUAGE_TURKISH,
                    onClick = {
                        Log.d(TAG, "Turkish language selected")
                        onLanguageSelected(LanguageManager.LANGUAGE_TURKISH)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // İngilizce Seçeneği
                LanguageOption(
                    languageName = "English",
                    languageCode = LanguageManager.LANGUAGE_ENGLISH,
                    isSelected = currentLanguage == LanguageManager.LANGUAGE_ENGLISH,
                    onClick = {
                        Log.d(TAG, "English language selected")
                        onLanguageSelected(LanguageManager.LANGUAGE_ENGLISH)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // İptal butonu
                Button(
                    onClick = {
                        Log.d(TAG, "Cancel button clicked")
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    // İptal metni için string resource kullanılıyor
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

/**
 * Her bir dil seçeneğini temsil eden bileşen.
 * Seçildiğinde visual feedback ve işaret gösterir.
 */
@Composable
private fun LanguageOption(
    languageName: String,
    languageCode: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val TAG = "LanguageOption"

    // Dil seçeneğinin renklerini belirle
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val textColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                Log.d(TAG, "Option clicked: $languageName")
                onClick()
            },
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = languageName,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}