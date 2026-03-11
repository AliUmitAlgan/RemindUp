package com.aliumitalgan.remindup.screens

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.aliumitalgan.remindup.domain.model.GoalCategory
import com.aliumitalgan.remindup.ui.theme.themedColor
import com.aliumitalgan.remindup.utils.GoalCategoryUtils
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.roundToInt

private val LightBg: Color
    get() = themedColor(Color(0xFFFDFBF9), Color(0xFF0F131A))
private val AccentOrange = Color(0xFFF26522)
private val Deep: Color
    get() = themedColor(Color(0xFF1A1A1A), Color(0xFFE5E7EB))
private val LightOrange = Color(0xFFFFF3E8)
private val LightMint = Color(0xFFE8F5E9)
private val LightLavender = Color(0xFFF3E8FF)
private val LightBlue = Color(0xFFE8F4FD)
private val LightYellow = Color(0xFFFFF8E1)

private val themeColors = listOf(
    AccentOrange to "Orange",
    LightMint to "Mint",
    LightLavender to "Lavender",
    LightBlue to "Blue",
    LightYellow to "Yellow"
)

private data class CategoryIconOption(
    val key: String,
    val icon: ImageVector
)

private val categoryIcons = listOf(
    CategoryIconOption("spa", Icons.Filled.Spa),
    CategoryIconOption("fitness_center", Icons.Filled.FitnessCenter),
    CategoryIconOption("bookmark", Icons.Filled.Bookmark),
    CategoryIconOption("nightlight", Icons.Filled.Nightlight),
    CategoryIconOption("coffee", Icons.Filled.Coffee),
    CategoryIconOption("work", Icons.Filled.Work),
    CategoryIconOption("alarm", Icons.Filled.Alarm),
    CategoryIconOption("pets", Icons.Filled.Pets),
    CategoryIconOption("self_care", Icons.Filled.SelfImprovement),
    CategoryIconOption("local_dining", Icons.Filled.LocalDining),
    CategoryIconOption("directions_run", Icons.AutoMirrored.Filled.DirectionsRun),
    CategoryIconOption("school", Icons.Filled.School),
    CategoryIconOption("movie", Icons.Filled.Movie),
    CategoryIconOption("shopping_cart", Icons.Filled.ShoppingCart),
    CategoryIconOption("favorite", Icons.Filled.Favorite)
)

@Composable
fun EditCategoryScreen(
    categoryId: String?,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val normalizedCategoryId = categoryId?.takeUnless { it.equals("new", ignoreCase = true) }
    var categoryName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(LightOrange) }
    var selectedIconIndex by remember { mutableStateOf(0) }
    var smartRemindersEnabled by remember { mutableStateOf(true) }
    var showAllIcons by remember { mutableStateOf(false) }
    var currentRoute by remember { mutableStateOf("categories") }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val navItems = mainBottomNavItems()
    val visibleIconOptions = if (showAllIcons) {
        categoryIcons.mapIndexed { index, icon -> index to icon }
    } else {
        categoryIcons.take(8).mapIndexed { index, icon -> index to icon }
    }

    LaunchedEffect(normalizedCategoryId) {
        if (normalizedCategoryId.isNullOrBlank()) return@LaunchedEffect
        GoalCategoryUtils.getGoalCategoryById(normalizedCategoryId)
            .onSuccess { category ->
                if (category != null) {
                    categoryName = category.name
                    selectedColor = parseCategoryColor(category.colorHex)
                    selectedIconIndex = categoryIcons.indexOfFirst { it.key == category.iconKey }
                        .takeIf { it >= 0 } ?: 0
                    smartRemindersEnabled = category.smartRemindersEnabled
                }
            }
    }

    Scaffold(
        containerColor = LightBg,
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                currentRoute = currentRoute,
                onItemSelected = { route ->
                    currentRoute = route
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
                .padding(horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Deep)
                }
                Text(
                    text = if (normalizedCategoryId == null) "New Category" else "Edit Category",
                    fontWeight = FontWeight.Bold,
                    color = Deep,
                    fontSize = 18.sp
                )
                TextButton(
                    enabled = !isSaving && categoryName.isNotBlank(),
                    onClick = {
                        val trimmedName = categoryName.trim()
                        if (trimmedName.isBlank()) {
                            saveError = "Category name is required."
                            return@TextButton
                        }
                        isSaving = true
                        saveError = null
                        scope.launch {
                            val category = GoalCategory(
                                id = normalizedCategoryId ?: UUID.randomUUID().toString(),
                                name = trimmedName,
                                colorHex = selectedColor.toHexRgb(),
                                iconKey = categoryIcons.getOrNull(selectedIconIndex)?.key ?: "self_care",
                                smartRemindersEnabled = smartRemindersEnabled,
                                createdAt = System.currentTimeMillis()
                            )
                            GoalCategoryUtils.saveGoalCategory(category)
                                .onSuccess {
                                    isSaving = false
                                    onSave()
                                }
                                .onFailure { error ->
                                    isSaving = false
                                    saveError = error.message ?: "Failed to save category."
                                }
                        }
                    }
                ) {
                    Text(
                        text = if (isSaving) "Saving..." else "Save",
                        color = AccentOrange,
                        fontWeight = FontWeight.SemiBold
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
                color = Color(0xFF9CA3AF),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 10.dp)
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = LightOrange
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(selectedColor.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            categoryIcons[selectedIconIndex].icon,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = categoryName.ifBlank { "Category Name" },
                            fontWeight = FontWeight.Bold,
                            color = Deep,
                            fontSize = 16.sp
                        )
                        Text("12 Reminders scheduled", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                }
            }

            Text(
                "Category Name",
                fontSize = 14.sp,
                color = Deep,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 24.dp)
            )
            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )

            Text(
                "Theme Color",
                fontSize = 14.sp,
                color = Deep,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 24.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                themeColors.forEach { (color, _) ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (selectedColor == color) Modifier.border(2.dp, AccentOrange, CircleShape)
                                else Modifier
                            )
                            .clickable { selectedColor = color },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == color) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp), tint = AccentOrange)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Select Icon",
                    fontSize = 14.sp,
                    color = Deep,
                    fontWeight = FontWeight.Medium
                )
                TextButton(onClick = { showAllIcons = !showAllIcons }) {
                    Text(
                        text = if (showAllIcons) "SHOW LESS" else "VIEW ALL",
                        color = AccentOrange,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (showAllIcons) 320.dp else 180.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(visibleIconOptions) { (index, icon) ->
                    val isSelected = selectedIconIndex == index
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) LightOrange else Color(0xFFF5F5F5)
                            )
                            .then(
                                if (isSelected) Modifier.border(2.dp, AccentOrange, RoundedCornerShape(12.dp))
                                else Modifier
                            )
                            .clickable { selectedIconIndex = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon.icon,
                            contentDescription = null,
                            tint = if (isSelected) AccentOrange else Color(0xFF6B7280),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF5F5F5)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(LightOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Alarm, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smart Reminders", fontWeight = FontWeight.SemiBold, color = Deep, fontSize = 15.sp)
                        Text("Optimized alerts for this category", fontSize = 12.sp, color = Color(0xFF6B7280))
                    }
                    Switch(
                        checked = smartRemindersEnabled,
                        onCheckedChange = { smartRemindersEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = AccentOrange
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
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

