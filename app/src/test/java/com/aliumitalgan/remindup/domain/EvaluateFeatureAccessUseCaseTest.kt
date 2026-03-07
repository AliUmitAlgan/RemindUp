package com.aliumitalgan.remindup.domain

import com.aliumitalgan.remindup.domain.model.Entitlement
import com.aliumitalgan.remindup.domain.model.EntitlementStatus
import com.aliumitalgan.remindup.domain.model.FeatureGate
import com.aliumitalgan.remindup.domain.model.LimitConfig
import com.aliumitalgan.remindup.domain.model.PlanType
import com.aliumitalgan.remindup.domain.repository.EntitlementRepository
import com.aliumitalgan.remindup.domain.usecase.EvaluateFeatureAccessUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EvaluateFeatureAccessUseCaseTest {

    @Test
    fun `ai is fallback only when paid tier disabled`() = runBlocking {
        val repo = FakeEntitlementRepository(
            entitlement = Entitlement(planType = PlanType.FREE, status = EntitlementStatus.ACTIVE),
            limitConfig = LimitConfig(
                paidTierEnabled = false,
                freeAiDailyQuota = 0,
                premiumAiDailyQuota = 0
            ),
            todayUsage = 0
        )
        val useCase = EvaluateFeatureAccessUseCase(repo)

        val decision = useCase(FeatureGate.AI_ASSISTANT)

        assertFalse(decision.allowed)
        assertTrue(decision.fallbackOnly)
    }

    @Test
    fun `context reminders require premium`() = runBlocking {
        val repo = FakeEntitlementRepository(
            entitlement = Entitlement(planType = PlanType.FREE, status = EntitlementStatus.ACTIVE),
            limitConfig = LimitConfig(paidTierEnabled = true),
            todayUsage = 0
        )
        val useCase = EvaluateFeatureAccessUseCase(repo)

        val decision = useCase(FeatureGate.CONTEXT_REMINDERS)

        assertFalse(decision.allowed)
    }
}

private class FakeEntitlementRepository(
    private val entitlement: Entitlement,
    private val limitConfig: LimitConfig,
    private val todayUsage: Int
) : EntitlementRepository {
    override suspend fun getEntitlement(): Entitlement = entitlement
    override suspend fun getLimitConfig(): LimitConfig = limitConfig
    override suspend fun getTodayAiUsage(): Int = todayUsage
    override suspend fun incrementTodayAiUsage(): Boolean = true
}
