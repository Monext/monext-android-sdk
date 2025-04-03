package com.monext.sdk.internal.util

internal fun interface TextFormatter {
    fun format(input: String): String
}

internal val DefaultFormatter = TextFormatter { it }