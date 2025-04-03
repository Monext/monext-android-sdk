package com.monext.sdk.internal.api.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SecuredPaymentParams(
    @SerialName("PAN") val pan: String? = null,
    @SerialName("CVV") val cvv: String? = null
)