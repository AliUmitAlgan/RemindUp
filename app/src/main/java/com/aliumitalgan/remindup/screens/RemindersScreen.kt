package com.aliumitalgan.remindup.screens

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.aliumitalgan.remindup.components.ReminderCard
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.utils.AnimationUtils
import com.aliumitalgan.remindup.utils.ReminderUtils
import kotlinx.coroutines.launch
import java.util.*

// Import Material (not Material3) swipe components
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.rememberDismissState

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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Hatırlatıcı Ekle"
                )
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
                Text(
                    text = "Henüz hatırlatıcı bulunmuyor.\nEklemek için + butonuna tıklayın.",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(reminders) { (id, reminder) ->
                        AnimationUtils.SlideAnimation(visible = true) {
                            ReminderItem(
                                reminder = reminder,
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
fun AddReminderDialog(
    onDismiss: () -> Unit,
    onAddReminder: (title: String, time: String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Hatırlatıcı") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Başlık") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showTimePicker(context) { selectedTime -> time = selectedTime } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (time.isEmpty()) "Saat Seç" else "Saat: $time")
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
                }
            ) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReminderItem(
    reminder: Reminder,
    onDelete: () -> Unit
) {
    // Using Material (not Material3) SwipeToDismiss
    val dismissState = rememberDismissState { dismissValue ->
        if (dismissValue == DismissValue.DismissedToEnd ||
            dismissValue == DismissValue.DismissedToStart) {
            onDelete()
            true
        } else {
            false
        }
    }

    SwipeToDismiss(
        state = dismissState,
        background = {
            val color = MaterialTheme.colorScheme.error

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = color
                )
            }
        },
        dismissContent = {
            ReminderCard(
                reminderTitle = reminder.title,
                reminderTime = reminder.time
            )
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