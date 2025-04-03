package com.monext.sdk.internal.data.sessionstate

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = FieldOptionSerializer::class)
internal enum class FormOption {
    CVV,
    ALT_NETWORK,
    EXPI_DATE,
    HOLDER,
    SAVE_PAYMENT_DATA,

    UNKNOWN;
}

internal object FieldOptionSerializer: KSerializer<FormOption> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "com.monext.sdk.internal.data.sessionstate.Option",
            PrimitiveKind.STRING
        )

    override fun serialize(encoder: Encoder, value: FormOption) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): FormOption {
        return try {
            val string = decoder.decodeString()
            FormOption.valueOf(string)
        } catch (_: Throwable) {
            FormOption.UNKNOWN
        }
    }
}