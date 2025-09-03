package com.monext.sdk.internal.data.sessionstate

import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentSuccess(
    val displayTicket: Boolean,
    val fragmented: Boolean,
    val paymentCard: String?,
    val selectedCardCode: String,
    val selectedContractNumber: String,
    val ticket: List<Ticket>?
)