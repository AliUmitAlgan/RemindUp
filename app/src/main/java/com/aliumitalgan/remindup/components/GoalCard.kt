package com.aliumitalgan.remindup.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.BluePrimaryDark
import com.aliumitalgan.remindup.ui.theme.BluePrimaryLight
import com.aliumitalgan.remindup.ui.theme.GreenSecondaryDark
import com.aliumitalgan.remindup.ui.theme.GreenSecondaryLight
import com.aliumitalgan.remindup.ui.theme.RemindUpTheme
import com.aliumitalgan.remindup.ui.theme.SuccessGreen
import com.aliumitalgan.remindup.ui.theme.WarningOrange

@Composable
fun GoalCard(goalTitle: String, goalProgress: Int, onProgressUpdate: ((Int) -> Unit)? = null) {
    val animatedProgress = animateFloatAsState(
        targetValue = goalProgress / 100f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val progressColor = when {
        goalProgress >= 100 -> SuccessGreen
        goalProgress >= 70 -> GreenSecondaryLight
        goalProgress >= 30 -> BluePrimaryLight
        else -> WarningOrange
    }

    val isCompleted = goalProgress >= 100

    var showUpdateDialog by remember { mutableStateOf(false) }

    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { showUpdateDialog = true }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sol taraf: Başlık ve ikon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // İkon kutusu (Neomorphic tasarım etkisi)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = if (isSystemInDarkTheme()) Color.Black else Color.Gray,
                                ambientColor = if (isSystemInDarkTheme()) Color.Black else Color.LightGray
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        if (isSystemInDarkTheme()) BluePrimaryDark else BluePrimaryLight,
                                        if (isSystemInDarkTheme()) GreenSecondaryDark else GreenSecondaryLight
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Flag,
                            contentDescription = "Hedef",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = goalTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        AnimatedVisibility(visible = !isCompleted) {
                            Text(
                                text = "Devam ediyor",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        AnimatedVisibility(visible = isCompleted) {
                            Text(
                                text = "Tamamlandı!",
                                style = MaterialTheme.typography.bodySmall,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // İlerleme yüzdesi (Dairesel gösterge)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress.value },
                        modifier = Modifier.size(56.dp),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 4.dp
                    )

                    Text(
                        text = "$goalProgress%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // İlerleme çubuğu (Gradient ile zenginleştirilmiş)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.value)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    if (isCompleted) SuccessGreen.copy(alpha = 0.8f) else BluePrimaryLight,
                                    if (isCompleted) SuccessGreen else GreenSecondaryLight
                                )
                            )
                        )
                )
            }

            // İlerleme güncelleme butonu
            if (onProgressUpdate != null) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showUpdateDialog = true },
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, progressColor),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = progressColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "İlerleme güncelle",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("İlerleme Güncelle", fontSize = 12.sp)
                }
            }
        }
    }

    // İlerleme güncelleme diyaloğu
    if (showUpdateDialog && onProgressUpdate != null) {
        var updatedProgress by remember { mutableStateOf(goalProgress.toString()) }

        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = { Text("İlerleme Güncelle") },
            icon = { Icon(Icons.Default.Edit, contentDescription = null) },
            text = {
                Column {
                    Text(
                        text = "Mevcut ilerleme: $goalProgress%",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = updatedProgress,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.all { it.isDigit() }) {
                                val num = input.toIntOrNull() ?: 0
                                if (num in 0..100) {
                                    updatedProgress = input
                                }
                            }
                        },
                        label = { Text("Yeni İlerleme (%)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // İlerleme slider'ı
                    Slider(
                        value = updatedProgress.toFloatOrNull() ?: goalProgress.toFloat(),
                        onValueChange = { updatedProgress = it.toInt().toString() },
                        valueRange = 0f..100f,
                        steps = 100,
                        colors = SliderDefaults.colors(
                            thumbColor = progressColor,
                            activeTrackColor = progressColor,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newProgress = updatedProgress.toIntOrNull() ?: goalProgress
                        onProgressUpdate(newProgress)
                        showUpdateDialog = false
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Güncelle")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showUpdateDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("İptal")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GoalCardPreview() {
    RemindUpTheme {
        GoalCard(goalTitle = "Günde 2 litre su iç", goalProgress = 75)
    }
}