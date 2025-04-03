package com.monext.sdk.internal.presentation

import android.app.Application
import android.content.Context
import android.icu.util.TimeZone
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.WalletConstants
import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.PaymentOverlayToggle
import com.monext.sdk.internal.api.PaymentAPIFactory
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.api.model.DeviceInfo
import com.monext.sdk.internal.api.model.request.PaymentParams
import com.monext.sdk.internal.api.model.request.PaymentRequest
import com.monext.sdk.internal.api.model.request.SecuredPaymentRequest
import com.monext.sdk.internal.api.model.request.WalletPaymentRequest
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.SessionStateRepository
import com.monext.sdk.internal.data.sessionstate.Wallet
import com.monext.sdk.internal.presentation.paymentmethods.GooglePayRequestData
import com.monext.sdk.internal.presentation.paymentmethods.GooglePayUtil
import com.monext.sdk.internal.threeds.ThreeDSManager
import com.monext.sdk.internal.threeds.model.SDKContextData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.Date
import java.util.concurrent.TimeUnit

internal class SessionStateViewModel(val sessionStateRepository: SessionStateRepository, val app: Application): AndroidViewModel(app) {

    companion object {

        internal val ANDROID_CONTEXT = object : CreationExtras.Key<Context> {}
        internal val INTERNAL_SDK_CONTEXT = object : CreationExtras.Key<InternalSDKContext> {}
        internal val LANGUAGE_KEY = object : CreationExtras.Key<String> {}
        internal val INSPECTION_MODE_KEY = object : CreationExtras.Key<Boolean> {}

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
            encodeDefaults = true
        }

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application

                val internalContext = this[INTERNAL_SDK_CONTEXT] as InternalSDKContext
                val paymentAPI = PaymentAPIFactory.create(
                    environment = internalContext.environment,
                    language = this[LANGUAGE_KEY] as String,
                    logger = internalContext.logger
                )
                val repository = SessionStateRepository(
                    paymentAPI = paymentAPI,
                    internalSDKContext = internalContext,
                    threeDSManager = ThreeDSManager(paymentApi = paymentAPI, internalSDKContext = internalContext, context = app.baseContext)
                )

                SessionStateViewModel(repository, app)
            }
        }
    }

    val sessionState: StateFlow<SessionState?>
        get() = sessionStateRepository.sessionState

    private val _loading = MutableStateFlow(false)
    val sessionLoading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _canPayGooglePay = MutableStateFlow(false)
    val canPayGooglePay = _canPayGooglePay.asStateFlow()

    var googlePayUtil: GooglePayUtil? = null
        private set

    private var paymentsClient: PaymentsClient? = null

    fun updateContext(context: InternalSDKContext) {
        sessionStateRepository.updateContext(context)
    }

    fun initializeSessionState(token: String) = viewModelScope.launch {
        sessionStateRepository.initializeSessionState(token)
        sessionState.value?.let {
            initializeGooglePay(it)
        }
    }

    private suspend fun initializeGooglePay(sessionState: SessionState) {

        val gPayEnv = when (sessionStateRepository.internalSDKContext) {
            MnxtEnvironment.Sandbox -> WalletConstants.ENVIRONMENT_TEST
            else -> WalletConstants.ENVIRONMENT_PRODUCTION
        }

        val inputs = GooglePayRequestData(sessionState, gPayEnv)
        inputs.googlePay?.also {
            googlePayUtil = GooglePayUtil(inputs)
            paymentsClient = googlePayUtil?.createPaymentsClient(app, inputs.environment)
        }
        verifyGooglePayReadiness(inputs.googlePay != null)
    }

    fun clearSession() {
        sessionStateRepository.clearSession()
    }

    fun updateSessionState(token: String) = viewModelScope.launch {
        sessionStateRepository.updateSessionState(token)
    }

    fun makePayment(paymentAttempt: PaymentAttempt, context: Context, showOverlay: (PaymentOverlayToggle) -> Unit) = viewModelScope.launch {

        _loading.value = true
        if (paymentAttempt.selectedPaymentMethod != null) {
            when (paymentAttempt.selectedPaymentMethod) {
                is PaymentMethod.Cards -> makeCardPayment(paymentAttempt.paymentFormData, context, showOverlay)
                else -> makePaymentMethodPayment(paymentAttempt.selectedPaymentMethod, paymentAttempt.paymentFormData, showOverlay)
            }
        } else if (paymentAttempt.selectedWallet != null) {
            makeWalletPayment(paymentAttempt.selectedWallet, paymentAttempt.walletFormData, showOverlay)
        }
        _loading.value = false
    }

    /**
     * Fonction qui permet de traiter les paiements par Carte
     */
    private suspend fun makeCardPayment(paymentFormData: FormData?, context: Context, showOverlay: (PaymentOverlayToggle) -> Unit) {

        val formData = (paymentFormData as? FormData.Card) ?: return
        val paymentMethod = formData.paymentMethod
        val cardCode = paymentMethod.cardCode ?: return
        val pmData = paymentMethod.data ?: return

        val cardType = formData.cardNetwork?.network?.name ?: cardCode.name

        // On check si le 3DS est initialisé, si ce n'est pas le cas, on le fait
        if(!sessionStateRepository.threeDSManager.isInitialized) {
            withContext(Dispatchers.IO) {
                // Blocking network request code
                sessionStateRepository.threeDSManager.initialize(sessionToken = sessionState.value!!.token, cardCode = cardType)
            }
        }

        val sdkContextData: SDKContextData =
            sessionStateRepository.threeDSManager.generateSDKContextData(cardType)

        val displayMetrics = context.resources.displayMetrics
        val timeZone = TimeZone.getDefault()
        val millisecondsOffset = timeZone.getOffset(Date().time)
        val minutesOffset = TimeUnit.MILLISECONDS.toSeconds(millisecondsOffset.toLong()).toInt()

        val paymentParams = formData.paymentParams()
        paymentParams.sdkContextData = json.encodeToString(sdkContextData)

        val params = SecuredPaymentRequest(
            cardCode = cardCode.name,
            contractNumber = pmData.contractNumber ?: "",
            deviceInfo = DeviceInfo(
                colorDepth = 32,
                containerHeight = 498.467,
                containerWidth = 750,
                javaEnabled = false,
                screenHeight = displayMetrics.heightPixels,
                screenWidth = displayMetrics.widthPixels,
                timeZoneOffset = minutesOffset
            ),
            isEmbeddedRedirectionAllowed = true,
            merchantReturnUrl = sessionStateRepository.returnURLString,
            paymentParams = paymentParams,
            securedPaymentParams = formData.securedPaymentParams()
        )

        showOverlay(PaymentOverlayToggle.on(formData.cardNetwork?.network ?: cardCode))
        sessionStateRepository.makeSecuredPayment(params)
        showOverlay(PaymentOverlayToggle.off())
    }

    /**
     * Fonction qui permet de traiter les paiements (autre que Carte)
     */
    private suspend fun makePaymentMethodPayment(selectedPaymentMethod: PaymentMethod?, paymentFormData: FormData?, showOverlay: (PaymentOverlayToggle) -> Unit) {

        val paymentMethod = selectedPaymentMethod ?: return
        val formData = paymentFormData ?: return
        val cardCode = paymentMethod.cardCode ?: return
        val pmData = paymentMethod.data ?: return

        val params = PaymentRequest(
            cardCode = cardCode.name,
            merchantReturnUrl = sessionStateRepository.returnURLString,
            isEmbeddedRedirectionAllowed = true,
            paymentParams = formData.paymentParams(),
            contractNumber = pmData.contractNumber ?: ""
        )

        showOverlay(PaymentOverlayToggle.on(cardCode))
        sessionStateRepository.makePayment(params)
        showOverlay(PaymentOverlayToggle.off())
    }

    private suspend fun makeWalletPayment(selectedWallet: Wallet?, walletFormData: FormData.Wallet?, showOverlay: (PaymentOverlayToggle) -> Unit) {

        val selectedWallet = selectedWallet ?: return
        val walletData = walletFormData ?: return

        val params = WalletPaymentRequest(
            cardCode = selectedWallet.cardCode,
            index = selectedWallet.index,
            isEmbeddedRedirectionAllowed = true,
            merchantReturnUrl = sessionStateRepository.returnURLString,
            securedPaymentParams = walletData.securedPaymentParams()
        )

        showOverlay(PaymentOverlayToggle.on(selectedWallet.cardType))
        sessionStateRepository.makeWalletPayment(params)
        showOverlay(PaymentOverlayToggle.off())
    }

    // region GooglePay

    /**
     * Determine the user's ability to pay with a payment method supported by your app and display
     * a Google Pay payment button.
     */
    private suspend fun verifyGooglePayReadiness(googlePayAvailable: Boolean) {
        if (googlePayAvailable) {
            _canPayGooglePay.value = try {
                fetchCanUseGooglePay()
            } catch (exception: ApiException) {
                Log.d("GOOGLE_PAY", exception.localizedMessage ?: "")
                false
            }
        }
    }

    /**
     * Determine the user's ability to pay with a payment method supported by your app.
     */
    private suspend fun fetchCanUseGooglePay(): Boolean {
        val request = IsReadyToPayRequest.fromJson(googlePayUtil?.isReadyToPayRequest().toString())
        return paymentsClient?.isReadyToPay(request)?.await() == true
    }

    /**
     * Creates a [Task] that starts the payment process with the transaction details included.
     *
     * @return a [Task] with the payment information.
     * @see [PaymentDataRequest](https://developers.google.com/android/reference/com/google/android/gms/wallet/PaymentsClient#loadPaymentData(com.google.android.gms.wallet.PaymentDataRequest)
     */
    fun loadPaymentDataTask(onComplete: OnCompleteListener<PaymentData>) {
        val priceLabel = googlePayUtil?.inputs?.orderAmount ?: ""
        val paymentDataRequestJson = googlePayUtil?.getPaymentDataRequest(priceLabel)
        val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        val task = paymentsClient?.loadPaymentData(request)
        task?.addOnCompleteListener(onComplete)
    }

    fun makeGooglePayPayment(paymentData: PaymentData, showOverlay: (PaymentOverlayToggle) -> Unit) = viewModelScope.launch {

        val paymentMethod = sessionState.value?.paymentMethodsList?.paymentMethods?.firstOrNull { it is PaymentMethod.GooglePay } ?: return@launch
        val cardCode = paymentMethod.cardCode ?: return@launch

        val params = PaymentRequest(
            cardCode = cardCode.name,
            merchantReturnUrl = sessionStateRepository.returnURLString,
            isEmbeddedRedirectionAllowed = false,
            paymentParams = PaymentParams(
                googlePayData = paymentData.toJson()
            ),
            contractNumber = paymentMethod.data?.contractNumber ?: ""
        )

        _loading.value = true
        showOverlay(PaymentOverlayToggle.on(cardCode))
        sessionStateRepository.makeGooglePayPayment(params)
        showOverlay(PaymentOverlayToggle.off())
        _loading.value = false
    }

    // endregion

}