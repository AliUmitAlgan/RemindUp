package com.aliumitalgan.remindup.screens

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.components.ModernCard
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.ui.theme.BluePrimary
import com.aliumitalgan.remindup.ui.theme.GreenSecondary
import com.aliumitalgan.remindup.utils.AnimationUtils
import com.aliumitalgan.remindup.utils.ReminderUtils
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreenContent(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var reminders by remember { mutableStateOf<List<Pair<String, Reminder>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedCardId by remember { mutableStateOf<String?>(null) }
    var showQuickAddOptions by remember { mutableStateOf(false) }

    // Hatırlatıcıları yükle
    LaunchedEffect(key1 = true) {
        loadReminders(
            onSuccess = { remindersList ->
                reminders = remindersList
                isLoading = false
            },
            onError = { error ->
                showToast(context, "Hatırlatıcılar yüklenemedi: $error")
                isLoading = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hatırlatıcılar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    // Arama butonu
                    IconButton(onClick = { /* Arama özelliği gelecekte eklenecek */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Ara")
                    }

                    // Menü butonu
                    IconButton(onClick = { /* Ek özellikler menüsü */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menü")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // Hızlı ekleme seçenekleri
                AnimatedVisibility(
                    visible = showQuickAddOptions,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Su içme hatırlatıcısı
                        SmallFAB(
                            icon = Icons.Default.WaterDrop,
                            label = "Su İçme",
                            onClick = {
                                coroutineScope.launch {
                                    val time = getCurrentTimeString()
                                    addReminder(
                                        context = context,
                                        reminder = Reminder(title = "Su İçme Vakti", time = time),
                                        onSuccess = { id, newReminder ->
                                            reminders = reminders + (id to newReminder)
                                            showToast(context, "Su içme hatırlatıcısı eklendi")
                                        },
                                        onError = { error ->
                                            showToast(context, "Hatırlatıcı eklenemedi: $error")
                                        }
                                    )
                                }
                                showQuickAddOptions = false
                            }
                        )

                        // İlaç hatırlatıcısı
                        SmallFAB(
                            icon = Icons.Default.Medication,
                            label = "İlaç",
                            onClick = {
                                coroutineScope.launch {
                                    val time = getCurrentTimeString()
                                    addReminder(
                                        context = context,
                                        reminder = Reminder(title = "İlaç Vakti", time = time),
                                        onSuccess = { id, newReminder ->
                                            reminders = reminders + (id to newReminder)
                                            showToast(context, "İlaç hatırlatıcısı eklendi")
                                        },
                                        onError = { error ->
                                            showToast(context, "Hatırlatıcı eklenemedi: $error")
                                        }
                                    )
                                }
                                showQuickAddOptions = false
                            }
                        )
                    }
                }

                // Ana FAB
                FloatingActionButton(
                    onClick = {
                        if (showQuickAddOptions) {
                            showQuickAddOptions = false
                            showAddDialog = true
                        } else {
                            showQuickAddOptions = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    if (showQuickAddOptions) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Yeni Hatırlatıcı"
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Yeni Hatırlatıcı"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (reminders.isEmpty()) {
                EmptyRemindersView(
                    onAddClick = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reminders) { (id, reminder) ->
                        AnimationUtils.SlideAnimation(visible = true) {
                            ModernReminderCard(
                                reminder = reminder,
                                isExpanded = expandedCardId == id,
                                onCardClick = {
                                    expandedCardId = if (expandedCardId == id) null else id
                                },
                                onDelete = {
                                    coroutineScope.launch {
                                        deleteReminder(
                                            context = context,
                                            reminderId = id,
                                            onSuccess = {
                                                reminders = reminders.filter { it.first != id }
                                                showToast(context, "Hatırlatıcı silindi")
                                            },
                                            onError = { error ->
                                                showToast(context, "Hatırlatıcı silinemedi: $error")
                                            }
                                        )
                                    }
                                },
                                onEdit = {
                                    // TODO: Düzenleme özelliği eklenecek
                                }
                            )
                        }
                    }
                }
            }

            if (showAddDialog) {
                AddReminderDialog(
                    onDismiss = { showAddDialog = false },
                    onAddReminder = { title, time ->
                        coroutineScope.launch {
                            addReminder(
                                context = context,
                                reminder = Reminder(title = title, time = time),
                                onSuccess = { id, newReminder ->
                                    reminders = reminders + (id to newReminder)
                                    showToast(context, "Hatırlatıcı eklendi")
                                },
                                onError = { error ->
                                    showToast(context, "Hatırlatıcı eklenemedi: $error")
                                }
                            )
                        }
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun SmallFAB(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Icon(icon, contentDescription = label)
        }
    }
}

@Composable
fun ModernReminderCard(
    reminder: Reminder,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val iconBgColor = if(isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    }

    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Saat ikonu
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Saat",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = reminder.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Saat: ${reminder.time}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                // Detay butonu
                IconButton(onClick = onCardClick) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Daha az göster" else "Daha fazla göster",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider()

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Düzenle butonu
                        FilledTonalButton(
                            onClick = onEdit,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Düzenle",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Düzenle")
                        }

                        // Sil butonu
                        FilledTonalButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Sil",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Sil")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyRemindersView(
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Hatırlatıcılar",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Henüz hatırlatıcı bulunmuyor",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Yeni bir hatırlatıcı ekleyerek başlayın",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Ekle"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hatırlatıcı Ekle")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onAddReminder: (title: String, time: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var isRepeat by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Yeni Hatırlatıcı",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        icon = { Icon(Icons.Default.Notifications, contentDescription = null) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık") },
                    leadingIcon = {
                        Icon(Icons.Default.Title, contentDescription = "Başlık")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showTimePicker(context) { selectedTime -> time = selectedTime } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = "Saat")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (time.isEmpty()) "Saat Seç" else "Saat: $time")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = isRepeat,
                        onCheckedChange = { isRepeat = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Her gün tekrarla")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotEmpty() && time.isNotEmpty()) {
                        onAddReminder(title, time)
                    } else {
                        showToast(context, "Lütfen tüm alanları doldurun")
                    }
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Ekle")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("İptal")
            }
        }
    )
}

// Saat seçme diyaloğu göster
private fun showTimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            onTimeSelected(formattedTime)
        },
        hour,
        minute,
        true // 24 saatlik format
    ).show()
}

// Şu anki zamanı döndür
private fun getCurrentTimeString(): String {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    return String.format("%02d:%02d", hour, minute)
}

// Hatırlatıcıları yükle
private suspend fun loadReminders(
    onSuccess: (List<Pair<String, Reminder>>) -> Unit,
    onError: (String) -> Unit
) {
    val result = ReminderUtils.getUserReminders()
    if (result.isSuccess) {
        onSuccess(result.getOrDefault(emptyList()))
    } else {
        onError(result.exceptionOrNull()?.message ?: "Bilinmeyen hata")
    }
}

// Hatırlatıcı ekle
private suspend fun addReminder(
    context: Context,
    reminder: Reminder,
    onSuccess: (String, Reminder) -> Unit,
    onError: (String) -> Unit
) {
    val result = ReminderUtils.addReminder(reminder, context)
    if (result.isSuccess) {
        onSuccess(result.getOrDefault(""), reminder)
    } else {
        onError(result.exceptionOrNull()?.message ?: "Bilinmeyen hata")
    }
}

// Hatırlatıcı sil
private suspend fun deleteReminder(
    context: Context,
    reminderId: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val result = ReminderUtils.deleteReminder(reminderId, context)
    if (result.isSuccess) {
        onSuccess()
    } else {
        onError(result.exceptionOrNull()?.message ?: "Bilinmeyen hata")
    }
}

// Toast mesajı göster
private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}