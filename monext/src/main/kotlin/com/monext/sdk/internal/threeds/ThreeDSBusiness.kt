package com.monext.sdk.internal.threeds

import ThreeDSConfiguration
import com.monext.sdk.internal.threeds.response.DirectoryServerSdkKey
import com.netcetera.threeds.sdk.api.configparameters.builder.ConfigurationBuilder
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration
import java.util.Collections

internal class ThreeDSBusiness {

    internal fun createSchemeConfiguration(
        key: DirectoryServerSdkKey,
        schemeLogo: String
    ): SchemeConfiguration =
        SchemeConfiguration.newSchemeConfiguration(convertValueIfCB(key.scheme))
            .ids(Collections.singletonList(key.rid))
            .encryptionPublicKey(key.publicKey, null)
            .rootPublicKey(key.rootPublicKey)
            .logo(schemeLogo)
            .build()

    /**
     * Fonction qui permet d'initier la configuration
     */
    internal fun createConfigParameters(): ConfigurationBuilder = ThreeDSConfiguration.createConfigParameters()
    /**
     * Fonction qui converti notre carType "CB" en "cartesBancaires" pour la compatibilité du SDK 3DS
     */
    internal fun convertValueIfCB(value:String): String {
        return if(value.equals("CB", ignoreCase = true)) "cartesBancaires" else value
    }
}