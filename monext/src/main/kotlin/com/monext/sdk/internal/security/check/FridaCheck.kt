package com.monext.sdk.internal.security.check

import com.monext.sdk.internal.security.check.SecurityCheck
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetSocketAddress
import java.net.Socket

class FridaCheck : SecurityCheck {

    companion object {
        /**
         * Port réseau par défaut sur lequel Frida Server écoute les connexions.
         */
        private const val FRIDA_DEFAULT_PORT = 27042

        /**
         * Adresse localhost utilisée pour tenter la connexion au serveur Frida.
         */
        private const val LOCALHOST = "127.0.0.1"

        /**
         * Délai maximum en millisecondes pour la tentative de connexion au port Frida.
         * Une valeur faible limite l'impact sur les performances.
         */
        private const val CONNECTION_TIMEOUT_MS = 300
    }

    /**
     * Exécute les vérifications de présence de Frida.
     *
     * @return `true` si Frida est détecté via le port ou les processus, `false` sinon.
     */
    override fun check(): Boolean =
        isFridaPortOpen() || isFridaProcessRunning()

    /**
     * Tente une connexion TCP sur le port par défaut de Frida Server ([FRIDA_DEFAULT_PORT]).
     *
     * Si la connexion aboutit, cela indique qu'un serveur Frida est actif sur l'appareil.
     * La connexion est fermée immédiatement après détection.
     *
     * @return `true` si la connexion au port Frida réussit, `false` sinon ou en cas d'erreur.
     */
    fun isFridaPortOpen(): Boolean = runCatching {
        Socket().use { socket ->
            socket.connect(
                InetSocketAddress(LOCALHOST, FRIDA_DEFAULT_PORT),
                CONNECTION_TIMEOUT_MS
            )
            true
        }
    }.getOrDefault(false)

    /**
     * Recherche le processus Frida parmi les processus actifs via la commande `ps`.
     *
     * Exécute `ps` et analyse la sortie ligne par ligne à la recherche de marqueurs
     * caractéristiques de Frida (`frida`, `frida-server`).
     *
     * > **Note** : la commande `ps` peut être inaccessible sur Android 9+ selon
     * > les restrictions SELinux en vigueur. En cas d'échec, la méthode retourne `false`
     * > sans lever d'exception.
     *
     * @return `true` si un processus Frida est trouvé, `false` sinon ou en cas d'erreur.
     */
    fun isFridaProcessRunning(): Boolean = runCatching {
        val process = Runtime.getRuntime().exec("ps")
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            reader.lineSequence().any { line ->
                line.contains("frida", ignoreCase = true)
            }
        }
    }.getOrDefault(false)
}