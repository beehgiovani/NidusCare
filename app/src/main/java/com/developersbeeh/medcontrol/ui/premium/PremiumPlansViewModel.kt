package com.developersbeeh.medcontrol.ui.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.ProductDetails
import com.developersbeeh.medcontrol.billing.BillingClientWrapper
import com.developersbeeh.medcontrol.util.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

// DATA CLASS SIMPLIFICADA
data class SubscriptionProducts(
    val monthly: ProductDetails? = null,
    val annual: ProductDetails? = null
)

@HiltViewModel
class PremiumPlansViewModel @Inject constructor(
    private val billingClientWrapper: BillingClientWrapper
) : ViewModel() {

    private val _products = MutableStateFlow<SubscriptionProducts?>(null)
    val products = _products.asStateFlow()

    private val _purchaseFeedback = MutableStateFlow<Event<String>?>(null)
    val purchaseFeedback = _purchaseFeedback.asStateFlow()

    init {
        billingClientWrapper.startConnection()
        viewModelScope.launch {
            billingClientWrapper.productDetails.collectLatest { productList ->
                // LÓGICA SIMPLIFICADA: Busca apenas os 2 produtos premium
                _products.value = SubscriptionProducts(
                    monthly = productList.find { it.productId == "premium_mes" },
                    annual = productList.find { it.productId == "premium_ano" }
                )
            }
        }
        viewModelScope.launch {
            billingClientWrapper.purchaseStatus.collectLatest { purchase ->
                purchase?.let {
                    // Após a compra, a notificação do Google/backend cuidará de atualizar o status.
                    // Aqui, apenas damos um feedback de sucesso para a UI.
                    _purchaseFeedback.value = Event("Assinatura ativada com sucesso! Bem-vindo(a) ao Premium.")
                }
            }
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        billingClientWrapper.launchPurchaseFlow(activity, productDetails)
    }

    fun retryConnection() {
        billingClientWrapper.startConnection()
    }
}