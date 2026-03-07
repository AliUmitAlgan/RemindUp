package com.aliumitalgan.remindup.ui.premium

data class PremiumOfferUi(
    val productId: String,
    val title: String,
    val price: String
)

data class PremiumUiState(
    val isLoading: Boolean = true,
    val currentPlanLabel: String = "Free",
    val offers: List<PremiumOfferUi> = emptyList(),
    val infoMessage: String? = null,
    val errorMessage: String? = null
)

sealed interface PremiumUiEvent {
    data class OffersLoaded(val offers: List<PremiumOfferUi>) : PremiumUiEvent
    data class PurchaseVerified(val purchaseToken: String, val productId: String) : PremiumUiEvent
    data class BillingError(val message: String) : PremiumUiEvent
    data object RestorePurchases : PremiumUiEvent
    data object Refresh : PremiumUiEvent
    data object ClearMessages : PremiumUiEvent
}
