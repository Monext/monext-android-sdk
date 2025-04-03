package com.monext.sdk.internal.data.sessionstate

import android.os.Parcelable
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.PaymentMethod.Cards
import com.monext.sdk.internal.presentation.paymentmethods.GooglePayRequestData
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class PaymentMethodsList(
    val isOriginalCreditTransfer: Boolean,
    val needsDeviceFingerprint: Boolean,
    @SerialName("paymentMethods") val paymentMethodsData: List<PaymentMethodData>,
    val scoringNeeded: Boolean?,
    val sensitiveInputContentMasked: Boolean,
    val shouldChangePaymentMethodPosition: Boolean,
    val wallets: List<Wallet>
) {

    // "Card" payment methods are grouped into a common object and presented first
    val paymentMethods: List<PaymentMethod>
        get() {
            val group = mutableListOf<PaymentMethod>()
            val cards = mutableListOf<PaymentMethod>()

            for (data in paymentMethodsData) {
                PaymentMethod.fromData(data)?.let {
                    if (it is PaymentMethod.Unsupported) return@let
                    if (it.isCard) { cards.add(it) }
                    else { group.add(it) }
                }
            }
            if (cards.isNotEmpty()) {
                group.add(0, Cards(cards))
            }
            return group
        }

    // GooglePay availability is determined by the system
    var selectablePaymentMethods: List<PaymentMethod> = paymentMethods.filter {
        it !is PaymentMethod.GooglePay
    }
}

@Parcelize
@Serializable
internal data class PaymentMethodData(

    /**
    The payment method identifier.
    - Examples:
    - CB
    - VISA
    - PAYPAL
    - IDEAL
    - etc.
     */
    val cardCode: PaymentMethodCardCode?,

//    val confirm: List<FormOption>?,

    /// The contract identifier
    val contractNumber: String?,

    val disabled: Boolean?,

    val hasForm: Boolean?,

    val hasLogo: Boolean?,

    val isIsolated: Boolean?,

    // Various options for diplaying fields of the card form
    val options: List<FormOption>?,

    val paymentMethodAction: Int?,

    // ???
//    let paymentParamsToBeControlled: [Any]

    val additionalData: AdditionalData,

    val requestContext: RequestContext?,

    val shouldBeInTopPosition: Boolean?,

    val state: String?
) : Parcelable

@Parcelize
@Serializable
internal data class RequestContext(
    val requestData: RequestData?
): Parcelable

@Parcelize
@Serializable
internal data class RequestData(
    @SerialName("GOOGLE_PAY_ALLOWED_AUTH_METHOD")
    val _googlePayAllowedAuthMethod: String?,
    @SerialName("GOOGLE_PAY_MERCHANT_NAME")
    val googlePayMerchantName: String?,
//    @SerialName("GOOGLE_PAY_MERCHANT_ORIGIN")
//    val googlePayMerchantOrigin: String,
//    @SerialName("GOOGLE_PAY_BUTTON_COLOR")
//    val googlePayButtonColor: String,
//    @SerialName("GOOGLE_PAY_BUTTON_TYPE")
//    val googlePayButtonType: String,
    @SerialName("GOOGLE_PAY_ALLOWED_NETWORKS")
    val _googlePayAllowedNetworks: String?,
    @SerialName("GOOGLE_PAY_MERCHANT_ID")
    val googlePayMerchantId: String?
): Parcelable {

    fun googlePayAllowedAuthMethod(): List<String> =
        try {
            Json.decodeFromString(_googlePayAllowedAuthMethod ?: "")
        } catch (_: Throwable) {
            emptyList()
        }

    fun googlePayAllowedNetworks(): List<String> =
        try {
            Json.decodeFromString(_googlePayAllowedNetworks ?: "")
        } catch (_: Throwable) {
            emptyList()
        }
}

