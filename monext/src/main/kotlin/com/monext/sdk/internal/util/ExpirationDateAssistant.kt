package com.monext.sdk.internal.util

import androidx.compose.ui.text.input.OffsetMapping
import com.monext.sdk.Appearance
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalField
import java.util.Date
import kotlin.math.floor
import kotlin.math.max

internal object ExpirationDateAssistant: FieldAssistant {

    override val sanitizer = NumericSanitizer
    override val charLimit = 4

    override val formatter = TextFormatter { input -> input.chunked(2).joinToString("/") }

    override val offsetMapping = object: OffsetMapping {

        override fun originalToTransformed(offset: Int): Int {
            val shifted = max(offset - 1, 0)
            val adjustedBy = floor(shifted.toFloat() / 2f).toInt()
            return offset + adjustedBy
        }

        override fun transformedToOriginal(offset: Int): Int {
            val adjustedBy = floor(offset.toFloat() / 3f).toInt()
            return offset - adjustedBy
        }
    }

    override val validator = TextValidator { input, _ ->
        try {
            val date = YearMonth.parse(input, Appearance.cardNetworkFormat)
            if (date.isBefore(YearMonth.now())) {
                ValidationError.InvalidExpiration
            } else {
                null
            }
        } catch (_: Throwable) {
            ValidationError.InvalidFormat
        }
    }
}