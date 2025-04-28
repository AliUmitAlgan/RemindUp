package com.aliumitalgan.remindup.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.RemindUpTheme

@Composable
fun MotivationalMessage(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        ListItem(
            headlineContent = {
                Text(
                    text = message,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Motivasyon",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MotivationalMessagePreview() {
    RemindUpTheme {
        MotivationalMessage(message = "Her gün bir adım, hedefine bir adım daha yaklaşmak demektir!")
    }
}