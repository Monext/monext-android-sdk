package com.monext.sdk.internal.data.sessionstate

import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentRedirectNoResponse(
    val cardCode: PaymentMethodCardCode,
    val contractNumber: String,
    val redirectionData: RedirectionData,
    val walletCardIndex: Int?
)

@Serializable
internal data class RedirectionData(
    val hasPartnerLogo: Boolean,
    val partnerLogoKey: String?,
    val iframeEmbeddable: Boolean,
    val iframeHeight: Int,
    val iframeWidth: Int,
    val isCompletionMethod: Boolean,
    val requestType: String,
    val requestUrl: String,
    val timeoutInMs: Int,
    val requestFields: Map<String, String>
)