package com.monext.sdk.internal.data

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.staticCompositionLocalOf
import com.monext.sdk.internal.api.AvailableCardNetworksRequest
import com.monext.sdk.internal.api.AvailableCardNetworksResponse
import com.monext.sdk.internal.api.PaymentAPI
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.api.model.request.PaymentRequest
import com.monext.sdk.internal.api.model.request.SecuredPaymentRequest
import com.monext.sdk.internal.api.model.request.WalletPaymentRequest
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.threeds.ThreeDSManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class SessionStateRepository(
    private val paymentAPI: PaymentAPI,
    var internalSDKContext: InternalSDKContext,
    var threeDSManager: ThreeDSManager) {

    private val _sessionState = MutableStateFlow<SessionState?>(null)
    val sessionState = _sessionState.asStateFlow()

    private var token: String? = null

    val returnURLString = Uri.Builder()
        .scheme("https")
        .authority(internalSDKContext.environment.host)
        .build()
        .toString()

    suspend fun initializeSessionState(token: String) {
        if (this.token == token) return
        updateSessionState(token)
    }

    fun clearSession() {
        token = null
        _sessionState.value = null
    }

    fun updateContext(context: InternalSDKContext) {
        paymentAPI.updateContext(context)
        internalSDKContext = context
    }

    suspend fun updateSessionState(token: String) {
        makeRequest {
            val sState = paymentAPI.stateCurrent(token)
            this.token = sState.token
            sState
        }
    }

    suspend fun makeSecuredPayment(params: SecuredPaymentRequest) {
        makeRequest {
            val token = token ?: throw INVALID_TOKEN_EXCEPTION
            paymentAPI.securedPayment(token, params)
        }
    }

    suspend fun makePayment(params: PaymentRequest) {
        makeRequest {
            val token = token ?: throw INVALID_TOKEN_EXCEPTION
            paymentAPI.payment(token, params)
        }
    }

    suspend fun makeGooglePayPayment(params: PaymentRequest) {
        makeRequest {
            val token = token ?: throw INVALID_TOKEN_EXCEPTION
            paymentAPI.payment(token, params)
        }
    }

    suspend fun makeWalletPayment(params: WalletPaymentRequest) {
        makeRequest {
            val token = token ?: throw INVALID_TOKEN_EXCEPTION
            paymentAPI.walletPayment(token, params)
        }
    }

    suspend fun availableCardNetworks(params: AvailableCardNetworksRequest): AvailableCardNetworksResponse? {
        return try {
            val token = token ?: throw INVALID_TOKEN_EXCEPTION
            paymentAPI.availableCardNetworks(token, params)
        } catch (t: Throwable) {
            Log.d("NETWORK", "==> availableCardNetworks")
            Log.e("NETWORK", t.localizedMessage ?: "unknown error", t)
            null
        }
    }

    private suspend fun makeRequest(yield: suspend () -> SessionState) {
        try {
            animateSessionStateChange(yield())
        } catch (t: Throwable) {
            Log.d("NETWORK", "==> makeRequest")
            Log.e("NETWORK", t.localizedMessage ?: "unknown error", t)
        }
    }

    private fun animateSessionStateChange(sState: SessionState) {
        _sessionState.value = sState
    }

    companion object {
        val INVALID_TOKEN_EXCEPTION = RuntimeException("invalid token")
    }
}

internal val LocalSessionStateRepo = staticCompositionLocalOf<SessionStateRepository> {
    error("No CompositionLocal LocalSessionStateRepo")
}