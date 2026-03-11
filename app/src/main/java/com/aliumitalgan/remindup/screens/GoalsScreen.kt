package com.aliumitalgan.remindup.screens
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.DeleteGoalDialog
import com.aliumitalgan.remindup.components.EmptyGoalsView
import com.aliumitalgan.remindup.components.NoGoalsFoundView
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.core.di.LocalAppContainer
import com.aliumitalgan.remindup.core.di.RemindUpViewModelFactory
import com.aliumitalgan.remindup.domain.model.Goal
import com.aliumitalgan.remindup.domain.model.GoalCategory
import com.aliumitalgan.remindup.presentation.goals.GOALS_FILTER_ALL
import com.aliumitalgan.remindup.presentation.goals.GoalsViewModel
import com.aliumitalgan.remindup.ui.theme.appTextPrimary
import com.aliumitalgan.remindup.ui.theme.appTextSecondary
import com.aliumitalgan.remindup.ui.theme.themedColor
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

private val GoalsBackground: Color
    get() = themedColor(Color(0xFFF4F3F3), Color(0xFF0F131A))
private val AccentOrange = Color(0xFFF26522)
private val AccentPurple = Color(0xFFA66CF1)
private val AccentMint = Color(0xFF2BBF8A)
private val AccentGreen = Color(0xFF22C55E)
private val GoalsHeaderSurface: Color
    get() = themedColor(Color(0xFFFBFBFC), Color(0xFF171D26))
private val GoalsChipContainer: Color
    get() = themedColor(Color(0xFFEFF2F6), Color(0xFF243041))
private val GoalsCardSurface: Color
    get() = themedColor(Color(0xFFFAFAFB), Color(0xFF171D26))
private val GoalsWarmCard: Color
    get() = themedColor(Color(0xFFF9E7DC), Color(0xFF241B16))
private val GoalsWarmCardText: Color
    get() = themedColor(Color(0xFF8B6F63), Color(0xFFC7B0A3))
private val GoalsTrackColor: Color
    get() = themedColor(Color(0xFFE5E7EB), Color(0xFF2A3542))
private val GoalsSearchField: Color
    get() = themedColor(Color(0xFFF3F4F6), Color(0xFF222C3B))
private val GoalsIconButton: Color
    get() = themedColor(Color(0xFFF2F4F7), Color(0xFF232F40))
private val GoalTimeInputFormatters = listOf(
    DateTimeFormatter.ofPattern("HH:mm"),
    DateTimeFormatter.ofPattern("hh:mm a")
)
private val GoalTimeOutputFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("hh:mm a")
private val weekDayLabels = listOf("P", "S", "C", "P", "C", "C", "P")
private val timeHourOptions = (1..12).toList()
private val timeMinuteOptions = (0..59).toList()
private val GoalsPopupPrimary: Color
    get() = themedColor(Color(0xFFF68B5C), Color(0xFFFF9D6E))
private val GoalsPopupOnPrimary: Color
    get() = themedColor(Color.White, Color(0xFF2A1A12))
private val GoalsPopupSecondarySurface: Color
    get() = themedColor(Color(0xFFE8ECF4), Color(0xFF314158))
private val GoalsPopupChipSurface: Color
    get() = themedColor(Color(0xFFEFF2F7), Color(0xFF253143))
private val GoalsPopupChipSelectedSurface: Color
    get() = themedColor(Color(0xFFFFEEE5), Color(0xFF4A2B1D))
private val GoalsPopupMutedText: Color
    get() = themedColor(Color(0xFF64748B), Color(0xFFAEB6C5))

private data class GoalFilterOption(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val accent: Color,
    val isAddAction: Boolean = false
)

private data class GoalCategoryPresentation(
    val name: String,
    val accent: Color,
    val icon: ImageVector
)

private enum class TimeSelectorTarget {
    Hour,
    Minute
}

private const val GOALS_FILTER_NEW_CATEGORY = "new_category"

private val categoryIconOptions = listOf(
    "spa" to Icons.Filled.Spa,
    "fitness_center" to Icons.Filled.FitnessCenter,
    "bookmark" to Icons.Filled.Bookmark,
    "nightlight" to Icons.Filled.Nightlight,
    "coffee" to Icons.Filled.Coffee,
    "work" to Icons.Filled.Work,
    "alarm" to Icons.Filled.Alarm,
    "pets" to Icons.Filled.Pets,
    "self_care" to Icons.Filled.SelfImprovement,
    "local_dining" to Icons.Filled.LocalDining,
    "directions_run" to Icons.AutoMirrored.Filled.DirectionsRun,
    "school" to Icons.Filled.School,
    "movie" to Icons.Filled.Movie,
    "shopping_cart" to Icons.Filled.ShoppingCart,
    "favorite" to Icons.Filled.Favorite
)

@Composable
fun GoalsScreenContent(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToReminders: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {},
    onNavigateToSocial: () -> Unit = {},
    onNavigateToEditCategory: (String?) -> Unit = {},
    onNavigateToSweetTaskDetail: (String) -> Unit = {},
    viewModel: GoalsViewModel = viewModel(
        factory = RemindUpViewModelFactory(LocalAppContainer.current)
    )
) {
    val uiState by viewModel.uiState.collectAsState()
    val goals = uiState.goals
    val categories = uiState.categories
    val isLoading = uiState.isLoading
    val selectedFilterId = uiState.selectedFilterId
    val categoriesById = remember(categories) { categories.associateBy { it.id } }

    var showAddDialog by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var selectedCategoryIdInput by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(8, 0)) }
    var smartRemindersEnabled by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf("goals") }
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var pendingDeleteGoalId by remember { mutableStateOf<String?>(null) }
    var showDatePickerPopup by remember { mutableStateOf(false) }
    var showTimePickerPopup by remember { mutableStateOf(false) }
    val dateLabelFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }
    val displayTimeFormatter = remember { DateTimeFormatter.ofPattern("hh:mm a") }
    val storageTimeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val listState = rememberLazyListState()
    val filterScrollState = rememberScrollState()
    val lifecycleOwner = LocalLifecycleOwner.current

    val navItems = mainBottomNavItems()
    val filterOptions = remember(categories) {
        buildList {
            add(
                GoalFilterOption(
                    key = GOALS_FILTER_ALL,
                    label = "All",
                    icon = Icons.Filled.AutoAwesome,
                    accent = Color(0xFF718096)
                )
            )
            add(
                GoalFilterOption(
                    key = GOALS_FILTER_NEW_CATEGORY,
                    label = "+ New Category",
                    icon = Icons.Filled.Add,
                    accent = AccentOrange,
                    isAddAction = true
                )
            )
            categories.forEach { category ->
                add(
                    GoalFilterOption(
                        key = category.id,
                        label = category.name.ifBlank { "Category" },
                        icon = iconForCategoryKey(category.iconKey),
                        accent = parseCategoryAccent(category.colorHex)
                    )
                )
            }
        }
    }
    val visibleGoals by remember(goals, selectedFilterId, searchQuery, categoriesById) {
        derivedStateOf {
            val query = searchQuery.trim()
            goals.asSequence()
                .filter { (_, goal) ->
                    selectedFilterId == GOALS_FILTER_ALL || goal.categoryId == selectedFilterId
                }
                .filter { (_, goal) ->
                    val categoryLabel = resolveCategoryPresentation(goal, categoriesById).name
                    query.isBlank() ||
                        goal.title.contains(query, ignoreCase = true) ||
                        goal.description.contains(query, ignoreCase = true) ||
                        categoryLabel.contains(query, ignoreCase = true)
                }
                .toList()
        }
    }
    val selectedFilterName = categoriesById[selectedFilterId]?.name?.ifBlank { "Category" } ?: "All"
    val headerTitle = if (selectedFilterId == GOALS_FILTER_ALL) "All Goals" else "$selectedFilterName Goals"
    val addGoalLabel = if (selectedFilterId == GOALS_FILTER_ALL) "Add New Goal" else "Add $selectedFilterName Goal"

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadCategories()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(categories) {
        if (selectedCategoryIdInput.isNotBlank() && categories.none { it.id == selectedCategoryIdInput }) {
            selectedCategoryIdInput = categories.firstOrNull()?.id.orEmpty()
        }
    }

    Scaffold(
        containerColor = GoalsBackground,
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = { route ->
                    currentRoute = route
                    when (route) {
                        "home" -> onNavigateToHome()
                        "goals" -> Unit
                        "social" -> onNavigateToSocial()
                        "analytic" -> onNavigateToProgress()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AccentOrange)
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Surface(
                        color = GoalsHeaderSurface,
                        shape = RoundedCornerShape(28.dp),
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = headerTitle,
                                    color = appTextPrimary,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 56.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HeaderCircleButton(
                                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        onClick = onNavigateBack
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    HeaderCircleButton(
                                        icon = Icons.Filled.Search,
                                        contentDescription = "Search goals",
                                        onClick = {
                                            if (isSearchVisible && searchQuery.isNotBlank()) {
                                                searchQuery = ""
                                            } else {
                                                isSearchVisible = !isSearchVisible
                                                if (!isSearchVisible) {
                                                    searchQuery = ""
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            if (isSearchVisible) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    placeholder = {
                                        Text(
                                            text = "Search goals",
                                            color = appTextSecondary
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Search,
                                            contentDescription = null,
                                            tint = appTextSecondary
                                        )
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotBlank()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = "Clear search",
                                                    tint = appTextSecondary
                                                )
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = GoalsSearchField,
                                        unfocusedContainerColor = GoalsSearchField,
                                        disabledContainerColor = GoalsSearchField,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        disabledIndicatorColor = Color.Transparent,
                                        focusedTextColor = appTextPrimary,
                                        unfocusedTextColor = appTextPrimary,
                                        cursorColor = AccentOrange
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(filterScrollState),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                filterOptions.forEach { filter ->
                                    GoalFilterChip(
                                        option = filter,
                                        selected = !filter.isAddAction && selectedFilterId == filter.key,
                                        onClick = {
                                            if (filter.isAddAction) {
                                                onNavigateToEditCategory(null)
                                            } else {
                                                viewModel.setFilter(filter.key)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (goals.isEmpty()) {
                    item {
                        EmptyGoalsView(
                            onCreateGoal = {
                                selectedCategoryIdInput = defaultCategoryForFilter(
                                    selectedFilterId = selectedFilterId,
                                    categories = categories,
                                    fallback = selectedCategoryIdInput
                                )
                                showAddDialog = true
                            }
                        )
                    }
                } else if (visibleGoals.isEmpty()) {
                    item {
                        NoGoalsFoundView(
                            onClearFilters = {
                                viewModel.setFilter(GOALS_FILTER_ALL)
                                searchQuery = ""
                                isSearchVisible = false
                            }
                        )
                    }
                } else {
                    items(
                        items = visibleGoals,
                        key = { it.first },
                        contentType = { "goal" }
                    ) { (goalId, goal) ->
                        GoalOverviewCard(
                            goal = goal,
                            categoriesById = categoriesById,
                            onClick = { onNavigateToSweetTaskDetail(goalId) },
                            onDeleteClick = { pendingDeleteGoalId = goalId }
                        )
                    }
                }

                item {
                    AddGoalInlineButton(
                        label = addGoalLabel,
                        onClick = {
                            selectedCategoryIdInput = defaultCategoryForFilter(
                                selectedFilterId = selectedFilterId,
                                categories = categories,
                                fallback = selectedCategoryIdInput
                            )
                            showAddDialog = true
                        }
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GoalsWarmCard,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Unlock Premium Gems", color = AccentOrange, fontWeight = FontWeight.ExtraBold)
                                Text(
                                    "Get 3D animated icons and advanced analytics for your goals.",
                                    color = GoalsWarmCardText,
                                    fontSize = 12.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(themedColor(Color.White, Color(0xFF2A3548))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = AccentOrange
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CreateGoalDialog(
            title = titleInput,
            categories = categories,
            selectedCategoryId = selectedCategoryIdInput,
            dateLabel = selectedDate.format(dateLabelFormatter),
            timeLabel = selectedTime.format(displayTimeFormatter),
            smartRemindersEnabled = smartRemindersEnabled,
            isSaving = isSaving,
            onDismiss = {
                showAddDialog = false
                showDatePickerPopup = false
                showTimePickerPopup = false
                isSaving = false
            },
            onTitleChange = { titleInput = it },
            onCategorySelected = { selectedCategoryIdInput = it },
            onCreateCategoryClick = {
                showAddDialog = false
                showDatePickerPopup = false
                showTimePickerPopup = false
                onNavigateToEditCategory(null)
            },
            onDateClick = { showDatePickerPopup = true },
            onTimeClick = { showTimePickerPopup = true },
            onSmartReminderToggle = { smartRemindersEnabled = it },
            onCreateGoal = {
                isSaving = true
                viewModel.addGoal(
                    title = titleInput.trim(),
                    description = "",
                    categoryId = selectedCategoryIdInput,
                    dueDate = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    reminderTime = selectedTime.format(storageTimeFormatter),
                    smartReminderEnabled = smartRemindersEnabled
                )
                titleInput = ""
                selectedCategoryIdInput = defaultCategoryForFilter(
                    selectedFilterId = selectedFilterId,
                    categories = categories,
                    fallback = selectedCategoryIdInput
                )
                selectedDate = LocalDate.now()
                selectedTime = LocalTime.of(8, 0)
                smartRemindersEnabled = true
                showDatePickerPopup = false
                showTimePickerPopup = false
                showAddDialog = false
                isSaving = false
            }
        )
    }

    if (showDatePickerPopup) {
        SweetDatePickerDialog(
            initialDate = selectedDate,
            onDismiss = { showDatePickerPopup = false },
            onConfirm = { pickedDate ->
                selectedDate = pickedDate
                showDatePickerPopup = false
            }
        )
    }

    if (showTimePickerPopup) {
        SweetTimePickerDialog(
            initialTime = selectedTime,
            onDismiss = { showTimePickerPopup = false },
            onConfirm = { pickedTime ->
                selectedTime = pickedTime
                showTimePickerPopup = false
            }
        )
    }

    pendingDeleteGoalId?.let { goalId ->
        DeleteGoalDialog(
            onDismiss = { pendingDeleteGoalId = null },
            onConfirmDelete = {
                viewModel.deleteGoal(goalId)
                pendingDeleteGoalId = null
            }
        )
    }
}

@Composable
private fun HeaderCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = GoalsIconButton,
        shape = CircleShape,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = appTextPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun GoalFilterChip(
    option: GoalFilterOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val isActionChip = option.isAddAction
    val selectedContentColor = if (option.accent.luminance() > 0.6f) {
        themedColor(Color(0xFF1F2937), Color(0xFF111827))
    } else {
        Color.White
    }
    val defaultContentColor = if (isActionChip) {
        AccentOrange
    } else if (option.accent.luminance() > 0.6f) {
        themedColor(Color(0xFF374151), Color(0xFFD1D5DB))
    } else {
        option.accent
    }
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = when {
            selected -> option.accent
            isActionChip -> themedColor(Color(0xFFFFF1EA), Color(0xFF2A3548))
            else -> GoalsChipContainer
        },
        border = when {
            selected || !isActionChip -> null
            else -> BorderStroke(1.dp, themedColor(Color(0xFFFFD9C8), Color(0xFF3B495F)))
        },
        modifier = Modifier.heightIn(min = 44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                tint = if (selected) selectedContentColor else defaultContentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = option.label,
                color = if (selected) selectedContentColor else defaultContentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AddGoalInlineButton(
    label: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentOrange,
            contentColor = Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun GoalOverviewCard(
    goal: Goal,
    categoriesById: Map<String, GoalCategory>,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val categoryPresentation = remember(goal.categoryId, goal.category, categoriesById) {
        resolveCategoryPresentation(goal, categoriesById)
    }
    val category = categoryPresentation.name
    val accent = categoryPresentation.accent
    val icon = categoryPresentation.icon
    val subtitle = remember(category, goal.reminderTime) { subtitleFor(category, goal.reminderTime) }
    val progressLabel = remember(category) { progressLabelFor(category) }
    val progressValue = remember(category, goal.progress) { progressValueFor(category, goal.progress) }
    val borderColor = themedColor(Color(0xFFE6EAF0), Color(0xFF253143))

    Surface(
        color = GoalsCardSurface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title.ifBlank { "Untitled Goal" },
                        color = appTextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = appTextSecondary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box {
                    IconButton(
                        onClick = { isMenuExpanded = !isMenuExpanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = "Goal actions",
                            tint = themedColor(Color(0xFF94A3B8), Color(0xFFAEB6C5))
                        )
                    }
                    if (isMenuExpanded) {
                        Surface(
                            onClick = {
                                isMenuExpanded = false
                                onDeleteClick()
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = themedColor(Color(0xFFFFF1EA), Color(0xFF2A3548)),
                            border = BorderStroke(
                                width = 1.dp,
                                color = themedColor(Color(0xFFFFD9C8), Color(0xFF334158))
                            ),
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 30.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Delete Goal",
                                    color = themedColor(Color(0xFF1F2937), Color(0xFFE5E7EB)),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = progressLabel,
                    color = themedColor(Color(0xFF6B7280), Color(0xFFAEB6C5)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = progressValue,
                    color = themedColor(Color(0xFF64748B), Color(0xFFD5DCE7)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp),
                color = GoalsTrackColor,
                shape = RoundedCornerShape(999.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(goal.progress.coerceIn(0, 100) / 100f)
                        .height(10.dp)
                        .background(accent)
                )
            }
        }
    }
}

@Composable
private fun SweetDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    var selectedDate by remember(initialDate, today) {
        mutableStateOf(if (initialDate.isBefore(today)) today else initialDate)
    }
    var displayedMonth by remember(initialDate, today) {
        mutableStateOf(YearMonth.from(if (initialDate.isBefore(today)) today else initialDate))
    }
    val monthCells = remember(displayedMonth) { buildMonthCells(displayedMonth) }
    val monthLabelFormatter = remember {
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    }
    val dialogContainer = themedColor(Color.White, Color(0xFF161D27))
    val heading = themedColor(Color(0xFF2B3342), Color(0xFFE5E7EB))
    val panelColor = themedColor(Color(0xFFF9FAFB), Color(0xFF222C3B))
    val weekdayColor = themedColor(Color(0xFF64748B), Color(0xFF9AA6B2))
    val closeBg = themedColor(Color(0xFFF3F4F6), Color(0xFF2A3548))
    val closeTint = themedColor(Color(0xFFB9C0CA), Color(0xFF94A3B8))
    val monthTitle = displayedMonth.atDay(1)
        .format(monthLabelFormatter)
        .replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(34.dp),
            color = dialogContainer,
            shadowElevation = 14.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Date",
                        color = heading,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(closeBg)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close date picker",
                            tint = closeTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    color = panelColor,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HeaderCircleButton(
                                icon = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous month",
                                onClick = { displayedMonth = displayedMonth.minusMonths(1) }
                            )
                            Text(
                                text = monthTitle,
                                color = heading,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.weight(1f)
                            )
                            HeaderCircleButton(
                                icon = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next month",
                                onClick = { displayedMonth = displayedMonth.plusMonths(1) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            weekDayLabels.forEach { label ->
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = weekdayColor,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            monthCells.chunked(7).forEach { week ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    week.forEach { day ->
                                        DateCell(
                                            date = day,
                                            today = today,
                                            selectedDate = selectedDate,
                                            onSelect = { selectedDate = it },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = GoalsPopupMutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(selectedDate)
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoalsPopupPrimary)
                    ) {
                        Text("Done", color = GoalsPopupOnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SweetTimePickerDialog(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit
) {
    var selectedHour by remember(initialTime) { mutableStateOf(to12Hour(initialTime.hour)) }
    var selectedMinute by remember(initialTime) { mutableStateOf(initialTime.minute) }
    var isPm by remember(initialTime) { mutableStateOf(initialTime.hour >= 12) }
    var activeSelector by remember { mutableStateOf(TimeSelectorTarget.Hour) }
    val dialogContainer = themedColor(Color.White, Color(0xFF161D27))
    val heading = themedColor(Color(0xFF2B3342), Color(0xFFE5E7EB))
    val panelColor = themedColor(Color(0xFFF9FAFB), Color(0xFF222C3B))
    val previewColor = themedColor(Color(0xFFF1F3F8), Color(0xFF263244))
    val dividerColor = themedColor(Color(0xFF64748B), Color(0xFFAEB6C5))
    val closeBg = themedColor(Color(0xFFF3F4F6), Color(0xFF2A3548))
    val closeTint = themedColor(Color(0xFFB9C0CA), Color(0xFF94A3B8))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(34.dp),
            color = dialogContainer,
            shadowElevation = 14.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Time",
                        color = heading,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(closeBg)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close time picker",
                            tint = closeTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    color = panelColor,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimeValueBox(
                                value = selectedHour.formatTwoDigits(),
                                modifier = Modifier.weight(1f),
                                container = previewColor,
                                selected = activeSelector == TimeSelectorTarget.Hour,
                                onClick = { activeSelector = TimeSelectorTarget.Hour }
                            )
                            Text(
                                text = ":",
                                color = dividerColor,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            )
                            TimeValueBox(
                                value = selectedMinute.formatTwoDigits(),
                                modifier = Modifier.weight(1f),
                                container = previewColor,
                                selected = activeSelector == TimeSelectorTarget.Minute,
                                onClick = { activeSelector = TimeSelectorTarget.Minute }
                            )
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = GoalsPopupSecondarySurface
                            ) {
                                Text(
                                    text = if (isPm) "PM" else "AM",
                                    color = appTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TimeChoiceChip(
                                label = "AM",
                                selected = !isPm,
                                onClick = { isPm = false },
                                modifier = Modifier.weight(1f)
                            )
                            TimeChoiceChip(
                                label = "PM",
                                selected = isPm,
                                onClick = { isPm = true },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(
                            text = if (activeSelector == TimeSelectorTarget.Hour) "Hour" else "Minute",
                            color = heading,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val options = if (activeSelector == TimeSelectorTarget.Hour) {
                                timeHourOptions
                            } else {
                                timeMinuteOptions
                            }
                            items(options, key = { it }) { value ->
                                TimeChoiceChip(
                                    label = value.formatTwoDigits(),
                                    selected = if (activeSelector == TimeSelectorTarget.Hour) {
                                        selectedHour == value
                                    } else {
                                        selectedMinute == value
                                    },
                                    onClick = {
                                        if (activeSelector == TimeSelectorTarget.Hour) {
                                            selectedHour = value
                                        } else {
                                            selectedMinute = value
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = GoalsPopupMutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onConfirm(LocalTime.of(to24Hour(selectedHour, isPm), selectedMinute))
                        },
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoalsPopupPrimary)
                    ) {
                        Text("Done", color = GoalsPopupOnPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DateCell(
    date: LocalDate?,
    today: LocalDate,
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    if (date == null) {
        Spacer(modifier = modifier.aspectRatio(1f))
        return
    }

    val isSelected = date == selectedDate
    val isToday = date == today
    val isPast = date.isBefore(today)
    val textColor = when {
        isPast -> GoalsPopupMutedText.copy(alpha = 0.55f)
        isSelected -> GoalsPopupOnPrimary
        else -> appTextPrimary
    }

    Surface(
        onClick = { if (!isPast) onSelect(date) },
        enabled = !isPast,
        shape = CircleShape,
        color = when {
            isSelected -> GoalsPopupPrimary
            isToday -> GoalsPopupPrimary.copy(alpha = 0.14f)
            else -> Color.Transparent
        },
        border = if (isToday && !isSelected) {
            BorderStroke(1.dp, GoalsPopupPrimary.copy(alpha = 0.5f))
        } else {
            null
        },
        modifier = modifier.aspectRatio(1f)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun TimeValueBox(
    value: String,
    container: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        color = container,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp,
            if (selected) GoalsPopupPrimary else Color.Transparent
        ),
        modifier = modifier.height(52.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                color = if (selected) GoalsPopupPrimary else appTextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
        }
    }
}

@Composable
private fun TimeChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) GoalsPopupChipSelectedSurface else GoalsPopupChipSurface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) GoalsPopupPrimary else Color.Transparent
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (selected) GoalsPopupPrimary else appTextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CreateGoalDialog(
    title: String,
    categories: List<GoalCategory>,
    selectedCategoryId: String,
    dateLabel: String,
    timeLabel: String,
    smartRemindersEnabled: Boolean,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onTitleChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onCreateCategoryClick: () -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onSmartReminderToggle: (Boolean) -> Unit,
    onCreateGoal: () -> Unit
) {
    val dialogContainer = themedColor(Color.White, Color(0xFF161D27))
    val heading = themedColor(Color(0xFF2B3342), Color(0xFFE5E7EB))
    val labelColor = themedColor(Color(0xFF7C8495), Color(0xFF9AA6B2))
    val fieldBackground = themedColor(Color(0xFFF9FAFB), Color(0xFF222C3B))
    val fieldText = themedColor(Color(0xFF313B4B), Color(0xFFE5E7EB))
    val closeBg = themedColor(Color(0xFFF3F4F6), Color(0xFF2A3548))
    val closeTint = themedColor(Color(0xFFB9C0CA), Color(0xFF94A3B8))

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(40.dp),
            color = dialogContainer,
            shadowElevation = 14.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 26.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Create Goal",
                        color = heading,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(closeBg)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = closeTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Goal Title",
                        color = labelColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    TextField(
                        value = title,
                        onValueChange = onTitleChange,
                        placeholder = {
                            Text(
                                text = "e.g. Morning Yoga",
                                color = labelColor.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFF2C34B),
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = fieldBackground,
                            unfocusedContainerColor = fieldBackground,
                            disabledContainerColor = fieldBackground,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            cursorColor = AccentOrange,
                            focusedTextColor = fieldText,
                            unfocusedTextColor = fieldText
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Category",
                        color = labelColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    if (categories.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = themedColor(Color(0xFFFFF7F2), Color(0xFF243041)),
                            border = BorderStroke(1.dp, themedColor(Color(0xFFFFE0D0), Color(0xFF334158)))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "No category yet",
                                    color = heading,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = onCreateCategoryClick) {
                                    Text(
                                        text = "New Category",
                                        color = AccentOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { category ->
                                GoalCategoryChip(
                                    icon = iconForCategoryKey(category.iconKey),
                                    label = category.name.ifBlank { "Category" },
                                    selected = selectedCategoryId == category.id,
                                    accent = parseCategoryAccent(category.colorHex),
                                    onClick = { onCategorySelected(category.id) }
                                )
                            }
                            GoalCategoryChip(
                                icon = Icons.Filled.Add,
                                label = "New",
                                selected = false,
                                accent = AccentOrange,
                                onClick = onCreateCategoryClick
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Date",
                            color = labelColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        SoftSelectorField(
                            value = dateLabel,
                            icon = Icons.Filled.CalendarMonth,
                            iconTint = Color(0xFFEF4444),
                            onClick = onDateClick
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Time",
                            color = labelColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        SoftSelectorField(
                            value = timeLabel,
                            icon = Icons.Filled.AccessTime,
                            iconTint = Color(0xFFEF4444),
                            onClick = onTimeClick
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(themedColor(Color(0xFFFFEDD5), Color(0xFF2A3548))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.NotificationsNone,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Reminders",
                            color = heading,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Gently nudge me",
                            color = labelColor,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = smartRemindersEnabled,
                        onCheckedChange = onSmartReminderToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFF68B5C),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = themedColor(Color(0xFFE5E7EB), Color(0xFF475569)),
                            checkedBorderColor = Color.Transparent,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                Button(
                    onClick = onCreateGoal,
                    enabled = title.isNotBlank() && !isSaving && (categories.isEmpty() || selectedCategoryId.isNotBlank()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF68B5C))
                ) {
                    Text(
                        text = if (isSaving) "Creating..." else "Create Goal",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalCategoryChip(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
) {
    val selectedContentColor = if (accent.luminance() > 0.6f) {
        themedColor(Color(0xFF1F2937), Color(0xFF111827))
    } else {
        Color.White
    }
    val selectedBackground = if (accent.luminance() > 0.7f) {
        accent.copy(alpha = 0.95f)
    } else {
        accent
    }
    val unselectedText = themedColor(Color(0xFF475569), Color(0xFFCBD5E1))
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(100),
        color = if (selected) selectedBackground else themedColor(Color(0xFFEFF2F7), Color(0xFF253143)),
        border = BorderStroke(
            2.dp,
            if (selected) accent.copy(alpha = 0.8f) else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) selectedContentColor else unselectedText,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                color = if (selected) selectedContentColor else unselectedText,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun SoftSelectorField(
    value: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = themedColor(Color(0xFFF9FAFB), Color(0xFF222C3B)),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                color = themedColor(Color(0xFF4B5563), Color(0xFFE5E7EB)),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun buildMonthCells(month: YearMonth): List<LocalDate?> {
    val firstDayOfMonth = month.atDay(1)
    val leadingEmptyCells = (firstDayOfMonth.dayOfWeek.value + 6) % 7
    val daysInMonth = month.lengthOfMonth()
    val totalCells = ((leadingEmptyCells + daysInMonth + 6) / 7) * 7

    return List(totalCells) { index ->
        val day = index - leadingEmptyCells + 1
        if (day in 1..daysInMonth) month.atDay(day) else null
    }
}

private fun to12Hour(hour24: Int): Int {
    val normalized = hour24 % 12
    return if (normalized == 0) 12 else normalized
}

private fun to24Hour(hour12: Int, isPm: Boolean): Int {
    val normalized = hour12 % 12
    return if (isPm) normalized + 12 else normalized
}

private fun Int.formatTwoDigits(): String = toString().padStart(2, '0')

private fun defaultCategoryForFilter(
    selectedFilterId: String,
    categories: List<GoalCategory>,
    fallback: String
): String {
    if (selectedFilterId != GOALS_FILTER_ALL && categories.any { it.id == selectedFilterId }) {
        return selectedFilterId
    }
    if (fallback.isNotBlank() && categories.any { it.id == fallback }) {
        return fallback
    }
    return categories.firstOrNull()?.id.orEmpty()
}

private fun subtitleFor(category: String, reminderTime: String): String {
    val cadence = when (category) {
        "Health" -> "Daily routine"
        "Learning" -> "Practice session"
        "Work" -> "Focus session"
        "Personal" -> "Personal check-in"
        "Hobby" -> "Creative session"
        else -> "Daily goal"
    }
    val time = formatGoalTime(reminderTime)
    return if (time != null) "$cadence - $time" else cadence
}

private fun progressLabelFor(category: String): String {
    return when (category) {
        "Health", "Personal", "Hobby" -> "Weekly Progress"
        else -> "Daily Progress"
    }
}

private fun progressValueFor(category: String, progress: Int): String {
    val completed = ((progress.coerceIn(0, 100) / 20f).roundToInt()).coerceIn(0, 5)
    return when (category) {
        "Health", "Personal", "Hobby" -> "$completed/5 days"
        "Learning" -> "$completed/5 sessions"
        "Work" -> "$completed/5 tasks"
        else -> "$progress%"
    }
}

private fun formatGoalTime(value: String): String? {
    val raw = value.trim()
    if (raw.isEmpty()) {
        return null
    }

    for (formatter in GoalTimeInputFormatters) {
        val parsed = runCatching {
            LocalTime.parse(raw, formatter)
        }.getOrNull()
        if (parsed != null) {
            return parsed.format(GoalTimeOutputFormatter)
        }
    }
    return null
}

private fun parseCategoryAccent(hex: String): Color {
    val cleaned = hex.trim().removePrefix("#")
    val colorLong = when (cleaned.length) {
        6 -> ("FF$cleaned").toLongOrNull(16)
        8 -> cleaned.toLongOrNull(16)
        else -> null
    } ?: return themedColor(Color(0xFF9BA3B8), Color(0xFFAEB6C5))
    return Color(colorLong)
}

private fun iconForCategoryKey(iconKey: String): ImageVector {
    return categoryIconOptions.firstOrNull { it.first == iconKey }?.second
        ?: Icons.Filled.SelfImprovement
}

private fun resolveCategoryPresentation(
    goal: Goal,
    categoriesById: Map<String, GoalCategory>
): GoalCategoryPresentation {
    val customCategory = goal.categoryId.takeIf { it.isNotBlank() }?.let(categoriesById::get)
    if (customCategory != null) {
        return GoalCategoryPresentation(
            name = customCategory.name.ifBlank { "Category" },
            accent = parseCategoryAccent(customCategory.colorHex),
            icon = iconForCategoryKey(customCategory.iconKey)
        )
    }

    val legacyName = mapCategory(goal.category)
    val legacyAccent = when (legacyName) {
        "Health" -> AccentOrange
        "Learning" -> AccentPurple
        "Work" -> AccentMint
        "Personal" -> AccentGreen
        "Hobby" -> Color(0xFF3B82F6)
        else -> Color(0xFF9BA3B8)
    }
    val legacyIcon = when (legacyName) {
        "Health" -> Icons.Filled.LocalFlorist
        "Learning" -> Icons.Filled.School
        "Work" -> Icons.Filled.Work
        "Personal" -> Icons.Filled.Star
        "Hobby" -> Icons.Filled.Brush
        else -> Icons.Filled.Language
    }
    return GoalCategoryPresentation(
        name = legacyName,
        accent = legacyAccent,
        icon = legacyIcon
    )
}

private fun mapCategory(category: Int): String {
    return when (category) {
        1 -> "Health"
        2 -> "Learning"
        3 -> "Work"
        4 -> "Personal"
        5 -> "Hobby"
        else -> "General"
    }
}
