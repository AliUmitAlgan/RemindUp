package com.aliumitalgan.remindup.screens

import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.components.EmptyRemindersView
import com.aliumitalgan.remindup.components.ReminderCard
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.models.ReminderCategory
import com.aliumitalgan.remindup.models.ReminderType
import com.aliumitalgan.remindup.utils.ReminderUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

    // Filter state
    var selectedCategory by remember { mutableStateOf<ReminderCategory?>(null) }
    var selectedType by remember { mutableStateOf<ReminderType?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // Bottom Navigation Items
    val bottomNavItems = listOf(
        BottomNavItem(stringResource(R.string.home), Icons.Filled.Home, Icons.Outlined.Home, "home"),
        BottomNavItem(stringResource(R.string.goals), Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle, "goals"),
        BottomNavItem(stringResource(R.string.reminders), Icons.Filled.Notifications, Icons.Outlined.Notifications, "reminders"),
        BottomNavItem(stringResource(R.string.progress), Icons.Filled.ShowChart, Icons.Outlined.ShowChart, "progress"),
        BottomNavItem(stringResource(R.string.profile), Icons.Filled.Person, Icons.Outlined.Person, "profile")
    )
    var selectedNavItem by remember { mutableStateOf(bottomNavItems[2].route) }

    // Verileri yükle
    LaunchedEffect(Unit, selectedCategory, selectedType, searchQuery) {
        try {
            isLoading = true
            // Load all reminders (suspend fonksiyon çağrısı)
            val result = ReminderUtils.getUserReminders()

            if (result.isSuccess) {
                var filteredReminders = result.getOrDefault(emptyList())

                // Apply category filter
                selectedCategory?.let { cat ->
                    filteredReminders = filteredReminders.filter { it.second.category == cat }
                }

                // Apply type filter
                selectedType?.let { type ->
                    filteredReminders = filteredReminders.filter { it.second.type == type }
                }

                // Apply search filter
                if (searchQuery.isNotEmpty()) {
                    filteredReminders = filteredReminders.filter {
                        it.second.title.contains(searchQuery, ignoreCase = true) ||
                                it.second.description.contains(searchQuery, ignoreCase = true)
                    }
                }

                // Sort by time
                reminders = filteredReminders.sortedBy { it.second.time }
            } else {
                reminders = emptyList()
                showToast(context, "Yüklenemedi: ${result.exceptionOrNull()?.message ?: "Bilinmeyen hata"}")
            }
        } catch (e: Exception) {
            reminders = emptyList()
            showToast(context, "Hata: ${e.message}")
        } finally {
            isLoading = false
        }
    }


    Scaffold(
        topBar = {
            Column {
                // Main Top Bar with Search
                TopAppBar(
                    title = {
                        if (isSearchActive) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Hatırlatıcı ara...") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                                    cursorColor = MaterialTheme.colorScheme.primary,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.reminders),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    },
                    actions = {
                        // Search toggle button
                        IconButton(onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) searchQuery = ""
                        }) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (isSearchActive) "Aramayı Kapat" else "Ara"
                            )
                        }

                        // Add reminder button
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.add_reminder)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )

                // Category filter chips
                AnimatedVisibility(
                    visible = !isSearchActive || searchQuery.isEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    FilterChips(
                        selectedCategory = selectedCategory,
                        selectedType = selectedType,
                        onCategorySelected = { selectedCategory = it },
                        onTypeSelected = { selectedType = it }
                    )
                }

                // Active filters info
                AnimatedVisibility(
                    visible = searchQuery.isNotEmpty() || selectedCategory != null || selectedType != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Active filters count
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = "${reminders.size} sonuç",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        // Show active filters
                        if (selectedCategory != null) {
                            FilterChip(
                                selected = true,
                                onClick = { selectedCategory = null },
                                label = {
                                    Text(getCategoryName(selectedCategory!!))
                                },
                                trailingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = null, Modifier.size(16.dp))
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        if (selectedType != null) {
                            FilterChip(
                                selected = true,
                                onClick = { selectedType = null },
                                label = {
                                    Text(getTypeName(selectedType!!))
                                },
                                trailingIcon = {
                                    Icon(Icons.Default.Close, contentDescription = null, Modifier.size(16.dp))
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Clear all filters button
                        if (searchQuery.isNotEmpty() || selectedCategory != null || selectedType != null) {
                            TextButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategory = null
                                    selectedType = null
                                    isSearchActive = false
                                }
                            ) {
                                Icon(Icons.Default.FilterAlt, contentDescription = null, Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Temizle")
                            }
                        }
                    }
                }
            }
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 16.dp)
                    .size(56.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_reminder),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Content
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }
            } else if (reminders.isEmpty()) {
                // No reminders state
                EmptyRemindersView(
                    onAddClick = { showAddDialog = true },
                    isFiltered = searchQuery.isNotEmpty() || selectedCategory != null || selectedType != null
                )
            } else {
                // Reminders list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reminders, key = { it.first }) { (id, reminder) ->
                        ReminderCard(
                            reminder = reminder,
                            onToggleEnabled = { isEnabled ->
                                coroutineScope.launch {
                                    val updatedReminder = reminder.copy(isEnabled = isEnabled)
                                    val result = ReminderUtils.updateReminder(id, updatedReminder, context)
                                    if (result.isSuccess) {
                                        // Update local list
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
                                editingReminder = id to reminder
                                showAddDialog = true
                            },
                            onDeleteClick = {
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

                    // Add extra space at the bottom for FAB
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            // Reminder Add/Edit Dialog
            if (showAddDialog) {
                val reminderToEdit = editingReminder?.second
                ModernReminderDialog(
                    onDismiss = {
                        showAddDialog = false
                        editingReminder = null
                    },
                    onSave = { newReminderData ->
                        coroutineScope.launch {
                            val currentEditingReminder = editingReminder

                            if (currentEditingReminder != null) {
                                // Update existing reminder
                                val (id, existingReminder) = currentEditingReminder
                                val updatedReminder = existingReminder.copy(
                                    title       = newReminderData.title,
                                    time        = newReminderData.time,
                                    description = newReminderData.description,
                                    category    = newReminderData.category,
                                    type        = newReminderData.type,
                                    isEnabled   = newReminderData.isEnabled,
                                    isImportant = newReminderData.isImportant // Add new field
                                )

                                val result = ReminderUtils.updateReminder(id, updatedReminder, context)
                                if (result.isSuccess) {
                                    reminders = reminders.map {
                                        if (it.first == id) id to updatedReminder else it
                                    }
                                    showToast(context, "Hatırlatıcı güncellendi")
                                } else {
                                    showToast(context, "Güncelleme başarısız: ${result.exceptionOrNull()?.message}")
                                }
                            } else {
                                // Add new reminder
                                val newReminder = newReminderData.copy(
                                    userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                                )
                                val result = ReminderUtils.addReminder(newReminder, context)
                                if (result.isSuccess) {
                                    val newId = result.getOrDefault("")
                                    reminders = reminders + (newId to newReminder)
                                    showToast(context, "Hatırlatıcı eklendi")
                                } else {
                                    showToast(context, "Ekleme başarısız: ${result.exceptionOrNull()?.message}")
                                }
                            }

                            showAddDialog = false
                            editingReminder = null
                        }
                    },
                    initialReminder = reminderToEdit
                )
            }
        }
    }
}

@Composable
fun FilterChips(
    selectedCategory: ReminderCategory?,
    selectedType: ReminderType?,
    onCategorySelected: (ReminderCategory?) -> Unit,
    onTypeSelected: (ReminderType?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Categories scrollable row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            // Add "All" option
            item {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelected(null) },
                    label = { Text("Tümü") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            // Add category options
            items(ReminderCategory.values()) { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        if (selectedCategory == category) onCategorySelected(null)
                        else onCategorySelected(category)
                    },
                    label = { Text(getCategoryName(category)) },
                    leadingIcon = {
                        Icon(
                            imageVector = getCategoryIcon(category),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = getCategoryChipColor(category),
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    )
                )
            }
        }

        // Types scrollable row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add "All" option
            item {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { onTypeSelected(null) },
                    label = { Text("Tümü") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }

            // Add type options
            items(ReminderType.values()) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = {
                        if (selectedType == type) onTypeSelected(null)
                        else onTypeSelected(type)
                    },
                    label = { Text(getTypeName(type)) },
                    leadingIcon = {
                        Icon(
                            imageVector = getTypeIcon(type),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernReminderDialog(
    onDismiss: () -> Unit,
    onSave: (Reminder) -> Unit,
    initialReminder: Reminder? = null
) {
    // State variables
    var title by remember { mutableStateOf(initialReminder?.title ?: "") }
    var time by remember { mutableStateOf(initialReminder?.time ?: getCurrentTime()) }
    var description by remember { mutableStateOf(initialReminder?.description ?: "") }
    var category by remember { mutableStateOf(initialReminder?.category ?: ReminderCategory.GENERAL) }
    var type by remember { mutableStateOf(initialReminder?.type ?: ReminderType.SINGLE) }
    var isEnabled by remember { mutableStateOf(initialReminder?.isEnabled ?: true) }
    var isImportant by remember { mutableStateOf(initialReminder?.isImportant ?: false) } // New field

    val isEditing = initialReminder != null

    val context = LocalContext.current

    // Time picker dialog
    var showTimePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dialog header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = if (isEditing) Icons.Default.Edit else Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = if (isEditing) stringResource(R.string.edit_reminder) else stringResource(R.string.add_reminder),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Divider()

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.reminder_title)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Title, contentDescription = null)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Time field
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text(stringResource(R.string.reminder_time)) },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTimePicker = true },
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.AccessTime, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.Schedule, contentDescription = "Saat Seç")
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.reminder_description)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(Icons.Default.Description, contentDescription = null)
                    },
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Category selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.reminder_category),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(100.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Category chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(ReminderCategory.values()) { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(getCategoryName(cat), fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getCategoryIcon(cat),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = getCategoryChipColor(cat),
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Reminder type selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.reminder_type),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(100.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Type chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(ReminderType.values()) { reminderType ->
                            FilterChip(
                                selected = type == reminderType,
                                onClick = { type = reminderType },
                                label = { Text(getTypeName(reminderType), fontSize = 12.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = getTypeIcon(reminderType),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                    selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary
                                )
                            )
                        }
                    }
                }

                // Reminder status toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Enabled toggle
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Text(
                            text = stringResource(R.string.reminder_active),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    // Important toggle (new feature)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = isImportant,
                            onCheckedChange = { isImportant = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = MaterialTheme.colorScheme.error
                            )
                        )

                        Text(
                            text = "Önemli",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Divider()

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    // Save button
                    Button(
                        onClick = {
                            if (title.isNotBlank() && time.isNotBlank()) {
                                val newReminder = Reminder(
                                    id = initialReminder?.id ?: UUID.randomUUID().toString(),
                                    title = title,
                                    time = time,
                                    description = description,
                                    category = category,
                                    type = type,
                                    isEnabled = isEnabled,
                                    isImportant = isImportant,
                                    userId = initialReminder?.userId ?: ""
                                )
                                onSave(newReminder)
                            } else {
                                Toast.makeText(context, "Başlık ve saat alanları boş olamaz", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }

    // Show time picker dialog
    if (showTimePicker) {
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val initialTime = try {
            timeFormatter.parse(time)
        } catch (e: Exception) {
            Calendar.getInstance().time
        }

        val calendar = Calendar.getInstance().apply {
            if (initialTime != null) {
                set(Calendar.HOUR_OF_DAY, initialTime.hours)
                set(Calendar.MINUTE, initialTime.minutes)
            }
        }

        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                time = String.format("%02d:%02d", hourOfDay, minute)
                showTimePicker = false
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }
}

// Modern UI için yardımcı fonksiyonlar
private fun getCurrentTime(): String {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    return String.format("%02d:%02d", hour, minute)
}

private fun getCategoryName(category: ReminderCategory): String {
    return when (category) {
        ReminderCategory.GENERAL -> "Genel"
        ReminderCategory.WORK -> "İş"
        ReminderCategory.HEALTH -> "Sağlık"
        ReminderCategory.PERSONAL -> "Kişisel"
        ReminderCategory.STUDY -> "Çalışma"
        ReminderCategory.FITNESS -> "Spor"
    }
}

private fun getTypeName(type: ReminderType): String {
    return when (type) {
        ReminderType.SINGLE -> "Tek Seferlik"
        ReminderType.DAILY -> "Günlük"
        ReminderType.WEEKLY -> "Haftalık"
        ReminderType.MONTHLY -> "Aylık"
    }
}

private fun getCategoryIcon(category: ReminderCategory): ImageVector {
    return when (category) {
        ReminderCategory.GENERAL -> Icons.Default.Notifications
        ReminderCategory.WORK -> Icons.Default.Work
        ReminderCategory.HEALTH -> Icons.Default.Favorite
        ReminderCategory.PERSONAL -> Icons.Default.Person
        ReminderCategory.STUDY -> Icons.Default.School
        ReminderCategory.FITNESS -> Icons.Default.FitnessCenter
    }
}

private fun getTypeIcon(type: ReminderType): ImageVector {
    return when (type) {
        ReminderType.SINGLE -> Icons.Default.Alarm
        ReminderType.DAILY -> Icons.Default.Today
        ReminderType.WEEKLY -> Icons.Default.DateRange
        ReminderType.MONTHLY -> Icons.Default.Event
    }
}


private fun getCategoryChipColor(category: ReminderCategory): Color {
    return when (category) {
        ReminderCategory.GENERAL -> Color(0xFF1976D2)  // Blue
        ReminderCategory.WORK -> Color(0xFFE91E63)     // Pink
        ReminderCategory.HEALTH -> Color(0xFFE53935)   // Red
        ReminderCategory.PERSONAL -> Color(0xFF9C27B0) // Purple
        ReminderCategory.STUDY -> Color(0xFF00897B)    // Teal
        ReminderCategory.FITNESS -> Color(0xFF7CB342)  // Green
    }
}
