package com.monext.sdk.internal.presentation

import androidx.compose.runtime.Composable
import com.monext.sdk.PaymentResult
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.sessionstate.PaymentMethodsList
import com.monext.sdk.internal.api.model.SessionInfo
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.api.model.response.SessionStateType
import com.monext.sdk.internal.data.sessionstate.Wallet
import com.monext.sdk.internal.presentation.status.LoadingSection
import com.monext.sdk.internal.presentation.status.PaymentCanceledScreen
import com.monext.sdk.internal.presentation.status.PaymentFailureScreen
import com.monext.sdk.internal.presentation.status.PaymentRedirectionScreen
import com.monext.sdk.internal.presentation.status.PaymentSuccessScreen
import com.monext.sdk.internal.presentation.status.TokenExpiredScreen

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
    onResult: (PaymentResult) -> Unit
) {

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

        SessionStateType.PAYMENT_SUCCESS -> {
            sessionState.info?.let { info ->
                sessionState.paymentSuccess?.let { success ->
                    PaymentSuccessScreen(info, success) {
                        onResult(
                            PaymentResult.PaymentCompleted(
                                PaymentResult.TransactionState.PAYMENT_SUCCESS
                            )
                        )
                    }
                }
            } ?: LoadingSection()
        }

        SessionStateType.PAYMENT_FAILURE -> PaymentFailureScreen(
            sessionState.info?.formattedAmount ?: "",
            onRetry = onRetry,
            onExit = {
                onResult(
                    PaymentResult.PaymentCompleted(
                        PaymentResult.TransactionState.PAYMENT_FAILURE
                    )
                )
            }
        )

        SessionStateType.PAYMENT_CANCELED -> PaymentCanceledScreen {
            onResult(
                PaymentResult.PaymentCompleted(
                    PaymentResult.TransactionState.PAYMENT_CANCELED
                )
            )
        }

        SessionStateType.TOKEN_EXPIRED -> TokenExpiredScreen {
            onResult(
                PaymentResult.PaymentCompleted(
                    PaymentResult.TransactionState.TOKEN_EXPIRED
                )
            )
        }

        else -> LoadingSection()
    }
}

