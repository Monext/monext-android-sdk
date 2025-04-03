package com.monext.sdk.internal.data.sessionstate

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

// The identifier for this payment method
@Serializable(with = PaymentMethodCardCodeSerializer::class)
internal enum class PaymentMethodCardCode {

    CB, MCVISA, VISA, MASTERCARD, AMEX,

    PAYPAL,
    IDEAL,
    GOOGLE_PAY,

    UNSUPPORTED;
}

internal object PaymentMethodCardCodeSerializer: KSerializer<PaymentMethodCardCode> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "com.monext.sdk.internal.data.sessionstate.PaymentMethodCardCode",
            PrimitiveKind.STRING
        )

    override fun serialize(encoder: Encoder, value: PaymentMethodCardCode) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): PaymentMethodCardCode {
        return try {
            val string = decoder.decodeString()
            PaymentMethodCardCode.valueOf(string)
        } catch (_: Throwable) {
            PaymentMethodCardCode.UNSUPPORTED
        }
    }
}