package com.aliumitalgan.remindup.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.themedColor
import com.aliumitalgan.remindup.utils.ThemeManager
import com.aliumitalgan.remindup.utils.UserPreferenceUtils
import kotlinx.coroutines.launch

private val ScreenBg: Color
    get() = themedColor(Color(0xFFF8F6F6), Color(0xFF0D1117))
private val Primary = Color(0xFFEC5B13)
private val Deep: Color
    get() = themedColor(Color(0xFF1F2937), Color(0xFFE5E7EB))

@Composable
fun AppearanceScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isDarkMode = isSystemInDarkTheme()
    val persistedDynamicAccents by ThemeManager.dynamicAccentsEnabled
    val scope = rememberCoroutineScope()
    var selectedTheme by remember { mutableStateOf("Peach") }
    var cornerRoundness by remember { mutableFloatStateOf(12f) }
    var dynamicAccents by remember { mutableStateOf(persistedDynamicAccents) }
    var isLoadingPreferences by remember { mutableStateOf(true) }
    var isSavingDynamicAccents by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ThemeManager.loadDynamicAccentsState(context)
        dynamicAccents = ThemeManager.dynamicAccentsEnabled.value

        UserPreferenceUtils.getAppearancePreferences()
            .onSuccess { prefs ->
                prefs.dynamicAccentsEnabled?.let { remoteDynamicAccents ->
                    ThemeManager.saveDynamicAccentsState(context, remoteDynamicAccents)
                    dynamicAccents = remoteDynamicAccents
                }
            }
            .onFailure {
                // Local fallback is already loaded above.
            }

        isLoadingPreferences = false
    }

    fun updateDynamicAccents(nextValue: Boolean) {
        if (isSavingDynamicAccents || nextValue == dynamicAccents) return

        val previous = dynamicAccents
        dynamicAccents = nextValue
        ThemeManager.saveDynamicAccentsState(context, nextValue)
        isSavingDynamicAccents = true

        scope.launch {
            UserPreferenceUtils.updateAppearancePreference("dynamicAccentsEnabled", nextValue)
                .onFailure {
                    dynamicAccents = previous
                    ThemeManager.saveDynamicAccentsState(context, previous)
                }
            isSavingDynamicAccents = false
        }
    }

    Scaffold(
        containerColor = ScreenBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                color = themedColor(
                    Color.White.copy(alpha = 0.82f),
                    Color(0xFF121820).copy(alpha = 0.92f)
                ),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 16.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF1E6))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Primary
                        )
                    }

                    Text(
                        text = "Appearance",
                        color = Deep,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                SectionHeader(
                    icon = Icons.Filled.Visibility,
                    title = "Display Mode"
                )

                if (isLoadingPreferences) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DisplayModeCard(
                        title = "Light Mode",
                        icon = Icons.Filled.LightMode,
                        selected = !isDarkMode,
                        isDarkPreview = false,
                        iconTint = Primary,
                        modifier = Modifier.weight(1f),
                        enabled = false,
                        onClick = {}
                    )
                    DisplayModeCard(
                        title = "Dark Mode",
                        icon = Icons.Filled.DarkMode,
                        selected = isDarkMode,
                        isDarkPreview = true,
                        iconTint = Color(0xFF94A3B8),
                        modifier = Modifier.weight(1f),
                        enabled = false,
                        onClick = {}
                    )
                }

                Text(
                    text = "Theme follows your phone setting automatically.",
                    color = themedColor(Color(0xFF64748B), Color(0xFFAEB6C5)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Surface(
                color = themedColor(
                    Color(0xFFF1F5F9).copy(alpha = 0.55f),
                    Color(0xFF111827).copy(alpha = 0.75f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SectionHeader(
                        icon = Icons.Filled.ColorLens,
                        title = "Custom Themes"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ThemeSwatch("Peach", Color(0xFFFB923C), Color(0xFFFFEDD5), selectedTheme == "Peach") { selectedTheme = "Peach" }
                        ThemeSwatch("Mint", Color(0xFF34D399), Color(0xFFD1FAE5), selectedTheme == "Mint") { selectedTheme = "Mint" }
                        ThemeSwatch("Lavender", Color(0xFFC084FC), Color(0xFFF3E8FF), selectedTheme == "Lavender") { selectedTheme = "Lavender" }
                        ThemeSwatch("Sky", Color(0xFF38BDF8), Color(0xFFE0F2FE), selectedTheme == "Sky") { selectedTheme = "Sky" }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        icon = Icons.Filled.RoundedCorner,
                        title = "Corner Roundness",
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Primary.copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "${cornerRoundness.toInt()}px",
                            color = Primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Surface(
                    color = themedColor(Color.White, Color(0xFF1A202C)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Slider(
                            value = cornerRoundness,
                            onValueChange = { cornerRoundness = it },
                            valueRange = 0f..24f,
                            colors = SliderDefaults.colors(
                                thumbColor = Primary,
                                activeTrackColor = Color(0xFFDCE6F2),
                                inactiveTrackColor = Color(0xFFE5E7EB)
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sharp", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Text("Standard", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            Text("Bubbly", color = Color(0xFF9CA3AF), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                    }
                }

                Surface(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, Color(0xFFD1D5DB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(cornerRoundness.dp.coerceIn(10.dp, 24.dp)))
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Preview Element", color = Deep, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("See how corners change", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                        Surface(
                            shape = RoundedCornerShape(cornerRoundness.dp.coerceIn(10.dp, 18.dp)),
                            color = Primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Button",
                                color = Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Surface(
                    color = themedColor(Color.White, Color(0xFF1A202C)),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0E7FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFF6366F1)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dynamic Accents", color = Deep, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Match UI to wallpaper", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                        Switch(
                            checked = dynamicAccents,
                            onCheckedChange = { updateDynamicAccents(it) },
                            enabled = !isLoadingPreferences && !isSavingDynamicAccents,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Primary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }
            }

            Surface(
                color = themedColor(
                    Color.White.copy(alpha = 0.82f),
                    Color(0xFF121820).copy(alpha = 0.92f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomPreviewItem("Tasks", false)
                    BottomPreviewItem("Calendar", false)
                    BottomPreviewItem("Focus", false)
                    BottomPreviewItem("Settings", true)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Deep,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
private fun DisplayModeCard(
    title: String,
    icon: ImageVector,
    selected: Boolean,
    isDarkPreview: Boolean,
    iconTint: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (isDarkPreview) Color(0xFF1F2937) else Color.White,
            border = BorderStroke(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) Primary else Color(0xFFE2E8F0)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.74f)
                    .background(if (isDarkPreview) Color(0xFF111827) else Color(0xFFF8FAFC))
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(100))
                            .background(if (isDarkPreview) Color(0xFF6B7280) else Color(0xFFE2E8F0))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(100))
                            .background(if (isDarkPreview) Color(0xFF4B5563) else Color.White)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(100))
                            .background(if (isDarkPreview) Color(0xFF4B5563) else Color.White)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDarkPreview) Color(0xFF0F172A) else Primary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            color = if (selected) Deep else Color(0xFF6B7280),
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ThemeSwatch(
    name: String,
    color: Color,
    background: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(background)
                .border(4.dp, color.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color)
                    .alpha(if (selected) 1f else 0.92f)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = name,
            color = if (selected) Deep else Color(0xFF6B7280),
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun BottomPreviewItem(
    label: String,
    selected: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (selected) Primary.copy(alpha = 0.14f) else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (selected) Primary else Color(0xFF94A3B8))
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (selected) Primary else Color(0xFF94A3B8),
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )
    }
}
