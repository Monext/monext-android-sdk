package com.monext.sdk.internal.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class SessionInfo(
    @SerialName("PaylineAmountSmallestUnit") val amountSmallestUnit: Long,
    @SerialName("PaylineBuyerIp") val buyerIp: String?,
    @SerialName("PaylineCurrencyCode") val currencyCode: String,
    @SerialName("PaylineCurrencyDigits") val currencyDigits: Int,
    @SerialName("PaylineFormattedAmount") val formattedAmount: String,
    @SerialName("PaylineFormattedOrderAmount") val formattedOrderAmount: String,
    @SerialName("PaylineMerchantCountry") val merchantCountry: String,
    @SerialName("PaylineOrderAmountSmallestUnit") val orderAmountSmallestUnit: Int,
    @SerialName("PaylineOrderDate") val orderDate: String,
    @SerialName("PaylineOrderDeliveryMode") val orderDeliveryMode: String?,
    @SerialName("PaylineOrderDeliveryTime") val orderDeliveryTime: String?,
    @SerialName("PaylineOrderRef") val orderRef: String
)