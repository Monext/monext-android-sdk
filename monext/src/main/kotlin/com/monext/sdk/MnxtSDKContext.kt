package com.monext.sdk

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Context used by the SDK.
 * Only 'environment' is mandatory
 */
data class MnxtSDKContext(
    val environment: MnxtEnvironment,
    val config: MnxtSDKConfiguration = MnxtSDKConfiguration(),
    val appearance: Appearance = Appearance(),
    val googlePayConfiguration: GooglePayConfiguration = GooglePayConfiguration()
)

internal val LocalAppearance = staticCompositionLocalOf<Appearance>{
    error("No CompositionLocal LocalAppearance")
}

internal val LocalEnvironment = staticCompositionLocalOf<MnxtEnvironment> {
    error("No CompositionLocal LocalEnvironment")
}