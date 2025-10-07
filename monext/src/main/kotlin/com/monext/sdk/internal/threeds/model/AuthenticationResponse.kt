package com.monext.sdk.internal.threeds.model

import kotlinx.serialization.Serializable

/**
 * Classe qui représente la reponse du challenge sdk 3DS
 */
@Serializable
internal data class AuthenticationResponse (
    val acsReferenceNumber: String? = null,
    val acsTransID: String? = null,
    val threeDSVersion: String? = null,
    val threeDSServerTransID: String? = null,
    val transStatus: String? = null
)
