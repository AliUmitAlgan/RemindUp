package com.aliumitalgan.remindup.screens

import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.models.ReminderCategory
import com.aliumitalgan.remindup.models.ReminderType
import com.aliumitalgan.remindup.utils.ReminderUtils
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var reminders by remember { mutableStateOf<List<Pair<String, Reminder>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingReminder by remember { mutableStateOf<Pair<String, Reminder>?>(null) }

    // Bottom Navigation Items
    val bottomNavItems = listOf(
        BottomNavItem("Ana Sayfa", Icons.Filled.Home, Icons.Filled.Home, "home"),
        BottomNavItem("Hedefler", Icons.Filled.CheckCircle, Icons.Filled.CheckCircle, "goals"),
        BottomNavItem("Hatırlatıcılar", Icons.Filled.Notifications, Icons.Filled.Notifications, "reminders"),
        BottomNavItem("İlerleme", Icons.Filled.ShowChart, Icons.Filled.ShowChart, "progress"),
        BottomNavItem("Profil", Icons.Filled.Person, Icons.Filled.Person, "profile")
    )
    var selectedNavItem by remember { mutableStateOf(bottomNavItems[2].route) }

    // Verileri yükle - LaunchedEffect içinde
    LaunchedEffect(key1 = true) {
        try {
            // Daha verimli bir şekilde hatırlatıcıları yükle
            val result = ReminderUtils.getUserReminders()
            if (result.isSuccess) {
                reminders = result.getOrDefault(emptyList())
            } else {
                // Hata olursa boş liste kullan
                reminders = emptyList()
                showToast(context, "Hatırlatıcılar yüklenemedi: ${result.exceptionOrNull()?.message ?: "Bilinmeyen hata"}")
            }
        } catch (e: Exception) {
            reminders = emptyList()
            showToast(context, "Bir hata oluştu: ${e.message}")
        } finally {
            // Yükleme işlemi bittiğinde loading durumunu false yap
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reminders)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    // Ekleme butonu üst kısımda
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_reminder))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(
                items = bottomNavItems,
                currentRoute = selectedNavItem,
                onItemSelected = { route ->
                    selectedNavItem = route
                    when (route) {
                        "home" -> onNavigateToHome()
                        "goals" -> onNavigateToGoals()
                        "reminders" -> {} // Zaten hatırlatıcı ekranındayız
                        "progress" -> onNavigateToProgress()
                        "profile" -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Yükleniyor durumunu göster
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else if (reminders.isEmpty()) {
                // Hatırlatıcı yoksa boş durum göster
                EmptyRemindersView(
                    onAddClick = { showAddDialog = true }
                )
            } else {
                // Hatırlatıcıları listele
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reminders) { (id, reminder) ->
                        ModernReminderItem(
                            reminder = reminder,
                            onToggleEnabled = { isEnabled ->
                                // Hatırlatıcının aktiflik durumunu güncelle
                                coroutineScope.launch {
                                    val updatedReminder = reminder.copy(isEnabled = isEnabled)
                                    val result = ReminderUtils.updateReminder(id, updatedReminder, context)
                                    if (result.isSuccess) {
                                        // Listeyi güncelle
                                        reminders = reminders.map {
                                            if (it.first == id) id to updatedReminder
                                            else it
                                        }
                                        showToast(context, "Hatırlatıcı durumu güncellendi")
                                    } else {
                                        showToast(context, "Hatırlatıcı güncellenemedi")
                                    }
                                }
                            },
                            onEditClick = {
                                // Düzenleme için mevcut hatırlatıcıyı ayarla
                                editingReminder = id to reminder
                                showAddDialog = true
                            },
                            onDeleteClick = {
                                // Hatırlatıcıyı sil
                                coroutineScope.launch {
                                    val result = ReminderUtils.deleteReminder(id, context)
                                    if (result.isSuccess) {
                                        reminders = reminders.filter { it.first != id }
                                        showToast(context, "Hatırlatıcı silindi")
                                    } else {
                                        showToast(context, "Hatırlatıcı silinemedi: ${result.exceptionOrNull()?.message ?: "Bilinmeyen hata"}")
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Hatırlatıcı Ekleme/Düzenleme Diyaloğu
            if (showAddDialog) {
                ModernReminderDialog(
                    onDismiss = {
                        showAddDialog = false
                        editingReminder = null
                    },
                    onSave = { newReminder ->
                        coroutineScope.launch {
                            if (editingReminder != null) {
                                // Düzenleme
                                val result = ReminderUtils.updateReminder(editingReminder!!.first, newReminder, context)
                                if (result.isSuccess) {
                                    reminders = reminders.map {
                                        if (it.first == editingReminder!!.first) editingReminder!!.first to newReminder
                                        else it
                                    }
                                    showToast(context, "Hatırlatıcı güncellendi")
                                } else {
                                    showToast(context, "Hatırlatıcı güncellenemedi")
                                }
                            } else {
                                // Ekleme
                                val result = ReminderUtils.addReminder(newReminder, context)
                                if (result.isSuccess) {
                                    val newId = result.getOrDefault("")
                                    reminders = reminders + (newId to newReminder)
                                    showToast(context, "Hatırlatıcı eklendi")
                                } else {
                                    showToast(context, "Hatırlatıcı eklenemedi")
                                }
                            }
                        }
                        showAddDialog = false
                        editingReminder = null
                    },
                    initialReminder = editingReminder?.second
                )
            }
        }
    }
}

@Composable
fun ModernReminderItem(
    reminder: Reminder,
    onToggleEnabled: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sol taraf: Kategori ve Başlık
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Kategori etiketi
                    Text(
                        text = reminder.category.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    // Başlık
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Saat bilgisi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Saat",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = reminder.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Sağ taraf: Düzenleme ve Silme Aksiyonları
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Aktif/Pasif Anahtarı
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggleEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                // Düzenleme ve Silme Butonları
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Düzenle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernReminderDialog(
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
    initialReminder: Reminder? = null
) {
    val ctx = LocalContext.current
    var title by remember { mutableStateOf(initialReminder?.title ?: "") }
    var time by remember { mutableStateOf(initialReminder?.time ?: "") }
    var description by remember { mutableStateOf(initialReminder?.description ?: "") }
    var category by remember { mutableStateOf(initialReminder?.category ?: ReminderCategory.GENERAL) }
    var type by remember { mutableStateOf(initialReminder?.type ?: ReminderType.SINGLE) }
    var isEnabled by remember { mutableStateOf(initialReminder?.isEnabled ?: true) }

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialReminder == null)
                    stringResource(R.string.add_reminder)
                else
                    stringResource(R.string.edit_reminder)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Başlık
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.reminder_title)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Açıklama
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.reminder_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3
                )

                // Saat Seçimi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = stringResource(R.string.reminder_time)
                    )
                    Text(
                        text = time.ifEmpty { stringResource(R.string.select_time) },
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                showTimePicker(context) { selectedTime ->
                                    time = selectedTime
                                }
                            }
                            .padding(8.dp)
                    )
                }

                // Kategori
                Text(stringResource(R.string.reminder_category), style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ReminderCategory.values()) { reminderCategory ->
                        FilterChip(
                            selected = category == reminderCategory,
                            onClick = { category = reminderCategory },
                            label = { Text(reminderCategory.name) },
                            leadingIcon = { /* ikon kodu aynı */ }
                        )
                    }
                }

                // Tekrar Türü
                Text(stringResource(R.string.reminder_type), style = MaterialTheme.typography.bodyMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ReminderType.values()) { reminderType ->
                        FilterChip(
                            selected = type == reminderType,
                            onClick = { type = reminderType },
                            label = {
                                Text(
                                    when (reminderType) {
                                        ReminderType.SINGLE  -> stringResource(R.string.type_single)
                                        ReminderType.DAILY   -> stringResource(R.string.type_daily)
                                        ReminderType.WEEKLY  -> stringResource(R.string.type_weekly)
                                        ReminderType.MONTHLY -> stringResource(R.string.type_monthly)
                                    }
                                )
                            },
                            leadingIcon = { /* ikon kodu aynı */ }
                        )
                    }
                }

                // Aktif/Pasif Anahtarı
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.reminder_active), style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && time.isNotBlank()) {
                        val newReminder = Reminder(
                            /* ... */
                        )
                        onSave(newReminder)
                        onDismiss()
                    } else {
                        val msg = ctx.getString(R.string.error_empty_fields)
                        showToast(ctx, msg)
                    }
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

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
                stringResource(R.string.reminders),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            stringResource(R.string.no_reminders),
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
            Text(stringResource(R.string.add_reminder))
        }
    }
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

// Toast mesajı göster
private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}