package com.monext.sdk.internal.api.model.request

import com.monext.sdk.internal.api.model.DeviceInfo
import kotlinx.serialization.Serializable

@Serializable
internal data class SecuredPaymentRequest(
    val cardCode: String,
    val contractNumber: String,
    val deviceInfo: DeviceInfo,
    val isEmbeddedRedirectionAllowed: Boolean,
    val merchantReturnUrl: String,
    val paymentParams: PaymentParams,
    val securedPaymentParams: SecuredPaymentParams
)