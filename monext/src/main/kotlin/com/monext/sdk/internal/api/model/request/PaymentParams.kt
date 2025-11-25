package com.monext.sdk.internal.api.model.request

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.*

@Serializable(with = PaymentParams.Serializer::class)
internal data class PaymentParams(
    val network: String? = null,
    val expirationDate: String? = null,
    val savePaymentData: Boolean? = null,
    val holderName: String? = null,
    val googlePayData: String? = null,
    var sdkContextData: String? = null,
    val additionalParams: Map<String, String?>? = null
) {
    object Serializer : KSerializer<PaymentParams> {
        override val descriptor: SerialDescriptor = JsonObject.serializer().descriptor

        private val KNOWN_KEYS = setOf(
            "NETWORK", "EXPI_DATE", "SAVE_PAYMENT_DATA", "HOLDER", "data", "SDK_CONTEXT_DATA"
        )

        override fun serialize(encoder: Encoder, value: PaymentParams) {
            require(encoder is JsonEncoder) { "This serializer only works with Json" }
            val json = buildJsonObject {
                value.network?.let { put("NETWORK", JsonPrimitive(it)) }
                value.expirationDate?.let { put("EXPI_DATE", JsonPrimitive(it)) }
                value.savePaymentData?.let { put("SAVE_PAYMENT_DATA", JsonPrimitive(it)) }
                value.holderName?.let { put("HOLDER", JsonPrimitive(it)) }
                value.googlePayData?.let { put("data", JsonPrimitive(it)) }
                value.sdkContextData?.let { put("SDK_CONTEXT_DATA", JsonPrimitive(it)) }

                value.additionalParams?.forEach { (k, v) ->
                    // n'écrase pas les clés connues
                    if (k !in KNOWN_KEYS) {
                        put(k, v?.let { JsonPrimitive(it) } ?: JsonNull)
                    }
                }
            }
            encoder.encodeJsonElement(json)
        }

        override fun deserialize(decoder: Decoder): PaymentParams {
            require(decoder is JsonDecoder) { "This serializer only works with Json" }
            val jsonElement = decoder.decodeJsonElement()
            val obj = jsonElement.jsonObject

            val network = obj["NETWORK"]?.jsonPrimitive?.contentOrNull
            val exp = obj["EXPI_DATE"]?.jsonPrimitive?.contentOrNull
            val save = obj["SAVE_PAYMENT_DATA"]?.jsonPrimitive?.booleanOrNull
            val holder = obj["HOLDER"]?.jsonPrimitive?.contentOrNull
            val data = obj["data"]?.jsonPrimitive?.contentOrNull
            val sdk = obj["SDK_CONTEXT_DATA"]?.jsonPrimitive?.contentOrNull

            val additional = obj.filterKeys { it !in KNOWN_KEYS }
                .mapValues { it.value.jsonPrimitive.contentOrNull }

            return PaymentParams(
                network = network,
                expirationDate = exp,
                savePaymentData = save,
                holderName = holder,
                googlePayData = data,
                sdkContextData = sdk,
                additionalParams = additional.ifEmpty { null }
            )
        }
    }
}