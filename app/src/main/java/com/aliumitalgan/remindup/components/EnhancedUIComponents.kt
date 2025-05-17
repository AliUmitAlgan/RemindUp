package com.aliumitalgan.remindup.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.AppDimensions

/**
 * Modern, dikkat çekici başlık kartı.
 * Ana sayfa ve diğer önemli ekranlarda kullanılabilir.
 */
@Composable
fun HeaderCard(
    title: String,
    subtitle: String,
    icon: ImageVector? = null,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    content: @Composable () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppDimensions.spacingMD)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.15f),
                            secondaryColor.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppDimensions.spacingLG),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (icon != null) {
                    // Animate the icon for visual appeal
                    val scale = remember { Animatable(0.8f) }

                    LaunchedEffect(Unit) {
                        scale.animateTo(
                            targetValue = 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier
                                .size(36.dp)
                                .padding(AppDimensions.spacingXS)
                        )
                    }

                    Spacer(modifier = Modifier.height(AppDimensions.spacingMD))
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(AppDimensions.spacingMD))

                content()
            }
        }
    }
}

/**
 * Modern gölgeli görsel kartlar için gelişmiş version.
 * Çeşitli ekranlarda kullanılabilir.
 */
@Composable
fun EnhancedCard(
    modifier: Modifier = Modifier,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    elevation: Dp = 4.dp,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(cornerRadius),
                spotColor = primaryColor.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        content()
    }
}

/**
 * Modern metrik kartı - istatistikler göstermek için ideal.
 */
@Composable
fun StatisticCard(
    title: String,
    value: String,
    icon: ImageVector,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    animateOnAppear: Boolean = true,
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(!animateOnAppear) }

    LaunchedEffect(Unit) {
        if (animateOnAppear) {
            visible = true
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = fadeOut() + shrinkVertically()
    ) {
        EnhancedCard(
            modifier = modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(vertical = AppDimensions.spacingXS),
            primaryColor = primaryColor,
            cornerRadius = 20.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppDimensions.spacingMD),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(primaryColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(AppDimensions.spacingMD))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(AppDimensions.spacingXXS))

                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

/**
 * Gelişmiş buton - özel şekil, renk ve animasyonla.
 */
@Composable
fun EnhancedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    cornerRadius: Dp = 12.dp,
    elevationEnabled: Boolean = true
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = if (elevationEnabled) {
            ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        } else {
            null
        },
        modifier = modifier
            .height(48.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(AppDimensions.spacingXS))
        }

        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

/**
 * Gelişmiş çizgi - dekoratif ayırıcı.
 */
@Composable
fun EnhancedDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    thickness: Dp = 1.dp,
    startIndent: Dp = 0.dp,
    endIndent: Dp = 0.dp
) {
    Divider(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = startIndent, end = endIndent),
        color = color,
        thickness = thickness
    )
}

/**
 * Başlık ve alt başlık gösterimi için bileşen.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionButton: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppDimensions.spacingXS)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            if (actionButton != null) {
                actionButton()
            }
        }

        if (subtitle != null) {
            Spacer(modifier = Modifier.height(AppDimensions.spacingXXS))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/**
 * Progress göstergeli kart.
 */
@Composable
fun ProgressCard(
    title: String,
    progress: Float,
    modifier: Modifier = Modifier,
    maxProgress: Float = 1f,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    animationDuration: Int = 1000,
    showPercentage: Boolean = true
) {
    // Animate progress
    val animatedProgress = animateFloatAsState(
        targetValue = progress / maxProgress,
        animationSpec = tween(durationMillis = animationDuration, easing = FastOutSlowInEasing),
        label = "progress"
    )

    // Determine color based on progress
    val progressColor = when {
        progress >= maxProgress -> secondaryColor
        progress >= maxProgress * 0.7f -> primaryColor
        progress >= maxProgress * 0.4f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    EnhancedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppDimensions.spacingXS),
        primaryColor = progressColor,
        cornerRadius = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppDimensions.spacingMD)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (showPercentage) {
                    Text(
                        text = "${(progress / maxProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = progressColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.spacingMD))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress.value)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(6.dp))
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
        }
    }
}

/**
 * Gelişmiş chip bileşeni - çeşitli ekranlarda filtreleme için kullanılabilir.
 */
@Composable
fun EnhancedChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val backgroundColor = if (selected) {
        selectedColor
    } else {
        unselectedColor.copy(alpha = 0.5f)
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    }

    Surface(
        modifier = modifier
            .padding(end = AppDimensions.spacingXS)
            .height(32.dp),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppDimensions.spacingSM),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(AppDimensions.spacingXXS))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

/**
 * Yükselen buton, özel animasyonlarla.
 */
@Composable
fun AnimatedFloatingActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    elevation: Dp = 6.dp
) {
    // Button appearance animation
    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "scale"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    // Subtle pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseScale"
    )

    Box(
        modifier = modifier
            .scale(scale * pulseScale)
            .shadow(
                elevation = elevation,
                shape = CircleShape,
                spotColor = backgroundColor.copy(alpha = 0.3f)
            )
    ) {
        FloatingActionButton(
            onClick = onClick,
            shape = CircleShape,
            containerColor = backgroundColor,
            contentColor = contentColor
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}