package com.aliumitalgan.remindup.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.BluePrimary
import com.aliumitalgan.remindup.ui.theme.GreenSecondary
import com.aliumitalgan.remindup.ui.theme.RemindUpTheme

@Composable
fun GoalCard(goalTitle: String, goalProgress: Int) {
    val animatedProgress = animateFloatAsState(
        targetValue = goalProgress / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    val progressColor = when {
        goalProgress >= 100 -> GreenSecondary
        goalProgress >= 70 -> GreenSecondary
        goalProgress >= 30 -> BluePrimary
        else -> MaterialTheme.colorScheme.primary
    }

    val isCompleted = goalProgress >= 100

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // İkon kutusu
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    BluePrimary.copy(alpha = 0.2f),
                                    GreenSecondary.copy(alpha = 0.2f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.Timer,
                        contentDescription = "Hedef",
                        tint = if (isCompleted) GreenSecondary else BluePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = goalTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // İlerleme çubuğu
            Box(modifier = Modifier.fillMaxWidth()) {
                // Background progess bar
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                // Foreground progress bar
                LinearProgressIndicator(
                    progress = { animatedProgress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // İlerleme metni
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${goalProgress}% Tamamlandı",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Tamamlandı rozeti
                if (isCompleted) {
                    SuggestionChip(
                        onClick = { },
                        label = { Text("Tamamlandı") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = GreenSecondary.copy(alpha = 0.1f),
                            labelColor = GreenSecondary
                        ),
                        border = null
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GoalCardPreview() {
    RemindUpTheme {
        GoalCard(goalTitle = "Günde 2 litre su iç", goalProgress = 75)
    }
}