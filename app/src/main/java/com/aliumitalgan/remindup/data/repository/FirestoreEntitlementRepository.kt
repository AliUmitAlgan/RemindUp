package com.aliumitalgan.remindup.data.repository

import com.aliumitalgan.remindup.domain.model.Entitlement
import com.aliumitalgan.remindup.domain.model.EntitlementStatus
import com.aliumitalgan.remindup.domain.model.LimitConfig
import com.aliumitalgan.remindup.domain.model.PlanType
import com.aliumitalgan.remindup.domain.repository.EntitlementRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FirestoreEntitlementRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : EntitlementRepository {

    override suspend fun getEntitlement(): Entitlement {
        val userId = auth.currentUser?.uid ?: return Entitlement()
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("entitlements")
            .document("current")
            .get()
            .await()

        if (!snapshot.exists()) return Entitlement()

        val planType = when (snapshot.getString("planType")?.uppercase()) {
            "PREMIUM" -> PlanType.PREMIUM
            else -> PlanType.FREE
        }
        val status = when (snapshot.getString("status")?.uppercase()) {
            "INACTIVE" -> EntitlementStatus.INACTIVE
            "GRACE" -> EntitlementStatus.GRACE
            else -> EntitlementStatus.ACTIVE
        }

        return Entitlement(
            planType = planType,
            status = status,
            aiDailyQuotaOverride = snapshot.getLong("aiDailyQuota")?.toInt(),
            contextQuotaOverride = snapshot.getLong("contextQuota")?.toInt()
        )
    }

    override suspend fun getLimitConfig(): LimitConfig {
        val snapshot = firestore.collection("app_config")
            .document("ai_limits")
            .get()
            .await()

        if (!snapshot.exists()) {
            return LimitConfig(
                paidTierEnabled = false,
                freeAiDailyQuota = 0,
                premiumAiDailyQuota = 0,
                freeContextReminderLimit = 0,
                premiumContextReminderLimit = Int.MAX_VALUE
            )
        }

        return LimitConfig(
            paidTierEnabled = snapshot.getBoolean("paidTierEnabled") ?: false,
            freeAiDailyQuota = snapshot.getLong("freeAiDailyQuota")?.toInt() ?: 0,
            premiumAiDailyQuota = snapshot.getLong("premiumAiDailyQuota")?.toInt() ?: 0,
            freeContextReminderLimit = snapshot.getLong("freeContextReminderLimit")?.toInt() ?: 0,
            premiumContextReminderLimit = snapshot.getLong("premiumContextReminderLimit")?.toInt()
                ?: Int.MAX_VALUE
        )
    }

    override suspend fun getTodayAiUsage(): Int {
        val userId = auth.currentUser?.uid ?: return 0
        val dailyId = todayUsageId()
        val snapshot = firestore.collection("users")
            .document(userId)
            .collection("usage")
            .document(dailyId)
            .get()
            .await()
        return snapshot.getLong("aiCount")?.toInt() ?: 0
    }

    override suspend fun incrementTodayAiUsage(): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val dailyId = todayUsageId()
        val usageRef = firestore.collection("users")
            .document(userId)
            .collection("usage")
            .document(dailyId)

        firestore.runTransaction { tx ->
            val snap = tx.get(usageRef)
            val currentCount = snap.getLong("aiCount") ?: 0L
            tx.set(
                usageRef,
                mapOf(
                    "aiCount" to (currentCount + 1),
                    "updatedAt" to System.currentTimeMillis()
                )
            )
        }.await()

        return true
    }

    private fun todayUsageId(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    }
}
