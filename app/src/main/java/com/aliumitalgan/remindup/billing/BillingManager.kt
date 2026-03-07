package com.aliumitalgan.remindup.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class BillingManager(
    context: Context,
    private val onPurchaseCompleted: (Purchase) -> Unit,
    private val onPurchaseError: (String) -> Unit
) {
    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                purchases.forEach { purchase ->
                    acknowledgeIfNeeded(purchase)
                    onPurchaseCompleted(purchase)
                }
            } else if (billingResult.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
                onPurchaseError(billingResult.debugMessage)
            }
        }
        .enablePendingPurchases()
        .build()

    suspend fun connect(): Boolean = suspendCancellableCoroutine { continuation ->
        if (billingClient.isReady) {
            continuation.resume(true)
            return@suspendCancellableCoroutine
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                continuation.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
            }

            override fun onBillingServiceDisconnected() {
                if (continuation.isActive) continuation.resume(false)
            }
        })
    }

    suspend fun querySubscriptions(productIds: List<String>): List<ProductDetails> {
        if (!billingClient.isReady) return emptyList()
        val products = productIds.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()
        return suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(productDetailsList.orEmpty())
                } else {
                    onPurchaseError(billingResult.debugMessage)
                    continuation.resume(emptyList())
                }
            }
        }
    }

    fun launchPurchase(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: return
        val params = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(params))
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    suspend fun restorePurchases(): List<Purchase> = suspendCancellableCoroutine { continuation ->
        if (!billingClient.isReady) {
            continuation.resume(emptyList())
            return@suspendCancellableCoroutine
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            PurchasesResponseListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    purchases.forEach { acknowledgeIfNeeded(it) }
                    continuation.resume(purchases)
                } else {
                    onPurchaseError(billingResult.debugMessage)
                    continuation.resume(emptyList())
                }
            }
        )
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.isAcknowledged) return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params) {}
    }

    fun disconnect() {
        billingClient.endConnection()
    }
}
