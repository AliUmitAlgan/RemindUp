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
 * Dil seçimi için dialog bileşeni - Geliştirilmiş ve dil değişikliğini daha doğru ele alan
 */
@Composable
fun LanguageSelectionDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    Log.d("LanguageSelectionDialog", "Dialog composable called")

    // Composable'ın yeniden oluşturulması için dili dinle
    val currentLanguageState = LocalLanguage.current
    val currentLanguage = currentLanguageState.value
    val context = LocalContext.current

    Dialog(
        onDismissRequest = {
            Log.d("LanguageSelectionDialog", "Dialog dismissed via outside click")
            onDismiss()
        }
    ) {
        Log.d("LanguageSelectionDialog", "Rendering dialog content")

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
                // Header
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
                        text = stringResource(R.string.language_select),  // Sabit string - her iki dilde de görünecek
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider(modifier = Modifier.padding(bottom = 16.dp))

                Log.d("LanguageSelectionDialog", "Current language: $currentLanguage")

                // Turkish Option
                LanguageOption(
                    languageName = "Türkçe",
                    languageCode = LanguageManager.LANGUAGE_TURKISH,
                    isSelected = currentLanguage == LanguageManager.LANGUAGE_TURKISH,
                    onClick = {
                        Log.d("LanguageSelectionDialog", "Turkish selected")
                        onLanguageSelected(LanguageManager.LANGUAGE_TURKISH)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // English Option
                LanguageOption(
                    languageName = "English",
                    languageCode = LanguageManager.LANGUAGE_ENGLISH,
                    isSelected = currentLanguage == LanguageManager.LANGUAGE_ENGLISH,
                    onClick = {
                        Log.d("LanguageSelectionDialog", "English selected")
                        onLanguageSelected(LanguageManager.LANGUAGE_ENGLISH)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Cancel button - İptal/Cancel olarak dile göre değişir
                Button(
                    onClick = {
                        Log.d("LanguageSelectionDialog", "Cancel button clicked")
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel)
                    )
                }
            }
        }
    }
}

/**
 * Bir dil seçeneğini temsil eden bileşen.
 * Seçildiğinde farklı arka plan rengi ve işaret gösterir.
 */
@Composable
private fun LanguageOption(
    languageName: String,
    languageCode: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Log.d("LanguageOption", "Rendering option for $languageName, isSelected=$isSelected")

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
                Log.d("LanguageOption", "Option clicked: $languageName")
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