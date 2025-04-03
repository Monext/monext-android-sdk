package com.monext.sdk.internal.ext

import android.net.Uri
import java.net.URL

internal fun Uri.asURL(): URL = URL(toString())

// Luhn's Algorithm for card correctness validation
internal fun String.passesLuhnCheck(): Boolean {

    val reversedDigits = reversed().mapNotNull { it.digitToInt() }
    var sum = 0
    var isOdd = true

    for (digit in reversedDigits) {
        if (isOdd) {
            sum += digit
        } else {
            val doubled = digit * 2
            sum += if (doubled > 9) (doubled - 9) else doubled
        }
        isOdd = !isOdd
    }

    return sum % 10 == 0
}