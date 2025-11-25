package com.monext.sdk.internal.api.model.request

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PaymentParamsSerializerTest {

    private val json = Json { encodeDefaults = false }

    @Test
    fun handle_test_serialize_includes_known_keys_and_additional_keys_null_preserved() {
        val params = PaymentParams(
            network = "VISA",
            expirationDate = "12/34",
            savePaymentData = true,
            holderName = "John Doe",
            googlePayData = "gpay-payload",
            sdkContextData = "sdk-ctx",
            additionalParams = mapOf(
                "CUSTOM" to "custom-value",
                "NULLVAL" to null,
                // This key is known and should NOT overwrite the top-level NETWORK key
                "NETWORK" to "SHOULD_BE_IGNORED"
            )
        )

        val encoded = json.encodeToString(PaymentParams.Serializer, params)
        val element = json.parseToJsonElement(encoded)
        val obj = element.jsonObject

        // Known keys
        assertEquals("VISA", obj["NETWORK"]!!.jsonPrimitive.content)
        assertEquals("12/34", obj["EXPI_DATE"]!!.jsonPrimitive.content)
        assertEquals(true, obj["SAVE_PAYMENT_DATA"]!!.jsonPrimitive.boolean)
        assertEquals("John Doe", obj["HOLDER"]!!.jsonPrimitive.content)
        assertEquals("gpay-payload", obj["data"]!!.jsonPrimitive.content)
        assertEquals("sdk-ctx", obj["SDK_CONTEXT_DATA"]!!.jsonPrimitive.content)

        // Additional keys
        assertEquals("custom-value", obj["CUSTOM"]!!.jsonPrimitive.content)
        assertTrue(obj["NULLVAL"] is JsonNull)

        // Ensure the additional "NETWORK" did NOT overwrite the known key
        assertEquals("VISA", obj["NETWORK"]!!.jsonPrimitive.content)
    }

    @Test
    fun handle_test_deserialize_reconstructs_fields_and_additional_params_including_nulls() {
        val raw = """
            {
              "NETWORK": "MASTERCARD",
              "EXPI_DATE": "01/30",
              "SAVE_PAYMENT_DATA": false,
              "HOLDER": "Alice",
              "data": "gpay",
              "SDK_CONTEXT_DATA": "ctx-123",
              "EXTRA1": "v1",
              "EXTRA_NULL": null
            }
        """.trimIndent()

        val params = json.decodeFromString(PaymentParams.Serializer, raw)

        assertEquals("MASTERCARD", params.network)
        assertEquals("01/30", params.expirationDate)
        assertEquals(false, params.savePaymentData)
        assertEquals("Alice", params.holderName)
        assertEquals("gpay", params.googlePayData)
        assertEquals("ctx-123", params.sdkContextData)

        val additional = params.additionalParams
        assertNotNull(additional)
        assertEquals("v1", additional!!["EXTRA1"])
        // null in JSON -> null in map
        assertTrue(additional.containsKey("EXTRA_NULL"))
        assertNull(additional["EXTRA_NULL"])
    }

    @Test
    fun handle_test_roundtrip_encode_decode_preserves_data() {
        val params = PaymentParams(
            network = "AMEX",
            expirationDate = "11/29",
            savePaymentData = null,
            holderName = null,
            googlePayData = null,
            sdkContextData = "ctx-rt",
            additionalParams = mapOf("A" to "1", "B" to null)
        )

        val encoded = json.encodeToString(PaymentParams.Serializer, params)
        val decoded = json.decodeFromString(PaymentParams.Serializer, encoded)

        assertEquals(params.network, decoded.network)
        assertEquals(params.expirationDate, decoded.expirationDate)
        assertEquals(params.savePaymentData, decoded.savePaymentData)
        assertEquals(params.holderName, decoded.holderName)
        assertEquals(params.googlePayData, decoded.googlePayData)
        assertEquals(params.sdkContextData, decoded.sdkContextData)

        assertNotNull(decoded.additionalParams)
        assertEquals("1", decoded.additionalParams!!["A"])
        assertTrue(decoded.additionalParams!!.containsKey("B"))
        assertNull(decoded.additionalParams!!["B"])
    }
}