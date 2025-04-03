package com.monext.sdk.internal.util

internal fun interface TextSanitizer {
    fun sanitize(input: String): String
}

internal val DefaultSanitizer = TextSanitizer { it }

internal val NumericSanitizer = TextSanitizer { input ->
    input.replace(Regex("""\D"""), "")
}