package com.monext.sdk.internal.util

import androidx.compose.ui.text.input.OffsetMapping

internal interface FieldAssistant {
    val sanitizer: TextSanitizer
    val charLimit: Int?
    val formatter: TextFormatter
    val offsetMapping: OffsetMapping
    val validator: TextValidator?
}

internal object DefaultOffsetMapping: OffsetMapping {
    override fun originalToTransformed(offset: Int): Int = offset
    override fun transformedToOriginal(offset: Int): Int = offset
}