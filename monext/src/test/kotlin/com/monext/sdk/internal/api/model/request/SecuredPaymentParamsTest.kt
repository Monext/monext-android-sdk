package com.monext.sdk.internal.api.model.request

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SecuredPaymentParamsTest {

    private val json = Json { encodeDefaults = false }

    @Test
    fun handle_test_serialize_includes_known_keys_and_additional_keys_null_preserved() {
        val params = SecuredPaymentParams(
            pan = "4111111111111111",
            cvv = "123",
            additionalParams = mapOf(
                "CUSTOM" to "custom-value",
                "NULLVAL" to null,
                // This key is known and should NOT overwrite the top-level PAN key
                "PAN" to "SHOULD_BE_IGNORED"
            )
        )

        val encoded = json.encodeToString(SecuredPaymentParams.Serializer, params)
        val element = json.parseToJsonElement(encoded)
        val obj = element.jsonObject

        // Known keys
        assertEquals("4111111111111111", obj["PAN"]!!.jsonPrimitive.content)
        assertEquals("123", obj["CVV"]!!.jsonPrimitive.content)

        // Additional keys
        assertEquals("custom-value", obj["CUSTOM"]!!.jsonPrimitive.content)
        assertTrue(obj["NULLVAL"] is JsonNull)

        // Ensure the additional "PAN" did NOT overwrite the known key
        assertEquals("4111111111111111", obj["PAN"]!!.jsonPrimitive.content)
    }

    @Test
    fun handle_test_deserialize_reconstructs_fields_and_additional_params_including_nulls() {
        val raw = """
            {
              "PAN": "5500000000000004",
              "CVV": "999",
              "EXTRA1": "v1",
              "EXTRA_NULL": null
            }
        """.trimIndent()

        val params = json.decodeFromString(SecuredPaymentParams.Serializer, raw)

        assertEquals("5500000000000004", params.pan)
        assertEquals("999", params.cvv)

        val additional = params.additionalParams
        assertNotNull(additional)
        assertEquals("v1", additional!!["EXTRA1"])
        // null in JSON -> null in map
        assertTrue(additional.containsKey("EXTRA_NULL"))
        assertNull(additional["EXTRA_NULL"])
    }

    @Test
    fun handle_test_roundtrip_encode_decode_preserves_data() {
        val params = SecuredPaymentParams(
            pan = "340000000000009",
            cvv = "321",
            additionalParams = mapOf("A" to "1", "B" to null)
        )

        val encoded = json.encodeToString(SecuredPaymentParams.Serializer, params)
        val decoded = json.decodeFromString(SecuredPaymentParams.Serializer, encoded)

        assertEquals(params.pan, decoded.pan)
        assertEquals(params.cvv, decoded.cvv)

        assertNotNull(decoded.additionalParams)
        assertEquals("1", decoded.additionalParams!!["A"])
        assertTrue(decoded.additionalParams!!.containsKey("B"))
        assertNull(decoded.additionalParams!!["B"])
    }

    @Test
    fun handle_test_is_empty_true_when_all_missing() {
        val params = SecuredPaymentParams()
        assertTrue(params.isEmpty())
    }

    @Test
    fun handle_test_is_empty_false_when_has_values() {
        val p1 = SecuredPaymentParams(pan = "pan")
        assertFalse(p1.isEmpty())

        val p2 = SecuredPaymentParams(cvv = "cvv")
        assertFalse(p2.isEmpty())

        val p3 = SecuredPaymentParams(additionalParams = mapOf("X" to "1"))
        assertFalse(p3.isEmpty())
    }
}