package com.monext.sdk.internal.util

import androidx.compose.ui.text.input.OffsetMapping
import com.monext.sdk.internal.ext.passesLuhnCheck
import kotlin.math.floor
import kotlin.math.max

internal object CreditCardAssistant: FieldAssistant {

    override val sanitizer = NumericSanitizer
    override val charLimit = 19

    override val offsetMapping = object: OffsetMapping {

        override fun originalToTransformed(offset: Int): Int {
            val shifted = max(offset - 1, 0)
            val adjustedBy = floor(shifted.toFloat() / 4f).toInt()
            return offset + adjustedBy
        }

        override fun transformedToOriginal(offset: Int): Int {
            val adjustedBy = floor(offset.toFloat() / 5f).toInt()
            return offset - adjustedBy
        }
    }

    override val formatter = TextFormatter { input -> input.chunked(4).joinToString(" ") }

    override val validator = TextValidator { input, issuer ->
        if (issuer == null) return@TextValidator ValidationError.UnknownCardType
        if (!issuer.pattern.containsMatchIn(input)) return@TextValidator ValidationError.InvalidCardNumber
        if (input.length !in issuer.validLengths) return@TextValidator ValidationError.InvalidCardNumber
        if (!input.passesLuhnCheck()) return@TextValidator ValidationError.InvalidCardNumber
        null
    }
}

