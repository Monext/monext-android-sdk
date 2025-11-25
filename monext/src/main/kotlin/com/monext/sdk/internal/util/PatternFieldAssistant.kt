package com.monext.sdk.internal.util

import androidx.compose.ui.text.input.OffsetMapping

internal fun createPatternFieldAssistant(
    patternString: String?,
    required: Boolean = false,
    validationErrorMessage: String? = null,
    requiredErrorMessage: String? = null,
    charLimit: Int = Int.MAX_VALUE,
    sanitizer: TextSanitizer = DefaultSanitizer,
    formatter: TextFormatter = TextFormatter { it },
    offsetMapping: OffsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int = offset
        override fun transformedToOriginal(offset: Int): Int = offset
    },
    // If you prefer to allow partial matches during typing, set to true to use containsMatchIn
    allowPartialMatch: Boolean = false
): FieldAssistant {
    val pattern = patternString?.let {
        try {
            it.toRegex()
        } catch (_: Throwable) {
            null
        }
    }

    return object : FieldAssistant {
        override val sanitizer = sanitizer
        override val charLimit = charLimit
        override val formatter = formatter
        override val offsetMapping = offsetMapping

        override val validator = TextValidator { input, _ ->
            val trimmed = input.trim()

            // required check
            if (required && trimmed.isEmpty()) {
                val msg = requiredErrorMessage ?: "This field is required"
                return@TextValidator ValidationError.Custom(msg)
            }

            // if no pattern provided => valid
            if (pattern == null) return@TextValidator null

            val matches = if (allowPartialMatch) pattern.containsMatchIn(trimmed) else pattern.matches(trimmed)
            if (!matches) {
                val msg = validationErrorMessage ?: "Invalid format"
                return@TextValidator ValidationError.Custom(msg)
            }

            // OK
            null
        }
    }
}