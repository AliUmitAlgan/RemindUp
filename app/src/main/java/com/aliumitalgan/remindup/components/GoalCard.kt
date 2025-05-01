package com.aliumitalgan.remindup.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.*
import com.aliumitalgan.remindup.R

@Composable
fun GoalCard(
    goalTitle: String,
    goalProgress: Int,
    onProgressUpdate: ((Int) -> Unit)? = null
) {
    val animatedProgress = animateFloatAsState(
        targetValue = goalProgress / 100f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val isCompleted = goalProgress >= 100
    var showUpdateDialog by remember { mutableStateOf(false) }

    // Dinamik renk ayarlamaları
    val progressColor = when {
        goalProgress >= 100 -> SuccessGreen
        goalProgress >= 75 -> GreenSecondaryLight
        goalProgress >= 50 -> BluePrimaryLight
        goalProgress >= 25 -> Color(0xFFFFB74D) // Turuncu tonu
        else -> Color(0xFFFF7043) // Kırmızımsı turuncu
    }

    val progressGradient = Brush.horizontalGradient(
        colors = listOf(
            progressColor.copy(alpha = 0.95f),
            progressColor.copy(alpha = 0.75f)
        )
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 2.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = progressColor.copy(alpha = 0.1f),
                ambientColor = progressColor.copy(alpha = 0.05f)
            )
            .clickable { showUpdateDialog = true },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sol taraf: Başlık ve icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon kutusu (Gölgeli ve gradyanlı)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = progressColor.copy(alpha = 0.2f)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        progressColor.copy(alpha = 0.2f),
                                        progressColor.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.Flag,
                            contentDescription = "Hedef",
                            tint = progressColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = goalTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        AnimatedVisibility(
                            visible = !isCompleted,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text(
                                text = stringResource(R.string.in_progress),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontSize = 13.sp
                            )
                        }

                        AnimatedVisibility(
                            visible = isCompleted,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Tamamlandı",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = stringResource(R.string.completed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // İlerleme yüzdesi (Dairesel gösterge)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .shadow(
                            elevation = 3.dp,
                            shape = CircleShape,
                            spotColor = progressColor.copy(alpha = 0.15f)
                        )
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { animatedProgress.value },
                        modifier = Modifier.size(64.dp),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        strokeWidth = 5.dp
                    )

                    Text(
                        text = "$goalProgress%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // İlerleme çubuğu (Gradyanlı)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.value)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(10.dp))
                        .background(progressGradient)
                )
            }

            // İlerleme butonları
            if (onProgressUpdate != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tamamlandı bilgisi
                    if (isCompleted) {
                        Text(
                            text =stringResource(R.string.congratulations),
                            style = MaterialTheme.typography.bodyMedium,
                            color = progressColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Güncelleme butonu
                    Button(
                        onClick = { showUpdateDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = progressColor.copy(alpha = 0.15f),
                            contentColor = progressColor
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 2.dp
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_progress),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isCompleted) stringResource(R.string.edit) else stringResource(R.string.advance),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // İlerleme güncelleme diyaloğu - iyileştirilmiş versiyon
    if (showUpdateDialog && onProgressUpdate != null) {
        var updatedProgress by remember { mutableStateOf(goalProgress.toString()) }

        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = progressColor
                    )
                    Text(
                        stringResource(R.string.edit_progress),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        stringResource(R.string.current_progress, goalProgress),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Divider()

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
                        label = { stringResource(R.string.new_progress) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = progressColor,
                            focusedLabelColor = progressColor,
                            cursorColor = progressColor
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Percent,
                                contentDescription = "Yüzde",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    // İlerleme slider'ı
                    Text(
                        stringResource(R.string.slide_to_adjust),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Slider(
                        value = updatedProgress.toFloatOrNull() ?: goalProgress.toFloat(),
                        onValueChange = { updatedProgress = it.toInt().toString() },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = progressColor,
                            activeTrackColor = progressColor,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0%", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("100%", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    if (updatedProgress.toIntOrNull() == 100) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(SuccessGreen.copy(alpha = 0.1f))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Tamamlandı",
                                tint = SuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.completing_goal),
                                color = SuccessGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newProgress = updatedProgress.toIntOrNull() ?: goalProgress
                        onProgressUpdate(newProgress)
                        showUpdateDialog = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = progressColor
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        stringResource(R.string.update),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text( stringResource(R.string.update))
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showUpdateDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        stringResource(R.string.cancel),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
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