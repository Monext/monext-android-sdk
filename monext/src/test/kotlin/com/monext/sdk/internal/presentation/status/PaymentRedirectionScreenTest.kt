package com.monext.sdk.internal.presentation.status

import com.monext.sdk.internal.data.sessionstate.RedirectionData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import kotlin.test.Test

class PostDataTest {

    @Test
    fun postData_should_format_map_correctly_and_encode_as_byte_array() {
        // Arrange
        val requestFields = mapOf("foo" to "bar", "baz" to "qux")
        val data = RedirectionData(
            requestType = "POST",
            requestUrl = "https://3ds-acs.test.modirum.com/mdpayacs/3ds-method",
            requestFields = requestFields,
            iframeEmbeddable = true,
            iframeHeight = 1,
            iframeWidth = 1,
            timeoutInMs = 10_000,
            hasPartnerLogo = true,
            partnerLogoKey = "cb",
            isCompletionMethod = true
        )

        // Act
        val postData = data.requestFields?.entries
            ?.joinToString("&") { (k, v) -> "$k=$v" }
            ?.toByteArray()

        // Assert
        val expectedString = "foo=bar&baz=qux"
        assertNotNull(postData)
        assertEquals(expectedString, postData?.toString(Charsets.UTF_8))
    }

    @Test
    fun postData_should_be_null_when_requestFields_is_null() {
        // Arrange
        val requestFields = null
        val data = RedirectionData(
            requestType = "POST",
            requestUrl = "https://3ds-acs.test.modirum.com/mdpayacs/3ds-method",
            requestFields = requestFields,
            iframeEmbeddable = true,
            iframeHeight = 1,
            iframeWidth = 1,
            timeoutInMs = 10_000,
            hasPartnerLogo = true,
            partnerLogoKey = "cb",
            isCompletionMethod = true
        )

        // Act
        val postData = data.requestFields?.entries
            ?.joinToString("&") { (k, v) -> "$k=$v" }
            ?.toByteArray()

        // Assert
        assertNull(postData)
    }
}