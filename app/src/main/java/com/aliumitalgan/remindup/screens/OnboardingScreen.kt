package com.aliumitalgan.remindup.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.aliumitalgan.remindup.ui.theme.themedColor
import com.aliumitalgan.remindup.utils.OnboardingPreferences
import kotlinx.coroutines.launch

private val AccentOrange = Color(0xFFF26522)
private val OnboardingBg: Color
    get() = themedColor(Color.White, Color(0xFF0F131A))
private val TitleText: Color
    get() = themedColor(Color(0xFF1E2A43), Color(0xFFE5E7EB))
private val SubtitleText: Color
    get() = themedColor(Color(0xFF7C889E), Color(0xFFAEB6C5))
private val NeutralIndicator: Color
    get() = themedColor(Color(0xFFD7DEE9), Color(0xFF344155))
private val TopBackBg: Color
    get() = themedColor(Color(0xFFEFF2F6), Color(0xFF1F2937))
private val TopBackTint: Color
    get() = themedColor(Color(0xFF687488), Color(0xFFE5E7EB))
private val SoftMutedText: Color
    get() = themedColor(Color(0xFFA5AFC0), Color(0xFFAEB6C5))

private data class IntroPage(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val cardColors: List<Color>
)

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    val pages = listOf(
        IntroPage(
            title = "Sweet Productivity",
            subtitle = "Experience a more mindful way to manage your time and reach your goals with ease.",
            icon = Icons.Filled.Schedule,
            cardColors = listOf(Color(0xFF02121E), Color(0xFF142833))
        ),
        IntroPage(
            title = "Grow Your Habits",
            subtitle = "Track your routines daily and watch your progress flourish with intuitive habit building tools.",
            icon = Icons.Filled.AutoGraph,
            cardColors = listOf(Color(0xFF0A2018), Color(0xFF113425))
        ),
        IntroPage(
            title = "Celebrate Success",
            subtitle = "Acknowledge every milestone and celebrate your small victories along the way.",
            icon = Icons.Filled.EmojiEvents,
            cardColors = listOf(Color(0xFFF3F4F6), Color(0xFFE5E7EB))
        )
    )

    fun finishOnboarding() {
        OnboardingPreferences.setOnboardingCompleted(context)
        onComplete()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OnboardingBg)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        TopHeader(
            page = pagerState.currentPage,
            onBack = {
                if (pagerState.currentPage > 0) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                }
            },
            onSkip = { finishOnboarding() }
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            val data = pages[page]
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(236.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(colors = data.cardColors)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = null,
                        tint = if (page == 2) AccentOrange else Color(0xFFFFB88C),
                        modifier = Modifier.size(96.dp)
                    )

                    if (page == 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 10.dp, end = 8.dp)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD8C4))
                        )
                    }

                    if (page == 1) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Daily Streak", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(34.dp))

                Text(
                    text = data.title,
                    color = TitleText,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 44.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 48.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = data.subtitle,
                    color = SubtitleText,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 23.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(
                            width = if (index == pagerState.currentPage) 24.dp else 6.dp,
                            height = 6.dp
                        )
                        .clip(RoundedCornerShape(100))
                        .background(
                            if (index == pagerState.currentPage) AccentOrange
                            else NeutralIndicator
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                when (pagerState.currentPage) {
                    0, 1 -> scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    else -> finishOnboarding()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
        ) {
            Text(
                text = when (pagerState.currentPage) {
                    0 -> "Get Started"
                    1 -> "Next  →"
                    else -> "Let's Go!  →"
                },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        }

        if (pagerState.currentPage == 1) {
            TextButton(
                onClick = { finishOnboarding() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            ) {
                Text(
                    text = "Skip for now",
                    color = SoftMutedText,
                    fontSize = 13.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun TopHeader(
    page: Int,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (page > 0) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(TopBackBg)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TopBackTint
                )
            }
        }

        when (page) {
            0 -> {
                Text(
                    text = "RemindUp",
                    modifier = Modifier.align(Alignment.Center),
                    color = TitleText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Text("Skip", color = SoftMutedText, fontSize = 12.sp)
                }
            }
            1 -> {
                Text(
                    text = "RemindUp",
                    modifier = Modifier.align(Alignment.Center),
                    color = AccentOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            else -> {
                Text(
                    text = "3 / 3",
                    modifier = Modifier.align(Alignment.Center),
                    color = SoftMutedText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
