package com.monext.sdk.internal.data

import android.app.Activity
import androidx.compose.runtime.staticCompositionLocalOf
import com.monext.sdk.Appearance
import com.monext.sdk.internal.api.AvailableCardNetworksRequest
import com.monext.sdk.internal.api.AvailableCardNetworksResponse
import com.monext.sdk.internal.api.PaymentAPI
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.api.model.request.PaymentRequest
import com.monext.sdk.internal.api.model.request.SecuredPaymentRequest
import com.monext.sdk.internal.api.model.request.WalletPaymentRequest
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.threeds.ThreeDSManager
import com.monext.sdk.internal.threeds.model.AuthenticationResponse
import com.monext.sdk.internal.threeds.model.ChallengeUseCaseCallback
import com.monext.sdk.internal.threeds.model.SdkChallengeData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.URI

internal class SessionStateRepository(
    private val paymentAPI: PaymentAPI,
    var internalSDKContext: InternalSDKContext,
    var threeDSManager: ThreeDSManager) {

    private val _sessionState = MutableStateFlow<SessionState?>(null)
    val sessionState = _sessionState.asStateFlow()

    private var token: String? = null

    val returnURLString = URI("https", internalSDKContext.environment.host, null).toString()

    private var UNKNOWN_ERROR = "unknown error"

    suspend fun initializeSessionState(token: String) {
        if (this.token == token) return
        updateSessionState(token)
    }

    fun clearSession() {
        token = null
        _sessionState.value = null
    }

    fun updateContext(context: InternalSDKContext) {
        // On conserve la configuration issue du premier appelle Api
        val currentSendRemoteLogs = internalSDKContext.isSendRemoteLogs
        internalSDKContext = context
        internalSDKContext.isSendRemoteLogs = currentSendRemoteLogs;

        paymentAPI.updateContext(internalSDKContext)
    }

    suspend fun updateSessionState(token: String) {
        makeRequest {
            val sState = paymentAPI.stateCurrent(sessionToken = token, merchantReturnUrl = this.returnURLString)
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

    suspend fun makeSdkPayment(params: AuthenticationResponse) {
        makeRequest {
            val token = token ?: throw INVALID_TOKEN_EXCEPTION
            paymentAPI.sdkPaymentRequest(token, params)
        }
    }

    suspend fun isDone() {
        try {
            while (true) {
                val currentState = sessionState.value ?: return
                val sessionToken = currentState.token
                val cardCode = currentState.activeWaiting?.cardCode ?: return

                val done = try {
                    paymentAPI.isDone(sessionToken, cardCode)
                } catch (t: Throwable) {
                    internalSDKContext.logger.e(TAG, "error when call isDone ${t.localizedMessage ?:UNKNOWN_ERROR}", t)
                    return
                }

                if (done) {
                    updateSessionState(sessionToken)
                    return
                } else {
                    delay(3_000)
                }
            }
        } catch (t: Throwable) {
            internalSDKContext.logger.e(TAG, "error when call isDone ${t.localizedMessage ?: UNKNOWN_ERROR}", t)
        }
    }

    /**
     * Lance le flow Challenge
     */
    suspend fun makeThreeDsChallengeFlow(
        activity: Activity,
        sdkChallengeData: SdkChallengeData,
        theme: Appearance,
        useCaseCallback: ChallengeUseCaseCallback) {

        threeDSManager.doChallengeFlow(activity, sdkChallengeData, theme, object: ChallengeUseCaseCallback {
            override fun onChallengeCompletion(authenticationResponse: AuthenticationResponse) {
                // Le challenge est terminé, on close la transation
                threeDSManager.closeTransaction()
                // On appelle la callback pour la suite du traitement.
                useCaseCallback.onChallengeCompletion(authenticationResponse)
            }
        })
    }

    suspend fun availableCardNetworks(params: AvailableCardNetworksRequest): AvailableCardNetworksResponse? {
        return try {
            val token = token ?: throw INVALID_TOKEN_EXCEPTION
            paymentAPI.availableCardNetworks(token, params)
        } catch (t: Throwable) {
            internalSDKContext.logger.e(TAG, "error when call availableCardNetworks ${t.localizedMessage ?: UNKNOWN_ERROR}", t)
            null
        }
    }

    private suspend fun makeRequest(callback: suspend () -> SessionState) {
        try {
            animateSessionStateChange(callback())
        } catch (t: Throwable) {
            internalSDKContext.logger.e(TAG, "error when call makeRequest ${t.localizedMessage ?: UNKNOWN_ERROR}", t)
        }
    }

    internal fun animateSessionStateChange(sState: SessionState) {
        _sessionState.value = sState
    }

    companion object {
        val INVALID_TOKEN_EXCEPTION = RuntimeException("invalid token")
        const val TAG = "SessionStateRepo"
    }
}

internal val LocalSessionStateRepo = staticCompositionLocalOf<SessionStateRepository> {
    error("No CompositionLocal LocalSessionStateRepo")
}