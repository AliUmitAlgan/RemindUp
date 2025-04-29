package com.aliumitalgan.remindup.components

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aliumitalgan.remindup.ui.theme.*
import com.aliumitalgan.remindup.utils.SubGoal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestedGoalCard(
    goalTitle: String,
    goalProgress: Int,
    onProgressUpdate: ((Int) -> Unit)? = null,
    subGoals: List<SubGoal> = emptyList(),
    onAddSubGoal: ((String) -> Unit)? = null,
    onToggleSubGoal: ((SubGoal, Boolean) -> Unit)? = null
) {
    val animatedProgress = animateFloatAsState(
        targetValue = goalProgress / 100f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val isCompleted = goalProgress >= 100
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showSubGoalsDialog by remember { mutableStateOf(false) }
    var expandSubGoals by remember { mutableStateOf(false) }

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
                .animateContentSize()
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
                            imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Filled.Flag,
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
                                text = "Devam ediyor",
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
                                    text = "Tamamlandı!",
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

            // Alt hedefler kısmı
            if (subGoals.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                // Alt hedefleri genişletme/daraltma butonu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandSubGoals = !expandSubGoals }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alt Hedefler (${subGoals.count { it.completed }}/${subGoals.size})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        imageVector = if (expandSubGoals) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (expandSubGoals) "Daralt" else "Genişlet",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Alt hedefleri göster/gizle
                AnimatedVisibility(
                    visible = expandSubGoals,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                    ) {
                        subGoals.forEach { subGoal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = subGoal.completed,
                                    onCheckedChange = { isChecked ->
                                        onToggleSubGoal?.invoke(subGoal, isChecked)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = progressColor,
                                        uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = subGoal.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color =                 if (subGoal.completed)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (subGoal.completed)
                                        TextDecoration.LineThrough
                                    else
                                        TextDecoration.None
                                )
                            }
                        }
                    }
                }
            }

            // Butonlar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                // Alt Hedefler butonu
                OutlinedButton(
                    onClick = { showSubGoalsDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    ),                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = "Alt Hedefler",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Alt Hedefler", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // İlerle butonu
                Button(
                    onClick = { showUpdateDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = progressColor
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.Edit else Icons.Filled.Add,
                        contentDescription = if (isCompleted) "Düzenle" else "İlerle",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isCompleted) "Düzenle" else "İlerle",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Alt Hedefler Diyaloğu
    if (showSubGoalsDialog) {
        SubGoalsDialog(
            onDismiss = { showSubGoalsDialog = false },
            subGoals = subGoals,
            onAddSubGoal = onAddSubGoal,
            onToggleSubGoal = onToggleSubGoal
        )
    }

    // İlerleme güncelleme diyaloğu
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
                        text = "İlerleme Güncelle",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Mevcut ilerleme: $goalProgress%",
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
                        label = { Text("Yeni İlerleme (%)") },
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
                        text = "Kaydırarak ayarla:",
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
                                "Hedefinizi tamamlıyorsunuz!",
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
                        contentDescription = "Güncelle",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Güncelle")
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
                        contentDescription = "İptal",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("İptal")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        )
    }
}

@Composable
fun SubGoalsDialog(
    onDismiss: () -> Unit,
    subGoals: List<SubGoal>,
    onAddSubGoal: ((String) -> Unit)? = null,
    onToggleSubGoal: ((SubGoal, Boolean) -> Unit)? = null
) {
    var newSubGoalTitle by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Başlık
                Text(
                    text = "Alt Hedefler",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Divider(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Alt hedefler listesi
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (subGoals.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Henüz alt hedef eklenmemiş",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        items(subGoals) { subGoal ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (subGoal.completed)
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        else
                                            Color.Transparent
                                    )
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = subGoal.completed,
                                    onCheckedChange = { isChecked ->
                                        onToggleSubGoal?.invoke(subGoal, isChecked)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MaterialTheme.colorScheme.primary,
                                        uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = subGoal.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (subGoal.completed)
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    else
                                        MaterialTheme.colorScheme.onSurface,
                                    textDecoration = if (subGoal.completed)
                                        androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    else
                                        androidx.compose.ui.text.style.TextDecoration.None
                                )
                            }
                        }
                    }
                }

                // Yeni alt hedef ekleme
                if (onAddSubGoal != null) {
                    Divider(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Text(
                        text = "Yeni Alt Hedef Ekle",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSubGoalTitle,
                            onValueChange = { newSubGoalTitle = it },
                            placeholder = { Text("Örn: OOP kavramlarını öğren") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                if (newSubGoalTitle.isNotEmpty()) {
                                    onAddSubGoal(newSubGoalTitle)
                                    newSubGoalTitle = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Ekle",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Kapat butonu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Tamam",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}