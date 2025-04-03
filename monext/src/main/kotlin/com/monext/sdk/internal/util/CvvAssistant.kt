package com.monext.sdk.internal.util

internal object CvvAssistant: FieldAssistant {

    override val sanitizer = NumericSanitizer
    override val charLimit: Int = 4
    override val offsetMapping = DefaultOffsetMapping
    override val formatter: TextFormatter = DefaultFormatter
    override val validator = TextValidator { input, issuer ->
        if (issuer == null) {
            when (input.length) {
                in setOf(3, 4) -> null
                else -> ValidationError.InvalidCvv
            }
        } else {
            if (input.length != issuer.cvvLength) {
                ValidationError.InvalidCvv
            } else {
                null
            }
        }
    }
}