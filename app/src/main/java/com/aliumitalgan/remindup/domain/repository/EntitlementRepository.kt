package com.aliumitalgan.remindup.domain.repository

import com.aliumitalgan.remindup.domain.model.Entitlement
import com.aliumitalgan.remindup.domain.model.LimitConfig

interface EntitlementRepository {
    suspend fun getEntitlement(): Entitlement
    suspend fun getLimitConfig(): LimitConfig
    suspend fun getTodayAiUsage(): Int
    suspend fun incrementTodayAiUsage(): Boolean
}
