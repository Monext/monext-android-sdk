package com.monext.sdk.internal.data.sessionstate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FormScriptTest {

    @Test
    fun htmlContent_whenWrapIntoScriptTagTrue_wrapsContentInScriptTag() {
        // Given
        val formScript = FormScript(
            content = "console.log('hello');",
            wrapIntoScriptTag = true,
            formScriptEnum = "CUSTOM"
        )

        // When
        val result = formScript.htmlContent()

        // Then
        assertEquals("<script>console.log('hello');</script>", result)
    }

    @Test
    fun htmlContent_whenWrapIntoScriptTagFalse_returnsContentAsIs() {
        // Given
        val rawContent = "<script src=\"https://c.paypal.com/da/r/fb.js\"></script>"
        val formScript = FormScript(
            content = rawContent,
            wrapIntoScriptTag = false,
            formScriptEnum = "CUSTOM"
        )

        // When
        val result = formScript.htmlContent()

        // Then
        assertEquals(rawContent, result)
    }

    @Test
    fun htmlContent_whenWrapIntoScriptTagNull_returnsContentAsIs() {
        // Given
        val formScript = FormScript(
            content = "alert('hi');",
            wrapIntoScriptTag = null,
            formScriptEnum = "CUSTOM"
        )

        // When
        val result = formScript.htmlContent()

        // Then
        assertEquals("alert('hi');", result)
    }

    @Test
    fun htmlContent_whenContentNull_returnsNull() {
        // Given
        val formScript = FormScript(
            content = null,
            wrapIntoScriptTag = true,
            formScriptEnum = "CUSTOM"
        )

        // When
        val result = formScript.htmlContent()

        // Then
        assertNull(result)
    }
}
