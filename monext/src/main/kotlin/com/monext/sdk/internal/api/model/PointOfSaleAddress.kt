package com.monext.sdk.internal.api.model

import kotlinx.serialization.Serializable

@Serializable
internal data class PointOfSaleAddress(
    val address1: String?,
    val address2: String?,
    val city: String?,
    val zipCode: String?
)