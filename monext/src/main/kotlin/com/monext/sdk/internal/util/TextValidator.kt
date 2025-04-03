package com.monext.sdk.internal.util

import android.content.Context
import com.monext.sdk.R
import com.monext.sdk.internal.data.Issuer

internal fun interface TextValidator {
    fun validate(input: String, issuer: Issuer?): ValidationError?
}

internal sealed interface ValidationError {

    val errorRes: Int

    fun errorMessage(context: Context): String = context.getString(errorRes)

    data object InvalidCardNumber: ValidationError {
        override val errorRes: Int = R.string.validation_error_invalid_card
    }

    data object UnknownCardType: ValidationError {
        override val errorRes = R.string.validation_error_unknown_card_type
    }

    data object InvalidExpiration: ValidationError {
        override val errorRes = R.string.validation_error_invalid_date
    }

    data object InvalidFormat: ValidationError {
        override val errorRes = R.string.validation_error_invalid_date_format
    }

    data object InvalidCvv: ValidationError {
        override val errorRes: Int = R.string.validation_error_invalid_cvv
    }
}