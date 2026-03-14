package com.aliumitalgan.remindup.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.components.BottomNavigationBar
import com.aliumitalgan.remindup.components.mainBottomNavItems
import com.aliumitalgan.remindup.core.di.LocalAppContainer
import com.aliumitalgan.remindup.core.di.RemindUpViewModelFactory
import com.aliumitalgan.remindup.domain.model.GoalCategory
import com.aliumitalgan.remindup.presentation.category.EditCategoryViewModel
import com.aliumitalgan.remindup.ui.theme.themedColor
import java.util.UUID
import kotlin.math.roundToInt
import androidx.lifecycle.viewmodel.compose.viewModel

private val LightBg: Color
    get() = themedColor(Color(0xFFF5F3F4), Color(0xFF0F131A))
private val AccentOrange = Color(0xFFF26522)
private val Deep: Color
    get() = themedColor(Color(0xFF0F172A), Color(0xFFE5E7EB))
private val EditCard: Color
    get() = themedColor(Color.White, Color(0xFF171F2A))
private val EditPreviewBorder: Color
    get() = themedColor(Color(0xFFF4DFD3), Color(0xFF334158))
private val EditHeaderBack: Color
    get() = themedColor(Color(0xFFE9EEF6), Color(0xFF2A3548))
private val LightOrange = Color(0xFFF6D7C3)
private val LightMint = Color(0xFFC9E8DB)
private val LightLavender = Color(0xFFD8D9EB)
private val LightBlue = Color(0xFFDCE5ED)
private val LightYellow = Color(0xFFECE4B4)

private val themeColors = listOf(
    LightOrange to "Peach",
    LightMint to "Mint",
    LightLavender to "Lavender",
    LightBlue to "Soft Blue",
    LightYellow to "Lemon"
)

private data class CategoryIconOption(
    val key: String,
    val icon: ImageVector
)

private val categoryIcons = listOf(
    CategoryIconOption("self_care", Icons.Filled.SelfImprovement),
    CategoryIconOption("fitness_center", Icons.Filled.FitnessCenter),
    CategoryIconOption("bookmark", Icons.Filled.Bookmark),
    CategoryIconOption("nightlight", Icons.Filled.Nightlight),
    CategoryIconOption("coffee", Icons.Filled.Coffee),
    CategoryIconOption("work", Icons.Filled.Work),
    CategoryIconOption("shopping_cart", Icons.Filled.ShoppingCart),
    CategoryIconOption("pets", Icons.Filled.Pets),
    CategoryIconOption("spa", Icons.Filled.Spa),
    CategoryIconOption("alarm", Icons.Filled.Alarm),
    CategoryIconOption("local_dining", Icons.Filled.LocalDining),
    CategoryIconOption("directions_run", Icons.AutoMirrored.Filled.DirectionsRun),
    CategoryIconOption("school", Icons.Filled.School),
    CategoryIconOption("movie", Icons.Filled.Movie),
    CategoryIconOption("favorite", Icons.Filled.Favorite)
)

@Composable
fun EditCategoryScreen(
    categoryId: String?,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    viewModel: EditCategoryViewModel = viewModel(
        factory = RemindUpViewModelFactory(LocalAppContainer.current)
    )
) {
    val normalizedCategoryId = categoryId?.takeUnless { it.equals("new", ignoreCase = true) }
    val viewState by viewModel.uiState.collectAsState()
    var categoryName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(LightOrange) }
    var selectedIconIndex by remember { mutableStateOf(0) }
    var smartRemindersEnabled by remember { mutableStateOf(true) }
    var showAllIcons by remember { mutableStateOf(false) }
    val isSaving = viewState.isSaving
    val isDeleting = viewState.isDeleting
    val saveError = viewState.error
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val navItems = mainBottomNavItems()
    val visibleIconOptions = if (showAllIcons) {
        categoryIcons.mapIndexed { index, icon -> index to icon }
    } else {
        categoryIcons.take(8).mapIndexed { index, icon -> index to icon }
    }

    LaunchedEffect(normalizedCategoryId) {
        viewModel.loadCategory(normalizedCategoryId)
    }

    LaunchedEffect(viewState.loadedCategory?.id) {
        val category = viewState.loadedCategory ?: return@LaunchedEffect
        categoryName = category.name
        selectedColor = parseCategoryColor(category.colorHex)
        selectedIconIndex = categoryIcons.indexOfFirst { it.key == category.iconKey }
            .takeIf { it >= 0 } ?: 0
        smartRemindersEnabled = category.smartRemindersEnabled
    }

    Scaffold(
        containerColor = LightBg,
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                currentRoute = "goals",
                onItemSelected = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "goals" -> onNavigateToGoals()
                        "social" -> { }
                        "analytic" -> { }
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EditHeaderBack)
                        .clickable(onClick = onNavigateBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = themedColor(Color(0xFF64748B), Color(0xFFCBD5E1))
                    )
                }
                Text(
                    text = if (normalizedCategoryId == null) "New Category" else "Edit Category",
                    fontWeight = FontWeight.Bold,
                    color = Deep,
                    fontSize = 18.sp
                )
                TextButton(
                    enabled = !isSaving && !isDeleting && categoryName.isNotBlank(),
                    onClick = {
                        val trimmedName = categoryName.trim()
                        if (trimmedName.isBlank()) {
                            viewModel.clearError()
                            return@TextButton
                        }
                        val category = GoalCategory(
                            id = normalizedCategoryId ?: UUID.randomUUID().toString(),
                            name = trimmedName,
                            colorHex = selectedColor.toHexRgb(),
                            iconKey = categoryIcons.getOrNull(selectedIconIndex)?.key ?: "self_care",
                            smartRemindersEnabled = smartRemindersEnabled,
                            createdAt = System.currentTimeMillis()
                        )
                        viewModel.saveCategory(category = category, onSaved = onSave)
                    }
                ) {
                    Text(
                        text = if (isSaving) "Saving..." else "Save",
                        color = AccentOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            }

            saveError?.let { error ->
                Text(
                    text = error,
                    color = Color(0xFFB3261E),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Text(
                "LIVE PREVIEW",
                fontSize = 12.sp,
                color = themedColor(Color(0xFF516A8B), Color(0xFFA7B4C7)),
                letterSpacing = 3.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 22.dp)
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                shape = RoundedCornerShape(22.dp),
                color = EditCard,
                border = BorderStroke(1.dp, EditPreviewBorder),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(selectedColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            categoryIcons[selectedIconIndex].icon,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = categoryName.ifBlank { "Daily Mindfulness" },
                            fontWeight = FontWeight.Bold,
                            color = Deep,
                            fontSize = 18.sp
                        )
                        Text(
                            "12 Reminders scheduled",
                            fontSize = 15.sp,
                            color = themedColor(Color(0xFF64748B), Color(0xFFAEB6C5))
                        )
                    }
                    Icon(
                        imageVector = Icons.Filled.Spa,
                        contentDescription = null,
                        tint = themedColor(Color(0xFFD1D5DB), Color(0xFF6B7280)).copy(alpha = 0.55f),
                        modifier = Modifier.size(58.dp)
                    )
                }
            }

            Text(
                "Category Name",
                fontSize = 14.sp,
                color = themedColor(Color(0xFF243B53), Color(0xFFD5DCE7)),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )
            TextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Daily Mindfulness",
                        color = themedColor(Color(0xFF9CA3AF), Color(0xFF7C8EA3))
                    )
                },
                shape = RoundedCornerShape(18.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = EditCard,
                    unfocusedContainerColor = EditCard,
                    disabledContainerColor = EditCard,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = Deep,
                    unfocusedTextColor = Deep,
                    cursorColor = AccentOrange
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            )

            Text(
                "Theme Color",
                fontSize = 14.sp,
                color = themedColor(Color(0xFF243B53), Color(0xFFD5DCE7)),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                themeColors.forEach { (color, _) ->
                    val isSelected = selectedColor == color
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 2.dp,
                                color = if (isSelected) AccentOrange else themedColor(Color.White, Color(0xFF263244)),
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 26.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Select Icon",
                    fontSize = 14.sp,
                    color = themedColor(Color(0xFF243B53), Color(0xFFD5DCE7)),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (showAllIcons) "SHOW LESS" else "VIEW ALL",
                    color = AccentOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { showAllIcons = !showAllIcons }
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (showAllIcons) 328.dp else 176.dp)
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleIconOptions) { (index, icon) ->
                    val isSelected = selectedIconIndex == index
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) EditCard else themedColor(Color(0xFFF3F4F6), Color(0xFF253143)))
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Color(0xFFF1D3C4) else Color.Transparent,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedIconIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon.icon,
                            contentDescription = null,
                            tint = if (isSelected) AccentOrange else themedColor(Color(0xFF94A3B8), Color(0xFF94A3B8)),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp),
                shape = RoundedCornerShape(24.dp),
                color = themedColor(Color(0xFFFFF6F0), Color(0xFF1F2937)),
                border = BorderStroke(1.dp, EditPreviewBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(themedColor(Color(0xFFF7DFCF), Color(0xFF2A3548))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Alarm,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Smart Reminders",
                            fontWeight = FontWeight.Bold,
                            color = Deep,
                            fontSize = 16.sp
                        )
                        Text(
                            "Optimized alerts for this category",
                            fontSize = 12.sp,
                            color = themedColor(Color(0xFF64748B), Color(0xFFAEB6C5))
                        )
                    }
                    Switch(
                        checked = smartRemindersEnabled,
                        onCheckedChange = { smartRemindersEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentOrange,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = themedColor(Color(0xFFD7DEE7), Color(0xFF475569)),
                            checkedBorderColor = Color.Transparent,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            if (normalizedCategoryId != null) {
                Button(
                    onClick = { showDeleteConfirm = true },
                    enabled = !isSaving && !isDeleting,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 18.dp)
                        .height(52.dp)
                ) {
                    Text(
                        text = if (isDeleting) "Deleting..." else "Delete Category",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    if (showDeleteConfirm && normalizedCategoryId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = "Delete category?",
                    fontWeight = FontWeight.Bold,
                    color = Deep
                )
            },
            text = {
                Text(
                    text = "This action is safe but irreversible. Related goals may lose this category label.",
                    color = themedColor(Color(0xFF64748B), Color(0xFFAEB6C5))
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteCategory(normalizedCategoryId) {
                            onSave()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = Deep)
                }
            }
        )
    }
}

private fun parseCategoryColor(hex: String): Color {
    val cleaned = hex.trim().removePrefix("#")
    val colorLong = when (cleaned.length) {
        6 -> ("FF$cleaned").toLongOrNull(16)
        8 -> cleaned.toLongOrNull(16)
        else -> null
    } ?: return LightOrange
    return Color(colorLong)
}

private fun Color.toHexRgb(): String {
    val r = (red * 255).roundToInt().coerceIn(0, 255)
    val g = (green * 255).roundToInt().coerceIn(0, 255)
    val b = (blue * 255).roundToInt().coerceIn(0, 255)
    return "#%02X%02X%02X".format(r, g, b)
}

