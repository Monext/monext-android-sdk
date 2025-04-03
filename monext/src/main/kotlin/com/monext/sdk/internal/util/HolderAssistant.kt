package com.monext.sdk.internal.util

internal object HolderAssistant: FieldAssistant {

    override val sanitizer = DefaultSanitizer
    override val charLimit = null
    override val formatter = DefaultFormatter
    override val offsetMapping = DefaultOffsetMapping

    override val validator = TextValidator { input, issuer ->
        if (input.isBlank()) {
            ValidationError.InvalidFormat
        } else {
            null
        }
    }
}