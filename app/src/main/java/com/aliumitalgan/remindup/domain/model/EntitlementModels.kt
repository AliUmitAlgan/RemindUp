package com.aliumitalgan.remindup.domain.model

enum class PlanType {
    FREE,
    PREMIUM
}

enum class EntitlementStatus {
    ACTIVE,
    INACTIVE,
    GRACE
}

enum class FeatureGate {
    AI_ASSISTANT,
    CONTEXT_REMINDERS,
    SMART_PLANNING
}

data class Entitlement(
    val planType: PlanType = PlanType.FREE,
    val status: EntitlementStatus = EntitlementStatus.ACTIVE,
    val aiDailyQuotaOverride: Int? = null,
    val contextQuotaOverride: Int? = null
)

data class LimitConfig(
    val paidTierEnabled: Boolean = false,
    val freeAiDailyQuota: Int = 0,
    val premiumAiDailyQuota: Int = 0,
    val freeContextReminderLimit: Int = 0,
    val premiumContextReminderLimit: Int = Int.MAX_VALUE
)

data class AccessDecision(
    val allowed: Boolean,
    val reason: String? = null,
    val fallbackOnly: Boolean = false
)
