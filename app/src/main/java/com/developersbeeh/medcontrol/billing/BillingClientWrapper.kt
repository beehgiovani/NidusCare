package com.developersbeeh.medcontrol.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.developersbeeh.medcontrol.data.UserPreferences
import com.developersbeeh.medcontrol.data.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingClientWrapper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : PurchasesUpdatedListener, BillingClientStateListener {

    private lateinit var billingClient: BillingClient
    private var onConnectedActions = mutableListOf<() -> Unit>()
    private val premiumProductIds = listOf("premium_mes", "premium_ano")

    private val _productDetails = MutableStateFlow<List<ProductDetails>>(emptyList())
    val productDetails = _productDetails.asStateFlow()

    private val _purchaseStatus = MutableStateFlow<Purchase?>(null)
    val purchaseStatus = _purchaseStatus.asStateFlow()

    fun startConnection() {
        if (::billingClient.isInitialized && billingClient.isReady) {
            onConnectedActions.forEach { it.invoke() }
            onConnectedActions.clear()
            return
        }
        val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases(pendingPurchasesParams)
            .build()
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.i("BillingClient", "Conexão com a Google Play Store estabelecida com sucesso.")
            onConnectedActions.forEach { it.invoke() }
            onConnectedActions.clear()
            queryProductDetails()
            queryPurchases()
        } else {
            Log.e("BillingClient", "Falha na conexão com a Google Play Store: ${billingResult.debugMessage}")
            onConnectedActions.clear()
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.w("BillingClient", "Serviço de faturamento desconectado. Tentando reconectar...")
    }

    private fun executeOnSuccess(block: () -> Unit) {
        if (::billingClient.isInitialized && billingClient.isReady) {
            block()
        } else {
            onConnectedActions.add(block)
            startConnection()
        }
    }

    private fun queryProductDetails() {
        executeOnSuccess {
            val productList = premiumProductIds.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }
            val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

            // ✅ CORREÇÃO APLICADA AQUI
            billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
                // 1. Pega a lista de produtos de dentro do objeto de resultado.
                val productDetailsList = queryProductDetailsResult?.productDetailsList

                // 2. Verifica se a lista extraída não é nula ou vazia.
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !productDetailsList.isNullOrEmpty()) {
                    _productDetails.value = productDetailsList
                } else {
                    _productDetails.value = emptyList()
                    Log.e("BillingClient", "Erro ao buscar detalhes dos produtos: ${billingResult.debugMessage}")
                }
            }
        }
    }

    fun queryPurchases() {
        executeOnSuccess {
            val params = QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val hasActivePurchase = purchases.any { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    if (hasActivePurchase) {
                        CoroutineScope(Dispatchers.IO).launch {
                            purchases.forEach { handlePurchase(it) }
                        }
                    } else {
                        CoroutineScope(Dispatchers.IO).launch { userPreferences.saveIsPremium(false) }
                    }
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            CoroutineScope(Dispatchers.IO).launch {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _purchaseStatus.value = null
            Log.i("BillingClient", "Compra cancelada pelo usuário.")
        } else {
            _purchaseStatus.value = null
            Log.e("BillingClient", "Erro na atualização da compra: ${billingResult.debugMessage}")
        }
    }

    private suspend fun     handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.i("BillingClient", "Compra confirmada com sucesso.")
                    }
                }
            }

            // ✅ CORREÇÃO: Lógica simplificada. Qualquer compra agora ativa o Premium individual.
            userRepository.savePurchaseDetails(purchase).onSuccess {
                userRepository.updatePremiumStatus(true).onSuccess {
                    userPreferences.saveIsPremium(true)
                    _purchaseStatus.value = purchase // Sinaliza à UI que a compra foi processada
                }
            }
        }
    }



    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        executeOnSuccess {
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken == null) {
                Log.e("BillingClient", "Token de oferta não encontrado para o produto: ${productDetails.productId}")
                return@executeOnSuccess
            }
            val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productDetailsParams))
                .build()
            _purchaseStatus.value = null
            billingClient.launchBillingFlow(activity, billingFlowParams)
        }
    }
}