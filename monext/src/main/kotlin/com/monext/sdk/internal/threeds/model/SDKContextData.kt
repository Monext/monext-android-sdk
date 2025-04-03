package com.monext.sdk.internal.threeds.model

import kotlinx.serialization.Serializable

/**
 * Classse qui représente les données de context 3DS collectées sur le device.
 */
@Serializable
internal data class SDKContextData(val deviceRenderingOptionsIF: String,
                     val deviceRenderOptionsUI: String,
                     val maxTimeout: Int,
                     val referenceNumber: String,
                     val ephemPubKey: String,
                     val appID: String,
                     val transID: String,
                     val encData: String)