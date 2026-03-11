package com.aliumitalgan.remindup.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aliumitalgan.remindup.ui.theme.themedColor

private val CelebrationOrange = Color(0xFFF59A61)
private val CelebrationText: Color
    get() = themedColor(Color(0xFF1E293B), Color(0xFFE5E7EB))
private val CelebrationSubText: Color
    get() = themedColor(Color(0xFF52607A), Color(0xFFB5C0D3))

@Composable
fun GoalCelebrationScreen(
    goalTitle: String,
    bonusXp: Int,
    onAwesome: () -> Unit
) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            themedColor(Color(0xFFF8F2E8), Color(0xFF141B25)),
            themedColor(Color(0xFFE7F3EA), Color(0xFF0F131A))
        )
    )

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                Button(
                    onClick = onAwesome,
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CelebrationOrange,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Text(
                        text = "Awesome!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "*",
                color = Color(0xFFF6D867),
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 160.dp)
            )
            Text(
                text = "*",
                color = Color(0xFFF6D867),
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 220.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(themedColor(Color.White.copy(alpha = 0.9f), Color(0xFF1A2230))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = CelebrationOrange,
                        modifier = Modifier.size(96.dp)
                    )
                }

                Spacer(modifier = Modifier.height(34.dp))

                Text(
                    text = "Wow! Ahead of Schedule!",
                    color = CelebrationText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 50.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 56.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "You finished \"${goalTitle.ifBlank { "your goal" }}\" before the deadline. Keep this amazing momentum.",
                    color = CelebrationSubText,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 30.sp
                )

                Spacer(modifier = Modifier.height(30.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(themedColor(Color.White.copy(alpha = 0.78f), Color(0xFF1A2230)))
                        .padding(horizontal = 30.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "+$bonusXp Bonus XP",
                        color = CelebrationOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}
