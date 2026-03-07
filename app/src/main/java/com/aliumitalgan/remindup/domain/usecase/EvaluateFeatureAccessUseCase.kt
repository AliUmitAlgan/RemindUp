package com.aliumitalgan.remindup.domain.usecase

import com.aliumitalgan.remindup.domain.model.AccessDecision
import com.aliumitalgan.remindup.domain.model.EntitlementStatus
import com.aliumitalgan.remindup.domain.model.FeatureGate
import com.aliumitalgan.remindup.domain.model.PlanType
import com.aliumitalgan.remindup.domain.repository.EntitlementRepository

class EvaluateFeatureAccessUseCase(
    private val entitlementRepository: EntitlementRepository
) {
    suspend operator fun invoke(feature: FeatureGate): AccessDecision {
        val entitlement = entitlementRepository.getEntitlement()
        val limits = entitlementRepository.getLimitConfig()
        val isPremiumActive = entitlement.planType == PlanType.PREMIUM &&
            (entitlement.status == EntitlementStatus.ACTIVE || entitlement.status == EntitlementStatus.GRACE)

        return when (feature) {
            FeatureGate.CONTEXT_REMINDERS -> {
                if (isPremiumActive) {
                    AccessDecision(allowed = true)
                } else {
                    AccessDecision(
                        allowed = false,
                        reason = "Context reminders require Premium."
                    )
                }
            }

            FeatureGate.SMART_PLANNING -> {
                if (isPremiumActive) {
                    AccessDecision(allowed = true)
                } else {
                    AccessDecision(
                        allowed = false,
                        reason = "Smart planning is available on Premium."
                    )
                }
            }

            FeatureGate.AI_ASSISTANT -> {
                if (!limits.paidTierEnabled) {
                    return AccessDecision(
                        allowed = false,
                        reason = "AI is currently in closed beta mode.",
                        fallbackOnly = true
                    )
                }

                val usage = entitlementRepository.getTodayAiUsage()
                val configuredQuota = when {
                    entitlement.aiDailyQuotaOverride != null -> entitlement.aiDailyQuotaOverride
                    isPremiumActive -> limits.premiumAiDailyQuota
                    else -> limits.freeAiDailyQuota
                }

                if (usage < configuredQuota) {
                    AccessDecision(allowed = true)
                } else {
                    AccessDecision(
                        allowed = false,
                        reason = "Daily AI quota reached.",
                        fallbackOnly = true
                    )
                }
            }
        }
    }
}
