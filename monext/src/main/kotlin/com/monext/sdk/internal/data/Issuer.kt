package com.monext.sdk.internal.data

import com.monext.sdk.internal.api.model.PaymentMethodCardCode
import com.monext.sdk.internal.data.sessionstate.Wallet

internal enum class Issuer {

    VISA, MASTERCARD, AMEX
    ;

    val pattern: Regex
        get() = when (this) {
            VISA -> Regex("""^4""")
            MASTERCARD -> Regex("""^5[1-5]|^2[2-7]""")
            AMEX -> Regex("""^3[47]""")
        }

    val validLengths: Set<Int>
        get() = when (this) {
            VISA -> setOf(13, 16, 19)
            MASTERCARD -> setOf(16)
            AMEX -> setOf(15)
        }

    val cvvLength: Int
        get() = when (this) {
            VISA, MASTERCARD -> 3
            AMEX -> 4
        }

    val associatedCardCodes: List<String>
        get() = when (this) {
            VISA -> listOf(PaymentMethodCardCode.CB, PaymentMethodCardCode.MCVISA)
            MASTERCARD -> listOf(PaymentMethodCardCode.CB, PaymentMethodCardCode.MCVISA)
            AMEX -> listOf(PaymentMethodCardCode.AMEX)
        }

    fun paymentMethod(paymentMethods: List<PaymentMethod>): PaymentMethod? =
        paymentMethods.firstOrNull {
            it.cardCode in associatedCardCodes
        }

    companion object {
        fun lookupIssuer(cardNum: String): Issuer? {
            for (issuer in entries) {
                if (issuer.pattern.containsMatchIn(cardNum)) {
                    return issuer
                }
            }
            return null
        }

        fun lookupIssuer(wallet: Wallet): Issuer? {
            for (issuer in entries) {
                if (issuer.associatedCardCodes.contains(wallet.cardCode)) {
                    return issuer
                }
            }
            return null
        }
    }
}