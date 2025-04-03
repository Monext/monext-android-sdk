package com.monext.sdk.internal.api.model.request

import com.monext.sdk.internal.data.sessionstate.PaymentMethodCardCode
import kotlinx.serialization.Serializable

@Serializable
internal data class WalletPaymentRequest(
    val cardCode: PaymentMethodCardCode,
    val index: Int,
    val isEmbeddedRedirectionAllowed: Boolean,
    val merchantReturnUrl: String,
    val securedPaymentParams: SecuredPaymentParams
)