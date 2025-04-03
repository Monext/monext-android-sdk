package com.monext.sdk.internal.data

import android.os.Parcelable
import com.monext.sdk.internal.api.model.request.PaymentParams
import com.monext.sdk.internal.api.model.request.SecuredPaymentParams
import kotlinx.parcelize.Parcelize

@Parcelize
internal sealed interface FormData: Parcelable {

    fun paymentParams(): PaymentParams = PaymentParams()
    fun securedPaymentParams(): SecuredPaymentParams = SecuredPaymentParams()

    data class Card(
        var paymentMethod: PaymentMethod,
        var cardNum: String,
        var expDate: String,
        var cvvNum: String,
        var holder: String,
        var cardNetwork: CardNetwork?,
        var saveCard: Boolean
    ): FormData {

        override fun securedPaymentParams(): SecuredPaymentParams =
            SecuredPaymentParams(pan = cardNum, cvv = cvvNum)

        override fun paymentParams(): PaymentParams =
            PaymentParams(
                network = cardNetwork?.code,
                expirationDate = expDate,
                savePaymentData = saveCard,
                holderName = holder
            )
    }

    data class Wallet(val cvv: String? = null): FormData {
        override fun securedPaymentParams(): SecuredPaymentParams =
            SecuredPaymentParams(cvv = cvv)
    }

    data class PayPal(val saveCard: Boolean): FormData {
        override fun paymentParams(): PaymentParams =
            PaymentParams(savePaymentData = saveCard)
    }
}