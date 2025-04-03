package com.monext.sdk

import java.net.URI


/** The Monext environment that the SDK will use */
sealed class MnxtEnvironment {

    /** The Monext sandbox environment */
    data object Sandbox: MnxtEnvironment()

    /** The Monext production environment */
    data object Production: MnxtEnvironment()

    /** A custom environment with a configurable hostname */
    data class Custom(val hostname: String): MnxtEnvironment()

    fun isSandbox() : Boolean {
        return when(this) {
            Production -> false
            else -> true
        }
    }

    internal val host: String
        get() = when (this) {
            is Sandbox -> "homologation-payment.payline.com"
            is Production -> "payment.payline.com"
            is Custom -> {
                // Extraire le hostname de l'URL complète
                val uri = constructCustomUri()
                uri.host!!
            }
        }


    internal val path: String
        get() = when (this) {
            is Sandbox, Production -> ""
            is Custom -> {
                // Extraire le path de l'URL complète
                val uri = constructCustomUri()
                uri.path!!
            }
        }

    private fun Custom.constructCustomUri(): URI {

        var hostnameToUse = hostname
        if (hostname.isNotBlank() && !hostname.startsWith("http", true)) {
            hostnameToUse = "https://$hostname";
        }

        return URI(hostnameToUse)
    }

}