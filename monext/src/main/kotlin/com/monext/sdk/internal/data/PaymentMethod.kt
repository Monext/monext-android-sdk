package com.monext.sdk.internal.data

import android.os.Parcelable
import com.monext.sdk.internal.data.sessionstate.PaymentMethodCardCode
import com.monext.sdk.internal.data.sessionstate.PaymentMethodData
import kotlinx.parcelize.Parcelize

internal interface PaymentMethodType {

    val cardCode: PaymentMethodCardCode?

    val isCard: Boolean
        get() = cardCode in listOf(
            PaymentMethodCardCode.CB,
            PaymentMethodCardCode.MCVISA,
            PaymentMethodCardCode.AMEX
        )
}

@Parcelize
internal sealed interface PaymentMethod: PaymentMethodType, Parcelable {

    override val cardCode: PaymentMethodCardCode?
        get() = data?.cardCode

    val data: PaymentMethodData?

    data class Cards(
        val paymentMethods: List<PaymentMethod>,
        override val data: PaymentMethodData? = null
    ): PaymentMethod

    data class CB(override val data: PaymentMethodData): PaymentMethod
    data class MCVisa(override val data: PaymentMethodData): PaymentMethod
    data class Amex(override val data: PaymentMethodData): PaymentMethod
    data class GooglePay(override val data: PaymentMethodData): PaymentMethod
    data class PayPal(override val data: PaymentMethodData): PaymentMethod
    data class Ideal(override val data: PaymentMethodData): PaymentMethod

    data class Unsupported(override val data: PaymentMethodData): PaymentMethod

    companion object {
        fun fromData(data: PaymentMethodData): PaymentMethod? =
            when(data.cardCode) {
                PaymentMethodCardCode.CB -> CB(data)
                PaymentMethodCardCode.MCVISA -> MCVisa(data)
                PaymentMethodCardCode.AMEX -> Amex(data)
                PaymentMethodCardCode.PAYPAL -> PayPal(data)
                PaymentMethodCardCode.IDEAL -> Ideal(data)
                PaymentMethodCardCode.GOOGLE_PAY -> GooglePay(data)
                PaymentMethodCardCode.UNSUPPORTED -> Unsupported(data)
                else -> null
            }
    }
}