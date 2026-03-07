package com.aliumitalgan.remindup.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.models.Goal
import com.aliumitalgan.remindup.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalBottomSheet(
    onDismiss: () -> Unit,
    onSave: (Goal) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var goalTitle by remember { mutableStateOf("") }
    var goalProgress by remember { mutableStateOf(0) }
    var selectedCategory by remember { mutableStateOf(0) }
    var goalDescription by remember { mutableStateOf("") }
    var isImportant by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    // Enhanced categories with vibrant colors
    val categories = listOf("Genel", "İş", "Kişisel", "Eğitim", "Sağlık", "Spor")
    val categoryIcons = listOf(
        Icons.Default.Flag,
        Icons.Default.Work,
        Icons.Default.Person,
        Icons.Default.School,
        Icons.Default.Favorite,
        Icons.Default.FitnessCenter
    )
    // Using our vibrant colors for categories
    val categoryColors = listOf(
        BluePrimary,            // General - Blue
        AccentPink,             // Work - Pink
        AccentPurple,           // Personal - Purple
        AccentTeal,             // Education - Teal
        ErrorRed,               // Health - Red
        GreenSecondary          // Fitness - Green
    )

    // Animated progress color
    val progressColor = when {
        goalProgress >= 100 -> SuccessGreen
        goalProgress >= 75 -> GreenSecondary
        goalProgress >= 50 -> BluePrimary
        goalProgress >= 25 -> WarningOrange
        else -> BluePrimaryLight
    }

    // Animated progress
    val animatedProgress = animateFloatAsState(
        targetValue = goalProgress / 100f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "progressAnimation"
    )

    // Sheet with enhanced visual style
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomSheetDefaults.DragHandle()

                // Animated header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    categoryColors[selectedCategory],
                                    categoryColors[selectedCategory].copy(alpha = 0.7f)
                                )
                            )
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Yeni Hedef Oluştur",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Category selection with animated selection
            Text(
                text = "Kategori Seçin",
                style = MaterialTheme.typography.titleMedium,
                color = categoryColors[selectedCategory],
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )

            // Enhanced category chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp, bottom = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories.size) { index ->
                    // Animated category selection
                    val isSelected = selectedCategory == index
                    val scale = animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "categoryScale"
                    )

                    // Enhanced category chip with shadow and scale animation
                    Surface(
                        modifier = Modifier
                            .scale(scale.value)
                            .shadow(
                                elevation = if (isSelected) 8.dp else 2.dp,
                                shape = RoundedCornerShape(20.dp),
                                spotColor = categoryColors[index].copy(alpha = 0.3f)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected)
                            categoryColors[index].copy(alpha = 0.2f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) categoryColors[index] else Color.Transparent
                        ),
                        onClick = { selectedCategory = index }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Category icon with colorful background
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        color = if (isSelected)
                                            categoryColors[index].copy(alpha = 0.3f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = categoryIcons[index],
                                    contentDescription = null,
                                    tint = if (isSelected) categoryColors[index] else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = categories[index],
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) categoryColors[index] else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = categoryColors[index],
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Enhanced divider with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                categoryColors[selectedCategory].copy(alpha = 0.3f),
                                categoryColors[selectedCategory].copy(alpha = 0.1f),
                                categoryColors[selectedCategory].copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Enhanced goal title input
            Text(
                text = "Hedef Başlığı",
                style = MaterialTheme.typography.titleMedium,
                color = categoryColors[selectedCategory],
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = goalTitle,
                onValueChange = { goalTitle = it },
                placeholder = { Text("Örn: Her gün 30 dakika yürüyüş yapmak") },
                singleLine = false,
                maxLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = categoryColors[selectedCategory].copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = categoryColors[selectedCategory],
                    focusedLabelColor = categoryColors[selectedCategory],
                    cursorColor = categoryColors[selectedCategory],
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                leadingIcon = {
                    Icon(
                        imageVector = categoryIcons[selectedCategory],
                        contentDescription = null,
                        tint = categoryColors[selectedCategory]
                    )
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Enhanced description input
            Text(
                text = "Açıklama (İsteğe Bağlı)",
                style = MaterialTheme.typography.titleMedium,
                color = categoryColors[selectedCategory],
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )

            OutlinedTextField(
                value = goalDescription,
                onValueChange = { goalDescription = it },
                placeholder = { Text("Hedefle ilgili detaylar...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        spotColor = categoryColors[selectedCategory].copy(alpha = 0.1f)
                    ),
                shape = RoundedCornerShape(16.dp),
                minLines = 2,
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = categoryColors[selectedCategory],
                    focusedLabelColor = categoryColors[selectedCategory],
                    cursorColor = categoryColors[selectedCategory],
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = categoryColors[selectedCategory]
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Enhanced divider with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                categoryColors[selectedCategory].copy(alpha = 0.3f),
                                categoryColors[selectedCategory].copy(alpha = 0.1f),
                                categoryColors[selectedCategory].copy(alpha = 0.3f)
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Enhanced progress section with animation
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Başlangıç İlerlemesi: $goalProgress%",
                    style = MaterialTheme.typography.titleMedium,
                    color = progressColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Animated progress circle
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Enhanced background circle with shadow
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                spotColor = progressColor.copy(alpha = 0.2f)
                            )
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background circle
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(140.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            strokeWidth = 12.dp
                        )

                        // Progress circle with gradient brush
                        CircularProgressIndicator(
                            progress = { animatedProgress.value },
                            modifier = Modifier.size(140.dp),
                            color = progressColor,
                            strokeWidth = 12.dp
                        )

                        // Center text with dynamic color
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Animated scale for the percentage
                            val textScale = animateFloatAsState(
                                targetValue = if (goalProgress > 0) 1.1f else 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "textScale"
                            )

                            Text(
                                text = "$goalProgress%",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = progressColor,
                                modifier = Modifier.scale(textScale.value)
                            )

                            Text(
                                text = "İlerleme",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Enhanced slider with animation
                Slider(
                    value = goalProgress.toFloat(),
                    onValueChange = { goalProgress = it.toInt() },
                    valueRange = 0f..100f,
                    steps = 20,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = progressColor,
                        activeTrackColor = progressColor,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Quick progress selectors with animation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(0, 25, 50, 75, 100).forEach { level ->
                        // Animated scale for selected level
                        val bubbleScale = animateFloatAsState(
                            targetValue = if (goalProgress == level) 1.2f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "bubbleScale$level"
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { goalProgress = level }
                                .padding(4.dp)
                        ) {
                            // Enhanced progress level indicator with shadow and animation
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .scale(bubbleScale.value)
                                    .shadow(
                                        elevation = if (goalProgress >= level) 4.dp else 1.dp,
                                        shape = CircleShape,
                                        spotColor = if (goalProgress >= level) progressColor.copy(alpha = 0.3f) else Color.Transparent
                                    )
                                    .clip(CircleShape)
                                    .background(
                                        if (goalProgress >= level)
                                            progressColor.copy(alpha = 0.2f)
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    )
                                    .border(
                                        width = if (goalProgress == level) 2.dp else 1.dp,
                                        color = if (goalProgress >= level) progressColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (goalProgress >= level) {
                                    if (level == 100) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = progressColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = progressColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "$level%",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (goalProgress >= level) progressColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (goalProgress == level) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Important goal toggle with enhanced appearance
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { isImportant = !isImportant },
                    color = if (isImportant)
                        categoryColors[selectedCategory].copy(alpha = 0.1f)
                    else
                        MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isImportant) categoryColors[selectedCategory].copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isImportant,
                            onCheckedChange = { isImportant = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = categoryColors[selectedCategory],
                                uncheckedColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "Önemli Hedef",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isImportant) FontWeight.Bold else FontWeight.Normal,
                                color = if (isImportant)
                                    categoryColors[selectedCategory]
                                else
                                    MaterialTheme.colorScheme.onSurface
                            )

                            if (isImportant) {
                                Text(
                                    text = "Bu hedef diğerlerinden daha önemli olarak işaretlenecek",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Icon(
                            imageVector = Icons.Default.PriorityHigh,
                            contentDescription = null,
                            tint = if (isImportant)
                                categoryColors[selectedCategory]
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Enhanced save button with gradient background
            Button(
                onClick = {
                    if (goalTitle.isNotBlank()) {
                        isSaving = true

                        // Create goal object
                        val newGoal = Goal(
                            title = goalTitle,
                            description = goalDescription,
                            progress = goalProgress,
                            category = selectedCategory,
                            isImportant = isImportant,
                            userId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                            createdAt = System.currentTimeMillis()
                        )

                        // Save
                        coroutineScope.launch {
                            onSave(newGoal)
                            isSaving = false
                            onDismiss()
                        }
                    }
                },
                enabled = goalTitle.isNotBlank() && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = categoryColors[selectedCategory].copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = categoryColors[selectedCategory],
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Hedefi Kaydet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}