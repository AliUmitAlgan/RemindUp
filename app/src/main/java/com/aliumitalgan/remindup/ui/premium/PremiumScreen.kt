package com.aliumitalgan.remindup.ui.premium

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aliumitalgan.remindup.R
import com.aliumitalgan.remindup.billing.BillingManager
import com.aliumitalgan.remindup.core.di.LocalAppContainer
import kotlinx.coroutines.launch

private const val PREMIUM_MONTHLY_ID = "premium_monthly"
private const val PREMIUM_YEARLY_ID = "premium_yearly"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumScreen(
    onNavigateBack: () -> Unit,
    viewModel: PremiumViewModel = viewModel(
        factory = PremiumViewModelFactory(LocalAppContainer.current)
    )
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    val coroutineScope = rememberCoroutineScope()
    val detailsByProductId = remember { mutableStateMapOf<String, com.android.billingclient.api.ProductDetails>() }

    val billingManager = remember {
        BillingManager(
            context = context,
            onPurchaseCompleted = { purchase ->
                val productId = purchase.products.firstOrNull() ?: return@BillingManager
                viewModel.onEvent(
                    PremiumUiEvent.PurchaseVerified(
                        purchaseToken = purchase.purchaseToken,
                        productId = productId
                    )
                )
            },
            onPurchaseError = { message ->
                viewModel.onEvent(PremiumUiEvent.BillingError(message))
            }
        )
    }

    LaunchedEffect(Unit) {
        if (!billingManager.connect()) {
            return@LaunchedEffect
        }
        val details = billingManager.querySubscriptions(
            listOf(PREMIUM_MONTHLY_ID, PREMIUM_YEARLY_ID)
        )
        detailsByProductId.clear()
        details.forEach { detailsByProductId[it.productId] = it }
        val offers = details.map {
            val price = it.subscriptionOfferDetails
                ?.firstOrNull()
                ?.pricingPhases
                ?.pricingPhaseList
                ?.firstOrNull()
                ?.formattedPrice
                ?: "-"
            PremiumOfferUi(
                productId = it.productId,
                title = it.name,
                price = price
            )
        }
        viewModel.onEvent(PremiumUiEvent.OffersLoaded(offers))
    }

    DisposableEffect(Unit) {
        onDispose { billingManager.disconnect() }
    }

    Scaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                    Text(
                        text = stringResource(R.string.premium_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 10.dp)
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.premium_subtitle),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            item {
                Text(
                    text = stringResource(R.string.premium_current_plan, state.currentPlanLabel),
                    style = MaterialTheme.typography.labelLarge
                )
            }

            state.infoMessage?.let { message ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(text = message, modifier = Modifier.padding(12.dp))
                    }
                }
            }
            state.errorMessage?.let { message ->
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            if (state.isLoading) {
                item {
                    CircularProgressIndicator()
                }
            }

            items(state.offers) { offer ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = offer.title, style = MaterialTheme.typography.titleMedium)
                            Text(text = offer.price, style = MaterialTheme.typography.bodyMedium)
                        }
                        Button(
                            onClick = {
                                val details = detailsByProductId[offer.productId] ?: return@Button
                                val safeActivity = activity ?: return@Button
                                billingManager.launchPurchase(safeActivity, details)
                            }
                        ) {
                            Text(stringResource(R.string.premium_subscribe))
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        if (activity == null) return@TextButton
                        viewModel.onEvent(PremiumUiEvent.RestorePurchases)
                        coroutineScope.launch {
                            val purchases = billingManager.restorePurchases()
                            purchases.forEach { purchase ->
                                val productId = purchase.products.firstOrNull() ?: return@forEach
                                viewModel.onEvent(
                                    PremiumUiEvent.PurchaseVerified(
                                        purchaseToken = purchase.purchaseToken,
                                        productId = productId
                                    )
                                )
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.premium_restore))
                }
            }
        }
    }
}
