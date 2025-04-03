package com.monext.sdk.internal.api.model

import kotlinx.serialization.Serializable

@Serializable
internal data class DeviceInfo(
    val colorDepth: Int,
    val containerHeight: Double,
    val containerWidth: Int,
    val javaEnabled: Boolean,
    val screenHeight: Int,
    val screenWidth: Int,
    val timeZoneOffset: Int
)