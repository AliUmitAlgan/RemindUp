package com.aliumitalgan.remindup.utils

import android.content.Intent
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

data class GoalCelebrationNavigationPayload(
    val goalId: String,
    val goalTitle: String,
    val bonusXp: Int
)

object NotificationNavigationState {
    private val _goalCelebrationPayload = mutableStateOf<GoalCelebrationNavigationPayload?>(null)
    val goalCelebrationPayload: State<GoalCelebrationNavigationPayload?> = _goalCelebrationPayload

    fun updateFromIntent(intent: Intent?) {
        if (intent == null) return

        val target = intent.getStringExtra(NotificationUtils.EXTRA_NAV_TARGET)
        if (target != NotificationUtils.NAV_TARGET_GOAL_CELEBRATION) return

        val goalId = intent.getStringExtra(NotificationUtils.EXTRA_GOAL_ID).orEmpty()
        val goalTitle = intent.getStringExtra(NotificationUtils.EXTRA_GOAL_TITLE).orEmpty()
        val bonusXp = intent.getIntExtra(NotificationUtils.EXTRA_BONUS_XP, 25)

        if (goalId.isBlank()) return

        _goalCelebrationPayload.value = GoalCelebrationNavigationPayload(
            goalId = goalId,
            goalTitle = goalTitle.ifBlank { "Goal" },
            bonusXp = bonusXp
        )
    }

    fun consumeGoalCelebrationPayload() {
        _goalCelebrationPayload.value = null
    }
}
