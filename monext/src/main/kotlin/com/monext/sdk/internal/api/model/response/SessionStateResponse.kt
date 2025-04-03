package com.monext.sdk.internal.api.model.response

import com.monext.sdk.PaymentResult
import com.monext.sdk.internal.data.sessionstate.PaymentFailure
import com.monext.sdk.internal.data.sessionstate.PaymentMethodsList
import com.monext.sdk.internal.data.sessionstate.PaymentRedirectNoResponse
import com.monext.sdk.internal.data.sessionstate.PaymentSuccess
import com.monext.sdk.internal.api.model.PointOfSaleAddress
import com.monext.sdk.internal.api.model.SessionInfo
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
internal data class SessionState(
    val token: String,
    val type: SessionStateType,
    val automaticRedirectAtSessionsEnd: Boolean?,
    val cancelUrl: String?,
    val creationDate: String? = null,
    val info: SessionInfo? = null,
    val isSandbox: Boolean?,
    val language: String?,
    val pointOfSale: String?,
    val pointOfSaleAddress: PointOfSaleAddress? = null,
    val returnUrl: String?,
    val paymentMethodsList: PaymentMethodsList? = null,
    val paymentRedirectNoResponse: PaymentRedirectNoResponse? = null,
    val paymentSuccess: PaymentSuccess? = null,
    val paymentFailure: PaymentFailure? = null
)

@Serializable(with = SessionStateTypeSerializer::class)
internal enum class SessionStateType {

    PAYMENT_METHODS_LIST,
    PAYMENT_REDIRECT_NO_RESPONSE,
    PAYMENT_SUCCESS,
    PAYMENT_FAILURE,
    PAYMENT_FAILURE_WITH_RETRY,
    PAYMENT_CANCELED,
    TOKEN_EXPIRED,

    UNKNOWN;

    fun toTransactionState(): PaymentResult.TransactionState? =
        when (this) {
            PAYMENT_METHODS_LIST, PAYMENT_REDIRECT_NO_RESPONSE -> PaymentResult.TransactionState.PAYMENT_INCOMPLETE
            PAYMENT_SUCCESS -> PaymentResult.TransactionState.PAYMENT_SUCCESS
            PAYMENT_FAILURE, PAYMENT_FAILURE_WITH_RETRY -> PaymentResult.TransactionState.PAYMENT_FAILURE
            PAYMENT_CANCELED -> PaymentResult.TransactionState.PAYMENT_CANCELED
            TOKEN_EXPIRED -> PaymentResult.TransactionState.TOKEN_EXPIRED
            // TODO: Determine all possible states
            else -> null
        }
}

internal object SessionStateTypeSerializer: KSerializer<SessionStateType> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("com.monext.sdk.internal.api.model.response.SessionStateType", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: SessionStateType) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): SessionStateType {
        return try {
            val string = decoder.decodeString()
            SessionStateType.valueOf(string)
        } catch (_: Throwable) {
            SessionStateType.UNKNOWN
        }
    }
}