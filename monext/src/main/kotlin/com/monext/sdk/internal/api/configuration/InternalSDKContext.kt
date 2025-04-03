package com.monext.sdk.internal.api.configuration

import com.monext.sdk.Appearance
import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.MnxtSDKConfiguration
import com.monext.sdk.MnxtSDKContext
import com.monext.sdk.internal.service.CustomLogger
import com.monext.sdk.internal.service.Logger

/**
 * Contexte interne pour faire transiter les données.
 */
class InternalSDKContext(sdkContext: MnxtSDKContext) {
    val environment: MnxtEnvironment = sdkContext.environment
    val config: MnxtSDKConfiguration = sdkContext.config
    val appearance: Appearance = sdkContext.appearance
    val logger: Logger = CustomLogger()
}