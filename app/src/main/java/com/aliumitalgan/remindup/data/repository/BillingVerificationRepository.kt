package com.aliumitalgan.remindup.data.repository

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await

class BillingVerificationRepository(
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) {
    suspend fun verifyPurchase(purchaseToken: String, productId: String): Boolean {
        val payload = mapOf(
            "purchaseToken" to purchaseToken,
            "productId" to productId
        )
        val result = functions
            .getHttpsCallable("verifySubscriptionPurchase")
            .call(payload)
            .await()

        @Suppress("UNCHECKED_CAST")
        val data = result.data as? Map<String, Any?> ?: return false
        return data["success"] == true
    }
}
