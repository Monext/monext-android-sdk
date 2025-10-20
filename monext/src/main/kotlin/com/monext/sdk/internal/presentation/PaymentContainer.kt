package com.monext.sdk.internal.presentation

import androidx.compose.runtime.Composable
import com.monext.sdk.PaymentOverlayToggle
import com.monext.sdk.PaymentResult
import com.monext.sdk.PaymentResult.PaymentCompleted
import com.monext.sdk.PaymentResult.TransactionState
import com.monext.sdk.internal.api.model.SessionInfo
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.api.model.response.SessionStateType
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.sessionstate.PaymentMethodsList
import com.monext.sdk.internal.data.sessionstate.Wallet
import com.monext.sdk.internal.presentation.status.LoadingSection
import com.monext.sdk.internal.presentation.status.PaymentCanceledScreen
import com.monext.sdk.internal.presentation.status.PaymentFailureScreen
import com.monext.sdk.internal.presentation.status.PaymentPendingScreen
import com.monext.sdk.internal.presentation.status.PaymentRedirectionScreen
import com.monext.sdk.internal.presentation.status.PaymentSuccessScreen
import com.monext.sdk.internal.presentation.status.TokenExpiredScreen
import com.monext.sdk.internal.threeds.view.PaymentSdkChallengeScreen

internal data class PaymentAttempt(
    val selectedPaymentMethod: PaymentMethod?,
    val paymentFormData: FormData?,
    val selectedWallet: Wallet?,
    val walletFormData: FormData.Wallet?
)

/**
 * PaymentContainer
 * Permet de gérer le "State" du Widget et de dispatcher les états.
 */
@Composable
internal fun PaymentContainer(
    sessionState: SessionState?,
    paymentMethodsScreen: @Composable (PaymentMethodsList, SessionInfo) -> Unit,
    onRedirectionComplete: () -> Unit,
    onRetry: () -> Unit,
    onResult: (PaymentResult) -> Unit,
    onIsShowingChange: ((Boolean) -> Unit)? = null,
    showOverlay: (PaymentOverlayToggle) -> Unit
) {

    if (sessionState?.type?.isFinalState() == true) {
        when(sessionState.type) {
            SessionStateType.PAYMENT_SUCCESS -> onResult(
                PaymentCompleted(
                    TransactionState.PAYMENT_SUCCESS)
            )
            SessionStateType.PAYMENT_FAILURE -> onResult(
                PaymentCompleted(
                    TransactionState.PAYMENT_FAILURE)
            )
            SessionStateType.PAYMENT_ONHOLD_PARTNER -> onResult(
                PaymentCompleted(
                    TransactionState.PAYMENT_PENDING
                )
            )
            SessionStateType.PAYMENT_CANCELED -> onResult(
                PaymentCompleted(
                    TransactionState.PAYMENT_CANCELED)
            )
            SessionStateType.TOKEN_EXPIRED -> onResult(
                PaymentCompleted(
                    TransactionState.TOKEN_EXPIRED)
            )
            else -> {}
        }
        if (sessionState.automaticRedirectAtSessionsEnd == true) {
            onIsShowingChange?.invoke(false)
            return
        }
    }

    when (sessionState?.type) {
        SessionStateType.PAYMENT_METHODS_LIST -> {
            sessionState.info?.let { info ->
                sessionState.paymentMethodsList?.let { list ->
                    paymentMethodsScreen(list, info)
                }
            } ?: LoadingSection()
        }

        SessionStateType.PAYMENT_REDIRECT_NO_RESPONSE -> {
            sessionState.paymentRedirectNoResponse?.redirectionData?.let { data ->
                PaymentRedirectionScreen(data) {
                    onRedirectionComplete()
                }
            } ?: LoadingSection()
        }

        SessionStateType.SDK_CHALLENGE -> {
            sessionState.paymentSdkChallenge?.sdkChallengeData?.let { data ->
                PaymentSdkChallengeScreen(sdkChallengeData = data, showOverlay)
            } ?: LoadingSection()
        }

        SessionStateType.PAYMENT_SUCCESS -> {
            sessionState.info?.let { info ->
                sessionState.paymentSuccess?.let { success ->
                    PaymentSuccessScreen(info, successData = success) { onIsShowingChange?.invoke(false) }
                }
            } ?: LoadingSection()
        }

        SessionStateType.PAYMENT_FAILURE -> {
            PaymentFailureScreen(
                sessionState.info?.formattedAmount ?: "",
                onRetry = onRetry,
                onExit = { onIsShowingChange?.invoke(false) })
        }

        SessionStateType.PAYMENT_ONHOLD_PARTNER -> {
            sessionState.paymentOnholdPartner?.let { paymentOnholdPartner ->
                PaymentPendingScreen(
                    paymentOnholdPartner,
                    onExit = { onIsShowingChange?.invoke(false) }
                )
            } ?: LoadingSection()
        }

        SessionStateType.PAYMENT_CANCELED -> {
            PaymentCanceledScreen { onIsShowingChange?.invoke(false) }
        }

        SessionStateType.TOKEN_EXPIRED -> {
            TokenExpiredScreen { onIsShowingChange?.invoke(false) }
        }

        else -> LoadingSection()
    }
}

