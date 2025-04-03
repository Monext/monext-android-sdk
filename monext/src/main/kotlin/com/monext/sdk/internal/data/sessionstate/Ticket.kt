package com.monext.sdk.internal.data.sessionstate

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Ticket(
    val interline: Boolean?,
    @SerialName("k") val key: String?,
    @SerialName("s") val style: String?,
    val t: Int,
    @SerialName("v") val value: String
)