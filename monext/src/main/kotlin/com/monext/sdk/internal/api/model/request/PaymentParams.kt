package com.monext.sdk.internal.api.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentParams(
    @SerialName("NETWORK") var network: String? = null,
    @SerialName("EXPI_DATE") var expirationDate: String? = null,
    @SerialName("SAVE_PAYMENT_DATA") var savePaymentData: Boolean? = null,
    @SerialName("HOLDER") var holderName: String? = null,
    @SerialName("data") var googlePayData: String? = null,
    @SerialName("SDK_CONTEXT_DATA") var sdkContextData: String? = null
)