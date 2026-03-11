package com.aliumitalgan.remindup.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aliumitalgan.remindup.ui.theme.*
import com.aliumitalgan.remindup.R

@Composable
fun GoalCard(
    goalTitle: String,
    goalProgress: Int,
    modifier: Modifier = Modifier,
    onProgressUpdate: ((Int) -> Unit)? = null
) {
    // Compute vibrant colors based on progress
    val progressColor = when {
        goalProgress >= 100 -> SuccessGreen
        goalProgress >= 75 -> GreenSecondary
        goalProgress >= 50 -> BluePrimary
        goalProgress >= 25 -> AccentAmber
        else -> AccentOrange
    }

    // Animated values
    val animatedProgress = animateFloatAsState(
        targetValue = goalProgress / 100f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "progress"
    )

    val isCompleted = goalProgress >= 100
    var showUpdateDialog by remember { mutableStateOf(false) }

    // Enhanced card with shadow and gradient highlight
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = progressColor.copy(alpha = 0.2f)
            )
            .clickable { showUpdateDialog = true },
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
                            progressColor.copy(alpha = 0.8f),
                            progressColor.copy(alpha = 0.4f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            // Header with title and progress display
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side: Icon and title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Colorful icon with shadow
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(
                                elevation = 3.dp,
                                shape = CircleShape,
                                spotColor = progressColor.copy(alpha = 0.2f)
                            )
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        progressColor.copy(alpha = 0.2f),
                                        progressColor.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Flag,
                            contentDescription = null,
                            tint = progressColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = goalTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Status text with icon
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                Text(
                                    text = stringResource(R.string.completed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Medium
                                )
                            } else {
                                Text(
                                    text = "${goalProgress}% ${stringResource(R.string.in_progress)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = progressColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Right side: Progress circle
                CircularProgressDisplay(
                    progress = goalProgress,
                    progressColor = progressColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Enhanced progress bar with gradient fill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.value)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(5.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    progressColor,
                                    progressColor.copy(alpha = 0.7f)
                                )
                            )
                        )
                )
            }

            // Only show update button if callback provided
            if (onProgressUpdate != null) {
                Spacer(modifier = Modifier.height(16.dp))

                // Update button with animation
                Button(
                    onClick = { showUpdateDialog = true },
                    modifier = Modifier
                        .align(Alignment.End)
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = progressColor.copy(alpha = 0.2f)
                        ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = progressColor,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.Edit else Icons.Default.Update,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = stringResource(if (isCompleted) R.string.edit else R.string.advance),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Progress update dialog
    if (showUpdateDialog && onProgressUpdate != null) {
        ProgressUpdateDialog(
            initialProgress = goalProgress,
            onDismiss = { showUpdateDialog = false },
            onConfirm = {
                onProgressUpdate(it)
                showUpdateDialog = false
            },
            accentColor = progressColor
        )
    }
}

@Composable
fun CircularProgressDisplay(
    progress: Int,
    progressColor: Color,
    size: Dp = 56.dp,
    strokeWidth: Dp = 4.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape,
                spotColor = progressColor.copy(alpha = 0.15f)
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        // Background circle
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(size),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            strokeWidth = strokeWidth
        )

        // Progress circle
        CircularProgressIndicator(
            progress = { progress / 100f },
            modifier = Modifier.size(size),
            color = progressColor,
            strokeWidth = strokeWidth
        )

        // Center text
        Text(
            text = "$progress%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = progressColor
        )
    }
}

@Composable
fun ProgressUpdateDialog(
    initialProgress: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    accentColor: Color
) {
    var progress by remember { mutableIntStateOf(initialProgress) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = accentColor
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = stringResource(R.string.edit_progress),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Current progress info
                Text(
                    text = stringResource(R.string.current_progress, initialProgress),
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)

                // Progress value display with animation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated progress display
                    CircularProgressDisplay(
                        progress = progress,
                        progressColor = accentColor,
                        size = 120.dp,
                        strokeWidth = 8.dp
                    )
                }

                // Slider
                Slider(
                    value = progress.toFloat(),
                    onValueChange = { progress = it.toInt() },
                    valueRange = 0f..100f,
                    steps = 20,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                // Slider labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "0%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "100%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Completed notice
                if (progress == 100) {
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Celebration,
                                contentDescription = null,
                                tint = SuccessGreen
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = stringResource(R.string.completing_goal),
                                color = SuccessGreen,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(progress) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.update), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}