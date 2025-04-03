package com.monext.sdk.internal.api.model.request

import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentRequest(
    val cardCode: String,
    val merchantReturnUrl: String,
    val isEmbeddedRedirectionAllowed: Boolean,
    val paymentParams: PaymentParams,
    val contractNumber: String
)