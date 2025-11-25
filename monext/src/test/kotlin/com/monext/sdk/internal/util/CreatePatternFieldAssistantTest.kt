package com.monext.sdk.internal.util


import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertNull
import kotlin.test.Test
import kotlin.test.assertEquals

class CreatePatternFieldAssistantTest {

    @Test
    fun test_required_field_with_empty_input_returns_error() {
        val assistant = createPatternFieldAssistant(
            patternString = null,
            required = true,
            requiredErrorMessage = "Champ obligatoire"
        )

        val result = assistant.validator?.validate("", null)

        assertTrue(result is ValidationError.Custom)
        assertEquals("Champ obligatoire", result?.rawMessage)
    }

    @Test
    fun test_required_field_with_default_message() {
        val assistant = createPatternFieldAssistant(
            patternString = null,
            required = true
        )

        val result = assistant.validator?.validate("   ", null)

        assertTrue(result is ValidationError.Custom)
        assertEquals("This field is required", result?.rawMessage)
    }

    @Test
    fun test_non_required_field_with_empty_input_returns_null() {
        val assistant = createPatternFieldAssistant(
            patternString = null,
            required = false
        )

        val result = assistant.validator?.validate("", null)

        assertNull(result)
    }

    @Test
    fun test_pattern_matches_returns_null() {
        val assistant = createPatternFieldAssistant(
            patternString = "^[0-9]{3}$",
            required = false
        )

        val result = assistant.validator?.validate("123", null)

        assertNull(result)
    }

    @Test
    fun test_pattern_does_not_match_returns_error() {
        val assistant = createPatternFieldAssistant(
            patternString = "^[0-9]{3}$",
            required = false,
            validationErrorMessage = "Doit contenir 3 chiffres"
        )

        val result = assistant. validator?.validate("abc", null)

        assertTrue(result is ValidationError.Custom)
        assertEquals("Doit contenir 3 chiffres", result?.rawMessage)
    }

    @Test
    fun test_invalid_regex_pattern_is_ignored() {
        val assistant = createPatternFieldAssistant(
            patternString = "[invalid(regex",
            required = false
        )

        val result = assistant. validator?.validate("any input", null)

        assertNull(result)
    }

    @Test
    fun test_partial_match_enabled_succeeds() {
        val assistant = createPatternFieldAssistant(
            patternString = "[0-9]{3}",
            required = false,
            allowPartialMatch = true
        )

        val result = assistant. validator?.validate("abc123def", null)

        assertNull(result)
    }

    @Test
    fun test_partial_match_disabled_fails() {
        val assistant = createPatternFieldAssistant(
            patternString = "[0-9]{3}",
            required = false,
            allowPartialMatch = false
        )

        val result = assistant.validator?.validate("abc123def", null)

        assertTrue(result is ValidationError.Custom)
    }

    @Test
    fun test_input_is_trimmed_before_validation() {
        val assistant = createPatternFieldAssistant(
            patternString = "^[a-z]+$",
            required = false
        )

        val result = assistant.validator?.validate("  hello  ", null)

        assertNull(result)
    }

    @Test
    fun test_char_limit_and_sanitizer_are_set_correctly() {
        val customSanitizer = TextSanitizer { it.uppercase() }
        val assistant = createPatternFieldAssistant(
            patternString = null,
            charLimit = 10,
            sanitizer = customSanitizer
        )

        assertEquals(10, assistant. charLimit)
        assertEquals("HELLO", assistant. sanitizer.sanitize("hello"))
    }
}