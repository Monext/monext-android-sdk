package com.monext.sdk

import com.monext.sdk.internal.service.Logger

/** Various fields used by the SDK */
data class MnxtSDKConfiguration(

    /**
     * The language used by the SDK
     *
     * possible values: EN, FR
     */
    val language: String = "EN",

    /**
     * TODO:
     */
    val displayFinalState: Boolean = true
)