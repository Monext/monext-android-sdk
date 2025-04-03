package com.monext.sdk.internal.api

import kotlinx.serialization.Serializable

@Serializable
internal data class AvailableCardNetworksRequest(
    val cardNumber: String,
    val handledContracts: List<HandledContract>
)

@Serializable
internal data class HandledContract(
    val cardCode: String,
    val contractNumber: String
)