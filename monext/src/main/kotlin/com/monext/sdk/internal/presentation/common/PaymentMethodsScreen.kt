package com.monext.sdk.internal.presentation.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.monext.sdk.GooglePayConfiguration
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.sessionstate.PaymentMethodsList
import com.monext.sdk.internal.api.model.SessionInfo
import com.monext.sdk.internal.data.sessionstate.Wallet
import com.monext.sdk.internal.presentation.PaymentAttempt
import com.monext.sdk.internal.presentation.paymentmethods.PaymentMethodFormContainer
import com.monext.sdk.internal.presentation.wallet.WalletItem
import com.monext.sdk.internal.presentation.wallet.WalletsView
import com.monext.sdk.internal.preview.PreviewSamples
import com.monext.sdk.internal.preview.PreviewWrapper

/**
 * Composant qui permet d'afficher la Liste des moyens de paiement disponible à l'acheteur.
 * State : PAYMENT_METHOD_LIST
 */
@Composable
internal fun PaymentMethodsScreen(
    paymentMethodsList: PaymentMethodsList,
    paymentInfo: SessionInfo,
    sessionLoading: Boolean,
    showsGooglePay: Boolean,
    gPayConfig: GooglePayConfiguration,
    allowedPaymentMethods: String,
    onClickGooglePay: () -> Unit,
    onMakePayment: (PaymentAttempt) -> Unit
) {

    var selectedPaymentMethod by rememberSaveable { mutableStateOf<PaymentMethod?>(null) }
    var selectedWallet by rememberSaveable { mutableStateOf<Wallet?>(null) }
    var paymentFormValid by rememberSaveable { mutableStateOf<FormData?>(null) }
    var walletFormValid by rememberSaveable { mutableStateOf<FormData.Wallet?>(null) }

    LaunchedEffect(Unit) {
        if (paymentMethodsList.wallets.isEmpty() && paymentMethodsList.paymentMethods.size == 1) {
            selectedPaymentMethod = paymentMethodsList.paymentMethods.first()
        }
    }

    LaunchedEffect(selectedPaymentMethod) {
        paymentFormValid = null
    }

    val paymentValid = (selectedPaymentMethod != null && paymentFormValid != null)
    val walletValid = (selectedWallet != null && walletFormValid != null)
    val canPay = (walletValid && selectedPaymentMethod == null) || paymentValid

    Column(Modifier.verticalScroll(rememberScrollState())) {

        PaymentMethodsSelector(
            paymentMethodsList.selectablePaymentMethods,
            selectedPaymentMethod,
            onSelect = { paymentMethod ->
                selectedPaymentMethod =
                    if (paymentMethod == selectedPaymentMethod) null
                    else paymentMethod
                paymentFormValid = null
            }
        )

        if (selectedPaymentMethod == null && paymentMethodsList.wallets.isNotEmpty()) {

            LaunchedEffect(selectedWallet) {
                if (selectedWallet == null) {
                    paymentMethodsList.wallets.firstOrNull(Wallet::isDefault)?.let { selectedWallet = it }
                }
            }

            // TODO: implement wallets
            val truncatedWallets = paymentMethodsList.wallets.take(2)
            WalletsView(truncatedWallets) { wallets ->
                for (wallet in wallets) {
                    WalletItem(
                        wallet,
                        isSelected = selectedWallet == wallet,
                        onSelectWallet = {
                            if (selectedWallet != it) {
                                selectedWallet = it
                            }
                        },
                        onWalletFormDataValid = { walletFormValid = it }
                    )
                }
            }
        }

        selectedPaymentMethod?.let { selectedPaymentMethod ->
            PaymentMethodFormContainer(
                selectedPaymentMethod,
                { paymentFormValid = it }
            )
        }

        PayButtonsContainer(
            paymentInfo.formattedAmount,
            selectedPaymentMethod = selectedPaymentMethod,
            canPay = canPay,
            isLoading = sessionLoading,
            showsGooglePay = showsGooglePay,
            gPayConfig = gPayConfig,
            allowedPaymentMethods = allowedPaymentMethods,
            onClickGooglePay = onClickGooglePay
        ) {
            onMakePayment(
                PaymentAttempt(
                    selectedPaymentMethod,
                    paymentFormValid,
                    selectedWallet,
                    walletFormValid
                )
            )
        }
    }
}

@Preview
@Composable
internal fun PaymentMethodsSectionPreview() {
    PreviewWrapper {
        PaymentMethodsScreen(
            paymentMethodsList = PreviewSamples.paymentMethodsList,
            paymentInfo = PreviewSamples.sessionInfo,
            sessionLoading = false,
            showsGooglePay = true,
            gPayConfig = GooglePayConfiguration(),
            allowedPaymentMethods = "",
            {}
        ) {}
    }
}