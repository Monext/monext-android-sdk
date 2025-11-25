package com.monext.sdk.internal.util

import android.content.Context
import com.monext.sdk.R
import com.monext.sdk.internal.data.Issuer

internal fun interface TextValidator {
    fun validate(input: String, issuer: Issuer?): ValidationError?
}

internal sealed interface ValidationError {

    /**
     * Si l'erreur provient d'une resource, contient l'id @StringRes.
     * Sinon null.
     */
    val errorRes: Int?

    /**
     * Si l'erreur contient déjà le message texte (backend/localisé), l'utiliser.
     * Sinon null.
     */
    val rawMessage: String?

    /**
     * Retourne le message à afficher : rawMessage si présent, sinon la resource si présente,
     * sinon chaîne vide.
     */
    fun errorMessage(context: Context): String =
        rawMessage ?: errorRes?.let { context.getString(it) } ?: ""

    object InvalidCardNumber : ValidationError {
        override val errorRes: Int? = R.string.validation_error_invalid_card
        override val rawMessage: String? = null
    }

    object UnknownCardType : ValidationError {
        override val errorRes: Int? = R.string.validation_error_unknown_card_type
        override val rawMessage: String? = null
    }

    object InvalidExpiration : ValidationError {
        override val errorRes: Int? = R.string.validation_error_invalid_date
        override val rawMessage: String? = null
    }

    object InvalidFormat : ValidationError {
        override val errorRes: Int? = R.string.validation_error_invalid_date_format
        override val rawMessage: String? = null
    }

    object InvalidCvv : ValidationError {
        override val errorRes: Int? = R.string.validation_error_invalid_cvv
        override val rawMessage: String? = null
    }

    /**
     * Erreur personnalisée contenant un message brut (p.ex. validationErrorMessage du backend).
     */
    data class Custom(override val rawMessage: String) : ValidationError {
        override val errorRes: Int? = null
    }
}