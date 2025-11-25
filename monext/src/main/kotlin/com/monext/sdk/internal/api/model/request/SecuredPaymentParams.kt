package com.monext.sdk.internal.api.model.request

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = SecuredPaymentParams.Serializer::class)
internal data class SecuredPaymentParams(
    val pan: String? = null,
    val cvv: String? = null,
    val additionalParams: Map<String, String?>? = null
) {
    fun isEmpty(): Boolean = pan.isNullOrBlank() && cvv.isNullOrBlank() && additionalParams.isNullOrEmpty()

    object Serializer : KSerializer<SecuredPaymentParams> {
        override val descriptor: SerialDescriptor = JsonObject.serializer().descriptor

        private val KNOWN_KEYS = setOf("PAN", "CVV")

        override fun serialize(encoder: Encoder, value: SecuredPaymentParams) {
            require(encoder is JsonEncoder) { "This serializer only works with Json" }
            val json = buildJsonObject {
                value.pan?.let { put("PAN", JsonPrimitive(it)) }
                value.cvv?.let { put("CVV", JsonPrimitive(it)) }

                value.additionalParams?.forEach { (k, v) ->
                    if (k !in KNOWN_KEYS) {
                        put(k, v?.let { JsonPrimitive(it) } ?: JsonNull)
                    }
                }
            }
            encoder.encodeJsonElement(json)
        }

        override fun deserialize(decoder: Decoder): SecuredPaymentParams {
            require(decoder is JsonDecoder) { "This serializer only works with Json" }
            val obj = decoder.decodeJsonElement().jsonObject

            val pan = obj["PAN"]?.jsonPrimitive?.contentOrNull
            val cvv = obj["CVV"]?.jsonPrimitive?.contentOrNull

            val additional = obj.filterKeys { it !in KNOWN_KEYS }
                .mapValues { it.value.jsonPrimitive.contentOrNull }

            return SecuredPaymentParams(
                pan = pan,
                cvv = cvv,
                additionalParams = additional.ifEmpty { null }
            )
        }
    }
}