package com.monext.sdk.internal.data.sessionstate

import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentFailure(
    val message: FailureMessage,
    val selectedCardCode: String,
    val selectedContractNumber: String
)

@Serializable
internal data class FailureMessage(
    val displayIcon: Boolean,
    val localizedMessage: String,
    val type: String
)