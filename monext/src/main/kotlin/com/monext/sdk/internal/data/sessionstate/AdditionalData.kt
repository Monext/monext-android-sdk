package com.monext.sdk.internal.data.sessionstate

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
internal data class AdditionalData(
    @SerialName("MERCHANT_CAPABILITIES") val merchantCapabilities: List<String>?,
    @SerialName("NETWORKS") val networks: List<String>?,
    @SerialName("APPLE_PAY_MERCHANT_ID") val applePayMerchantId: String?,
    @SerialName("APPLE_PAY_MERCHANT_NAME") val applePayMerchantName: String?,
    @SerialName("SAVE_PAYMENT_DATA_CHECKED") val savePaymentDataChecked: Boolean?,
    @SerialName("EMAIL") val email: String?,
    @SerialName("DATE") val date: String?,
    @SerialName("HOLDER") val holder: String?,
    @SerialName("PAN") val pan: String?
): Parcelable