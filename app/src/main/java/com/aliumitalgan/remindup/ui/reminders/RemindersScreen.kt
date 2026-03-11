package com.aliumitalgan.remindup.ui.reminders

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.EmptyRemindersView
import com.aliumitalgan.remindup.components.ReminderCard
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.core.di.LocalAppContainer
import com.aliumitalgan.remindup.models.ReminderCategory
import com.aliumitalgan.remindup.models.ReminderType
import com.aliumitalgan.remindup.screens.FilterChips
import com.aliumitalgan.remindup.screens.ModernReminderDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: RemindersViewModel = viewModel(
        factory = RemindersViewModelFactory(LocalAppContainer.current)
    )
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    var selectedNavItem by remember { mutableStateOf("goals") }

    val bottomNavItems = mainBottomNavItems()

    val editingReminder = state.editingReminderId?.let { id ->
        state.allReminders.firstOrNull { it.id == id }?.reminder
    }

    LaunchedEffect(state.message) {
        val message = state.message ?: return@LaunchedEffect
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        viewModel.onEvent(RemindersUiEvent.ClearMessage)
    }

    val headerContent: @Composable () -> Unit = {
        Column {
            TopAppBar(
                title = {
                    if (state.isSearchActive) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = {
                                viewModel.onEvent(RemindersUiEvent.SearchQueryChanged(it))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Hatırlatıcı ara...") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.reminders),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onEvent(RemindersUiEvent.ToggleSearch) }) {
                        Icon(
                            imageVector = if (state.isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = { viewModel.onEvent(RemindersUiEvent.ShowAddDialog) }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )

            AnimatedVisibility(
                visible = !state.isSearchActive || state.searchQuery.isEmpty(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                FilterChips(
                    selectedCategory = state.selectedCategory,
                    selectedType = state.selectedType,
                    onCategorySelected = {
                        viewModel.onEvent(RemindersUiEvent.CategoryFilterChanged(it))
                    },
                    onTypeSelected = {
                        viewModel.onEvent(RemindersUiEvent.TypeFilterChanged(it))
                    }
                )
            }

            AnimatedVisibility(
                visible = state.searchQuery.isNotEmpty() ||
                    state.selectedCategory != null ||
                    state.selectedType != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "${state.visibleReminders.size} sonuç",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    state.selectedCategory?.let { category ->
                        FilterChip(
                            selected = true,
                            onClick = {
                                viewModel.onEvent(RemindersUiEvent.CategoryFilterChanged(null))
                            },
                            label = { Text(categoryLabel(category)) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = null, Modifier.size(16.dp))
                            },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = FilterChipDefaults.filterChipColors()
                        )
                    }

                    state.selectedType?.let { type ->
                        FilterChip(
                            selected = true,
                            onClick = {
                                viewModel.onEvent(RemindersUiEvent.TypeFilterChanged(null))
                            },
                            label = { Text(typeLabel(type)) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = null, Modifier.size(16.dp))
                            },
                            modifier = Modifier.padding(end = 8.dp),
                            colors = FilterChipDefaults.filterChipColors()
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = { viewModel.onEvent(RemindersUiEvent.ClearFilters) }) {
                        Text("Temizle")
                    }
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                items = bottomNavItems,
                currentRoute = selectedNavItem,
                onItemSelected = { route ->
                    selectedNavItem = route
                    when (route) {
                        "home" -> onNavigateToHome()
                        "goals" -> onNavigateToGoals()
                        "analytic" -> onNavigateToProgress()
                        "settings" -> onNavigateToSettings()
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
            when {
                state.isLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        headerContent()
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
                state.visibleReminders.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Top
                    ) {
                        headerContent()
                        EmptyRemindersView(
                            onAddClick = { viewModel.onEvent(RemindersUiEvent.ShowAddDialog) },
                            isFiltered = state.searchQuery.isNotEmpty() ||
                                state.selectedCategory != null ||
                                state.selectedType != null
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            headerContent()
                        }
                        items(state.visibleReminders, key = { it.id }) { record ->
                            ReminderCard(
                                reminder = record.reminder,
                                onToggleEnabled = { isEnabled ->
                                    viewModel.onEvent(
                                        RemindersUiEvent.ToggleEnabled(
                                            reminderId = record.id,
                                            enabled = isEnabled
                                        )
                                    )
                                },
                                onEditClick = {
                                    viewModel.onEvent(RemindersUiEvent.ShowEditDialog(record.id))
                                },
                                onDeleteClick = {
                                    viewModel.onEvent(RemindersUiEvent.DeleteReminder(record.id))
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }

            if (state.showDialog) {
                ModernReminderDialog(
                    onDismiss = { viewModel.onEvent(RemindersUiEvent.DismissDialog) },
                    onSave = { reminder ->
                        viewModel.onEvent(RemindersUiEvent.SaveReminder(reminder))
                    },
                    initialReminder = editingReminder
                )
            }
        }
    }
}

private fun categoryLabel(category: ReminderCategory): String {
    return when (category) {
        ReminderCategory.GENERAL -> "Genel"
        ReminderCategory.WORK -> "İş"
        ReminderCategory.HEALTH -> "Sağlık"
        ReminderCategory.PERSONAL -> "Kişisel"
        ReminderCategory.STUDY -> "Çalışma"
        ReminderCategory.FITNESS -> "Spor"
    }
}

private fun typeLabel(type: ReminderType): String {
    return when (type) {
        ReminderType.SINGLE -> "Tek Seferlik"
        ReminderType.DAILY -> "Günlük"
        ReminderType.WEEKLY -> "Haftalık"
        ReminderType.MONTHLY -> "Aylık"
    }
}


