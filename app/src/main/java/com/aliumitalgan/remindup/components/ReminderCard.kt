package com.aliumitalgan.remindup.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.models.Reminder
import com.aliumitalgan.remindup.models.ReminderCategory
import com.aliumitalgan.remindup.models.ReminderType
import com.aliumitalgan.remindup.ui.theme.AccentPink
import com.aliumitalgan.remindup.ui.theme.AccentPurple
import com.aliumitalgan.remindup.ui.theme.AccentTeal
import com.aliumitalgan.remindup.ui.theme.BluePrimary
import com.aliumitalgan.remindup.ui.theme.ErrorRed
import com.aliumitalgan.remindup.ui.theme.GreenSecondary
import kotlinx.coroutines.delay

@Composable
fun ReminderCard(
    reminder: Reminder,
    onToggleEnabled: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    // Compute category colors
    val categoryColor = when(reminder.category) {
        ReminderCategory.GENERAL -> BluePrimary
        ReminderCategory.WORK -> AccentPink
        ReminderCategory.HEALTH -> ErrorRed
        ReminderCategory.PERSONAL -> AccentPurple
        ReminderCategory.STUDY -> AccentTeal
        ReminderCategory.FITNESS -> GreenSecondary
    }

    // Animation states
    val cardElevation = animateDpAsState(
        targetValue = if (expanded) 8.dp else 4.dp,
        label = "cardElevation"
    )

    val iconScale = animateFloatAsState(
        targetValue = if (reminder.isEnabled) 1f else 0.8f,
        label = "iconScale"
    )

    // Enhanced card with shadow and gradient accent
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = cardElevation.value,
                shape = RoundedCornerShape(20.dp),
                spotColor = categoryColor.copy(alpha = 0.2f)
            )
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        // Add a subtle gradient accent at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            categoryColor.copy(alpha = 0.8f),
                            categoryColor.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time badge with category color
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = categoryColor.copy(alpha = 0.15f)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    categoryColor.copy(alpha = 0.2f),
                                    categoryColor.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Time display
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = reminder.time,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor
                        )

                        // Show small icon for repeat type if not single
                        if (reminder.type != ReminderType.SINGLE) {
                            Icon(
                                imageVector = when(reminder.type) {
                                    ReminderType.DAILY -> Icons.Default.Today
                                    ReminderType.WEEKLY -> Icons.Default.DateRange
                                    ReminderType.MONTHLY -> Icons.Default.Event
                                    else -> Icons.Default.Alarm
                                },
                                contentDescription = null,
                                tint = categoryColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Title and category
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Title with importance indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (reminder.isImportant) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = "Önemli",
                                tint = ErrorRed,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp)
                            )
                        }

                        Text(
                            text = reminder.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (reminder.isEnabled)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Category badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Surface(
                            color = categoryColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = when(reminder.category) {
                                        ReminderCategory.GENERAL -> Icons.Default.Notifications
                                        ReminderCategory.WORK -> Icons.Default.Work
                                        ReminderCategory.HEALTH -> Icons.Default.Favorite
                                        ReminderCategory.PERSONAL -> Icons.Default.Person
                                        ReminderCategory.STUDY -> Icons.Default.School
                                        ReminderCategory.FITNESS -> Icons.Default.FitnessCenter
                                    },
                                    contentDescription = null,
                                    tint = categoryColor,
                                    modifier = Modifier.size(12.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = when(reminder.category) {
                                        ReminderCategory.GENERAL -> stringResource(R.string.category_general)
                                        ReminderCategory.WORK -> stringResource(R.string.category_work)
                                        ReminderCategory.HEALTH -> stringResource(R.string.category_health)
                                        ReminderCategory.PERSONAL -> stringResource(R.string.category_personal)
                                        ReminderCategory.STUDY -> stringResource(R.string.category_study)
                                        ReminderCategory.FITNESS -> stringResource(R.string.category_fitness)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = categoryColor
                                )
                            }
                        }

                        // Type badge
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = when(reminder.type) {
                                    ReminderType.SINGLE -> stringResource(R.string.type_single)
                                    ReminderType.DAILY -> stringResource(R.string.type_daily)
                                    ReminderType.WEEKLY -> stringResource(R.string.type_weekly)
                                    ReminderType.MONTHLY -> stringResource(R.string.type_monthly)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Enable/disable switch with animation
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    modifier = Modifier.scale(iconScale.value),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = categoryColor,
                        checkedTrackColor = categoryColor.copy(alpha = 0.2f),
                        checkedBorderColor = categoryColor.copy(alpha = 0.3f),
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Expanded content with animation
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    // Only show description if it's not empty
                    if (reminder.description.isNotEmpty()) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )

                        Text(
                            text = stringResource(R.string.description),
                            style = MaterialTheme.typography.labelMedium,
                            color = categoryColor,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = reminder.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    )

                    // Action buttons with enhanced appearance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Edit button
                        Button(
                            onClick = onEditClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = categoryColor.copy(alpha = 0.1f),
                                contentColor = categoryColor
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 2.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.edit),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Delete button
                        Button(
                            onClick = onDeleteClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ErrorRed,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.delete),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper function to get category colors
private fun getCategoryColors(category: ReminderCategory): Pair<Color, Color> {
    return when (category) {
        ReminderCategory.GENERAL -> Pair(Color(0xFF1A73E8), Color(0xFF65A2F6))  // Blue
        ReminderCategory.WORK -> Pair(Color(0xFFE91E63), Color(0xFFF06292))     // Pink
        ReminderCategory.HEALTH -> Pair(Color(0xFFE53935), Color(0xFFEF5350))   // Red
        ReminderCategory.PERSONAL -> Pair(Color(0xFF9C27B0), Color(0xFFBA68C8)) // Purple
        ReminderCategory.STUDY -> Pair(Color(0xFF00897B), Color(0xFF26A69A))    // Teal
        ReminderCategory.FITNESS -> Pair(Color(0xFF7CB342), Color(0xFF9CCC65))  // Green
    }
}

// Helper function to get category icon
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

// Helper function to get reminder type icon
private fun getTypeIcon(type: ReminderType): ImageVector {
    return when (type) {
        ReminderType.SINGLE -> Icons.Default.Alarm
        ReminderType.DAILY -> Icons.Default.Today
        ReminderType.WEEKLY -> Icons.Default.DateRange
        ReminderType.MONTHLY -> Icons.Default.Event
    }
}

@Composable
fun EmptyRemindersView(
    onAddClick: () -> Unit,
    isFiltered: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = CircleShape,
                    spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            var scaled by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (scaled) 1.1f else 0.95f,
                label = "notificationIconScale"
            )

            LaunchedEffect(Unit) {
                // Create a pulsing animation
                while (true) {
                    delay(800)
                    scaled = !scaled
                }
            }

            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(64.dp)
                    .scale(scale)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = if (isFiltered)
                "Filtrelenmiş sonuç bulunamadı"
            else
                stringResource(R.string.no_reminders),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isFiltered)
                "Lütfen farklı filtre kriterleri deneyin"
            else
                stringResource(R.string.add_first_reminder),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (!isFiltered) {
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.add_reminder),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}