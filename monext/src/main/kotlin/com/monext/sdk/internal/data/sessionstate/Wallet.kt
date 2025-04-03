package com.monext.sdk.internal.data.sessionstate

import android.os.Parcelable
import com.monext.sdk.internal.data.PaymentMethodType
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
internal data class Wallet(
    val additionalData: AdditionalData,
    override val cardCode: PaymentMethodCardCode,
    val cardType: PaymentMethodCardCode,
    val confirm: List<FormOption>,
    val customLogoRatio: Int,
    val expiredMore6Months: Boolean,
    val hasCustomLogo: Boolean,
    val hasCustomLogoBase64: Boolean,
    val hasCustomLogoUrl: Boolean,
    val hasSpecificDisplay: Boolean,
    val index: Int,
    val isDefault: Boolean,
    val isExpired: Boolean,
    val isPmAPI: Boolean,
    val options: List<FormOption>?
): PaymentMethodType, Parcelable

