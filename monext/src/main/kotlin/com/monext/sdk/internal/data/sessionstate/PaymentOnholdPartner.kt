package com.monext.sdk.internal.data.sessionstate

import kotlinx.serialization.Serializable;

@Serializable
internal data class PaymentOnholdPartner(
    val message: CustomMessage?,
    val selectedCardCode: String,
    val selectedContractNumber: String,
)
