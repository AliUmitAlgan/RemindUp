package com.aliumitalgan.remindup.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object AnimationUtils {

    // Fade in/out animasyonu
    @Composable
    fun FadeAnimation(
        visible: Boolean,
        content: @Composable () -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500))
        ) {
            content()
        }
    }

    // Slide animasyonu
    @Composable
    fun SlideAnimation(
        visible: Boolean,
        content: @Composable () -> Unit
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            content()
        }
    }

    // İlerleme animasyonu
    @Composable
    fun ProgressAnimation(
        targetValue: Float,
        durationMillis: Int = 1000,
        content: @Composable (animatedValue: Float) -> Unit
    ) {
        val animatedProgress = animateFloatAsState(
            targetValue = targetValue,
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            label = "progress"
        )

        content(animatedProgress.value)
    }

    // Pulse/Atma animasyonu (örn. hedef tamamlandığında)
    @Composable
    fun PulseAnimation(
        modifier: Modifier = Modifier,
        pulseFraction: Float = 1.2f,
        content: @Composable (scale: Float) -> Unit
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale = infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = pulseFraction,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        content(scale.value)
    }

    // Hedef tamamlandı animasyonu
    @Composable
    fun GoalCompletedAnimation(
        isCompleted: Boolean,
        content: @Composable () -> Unit
    ) {
        AnimatedVisibility(
            visible = isCompleted,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn()
        ) {
            content()
        }
    }
}