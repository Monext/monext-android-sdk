package com.monext.sdk.internal.data.sessionstate

import kotlinx.serialization.Serializable

@Serializable
internal data class CustomMessage(
    val type: String,
    val localizedMessage: String?,
    val displayIcon: Boolean
)