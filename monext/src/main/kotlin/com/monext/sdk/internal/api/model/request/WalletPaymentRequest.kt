package com.monext.sdk.internal.api.model.request

import kotlinx.serialization.Serializable

@Serializable
internal data class WalletPaymentRequest(
    val cardCode: String,
    val index: Int,
    val isEmbeddedRedirectionAllowed: Boolean,
    val merchantReturnUrl: String,
    val paymentParams: PaymentParams,
    val securedPaymentParams: SecuredPaymentParams
)