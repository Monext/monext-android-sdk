package com.monext.sdk.internal.security.check

import android.os.Debug
import com.monext.sdk.internal.security.check.SecurityCheck

class DebuggerCheck : SecurityCheck {

    companion object {
        /**
         * Seuil en nanosecondes au-delà duquel le temps CPU de la boucle de référence
         * est considéré comme anormalement élevé, indiquant la présence d'un débogueur.
         *
         * Valeur : 10 ms (10 000 000 ns).
         */
        private const val TIMING_THRESHOLD_NS = 10_000_000L

        /**
         * Nombre d'itérations de la boucle de référence utilisée pour la détection par timing.
         */
        private const val TIMING_LOOP_ITERATIONS = 1_000_000
    }

    /**
     * Exécute les vérifications de présence d'un débogueur.
     *
     * @return `true` si un débogueur est détecté, `false` sinon.
     */
    override fun check(): Boolean =
        detectJavaDebugger() || detectDebuggerByTiming()

    /**
     * Vérifie si un débogueur Java (JDWP) est actuellement connecté ou en attente.
     *
     * Utilise les méthodes standard du SDK Android pour interroger l'état du débogueur :
     * - [Debug.isDebuggerConnected] : `true` si un débogueur est actif.
     * - [Debug.waitingForDebugger] : `true` si l'application attend une connexion.
     *
     * @return `true` si un débogueur Java est détecté.
     */
    fun detectJavaDebugger(): Boolean =
        Debug.isDebuggerConnected() || Debug.waitingForDebugger()

    /**
     * Détecte la présence d'un débogueur par analyse du temps d'exécution (timing attack).
     *
     * Mesure le temps CPU nécessaire pour exécuter une boucle de référence de
     * [TIMING_LOOP_ITERATIONS] itérations. En présence d'un débogueur, le temps
     * d'exécution est significativement supérieur au seuil [TIMING_THRESHOLD_NS].
     *
     * Cette technique est complémentaire à [detectJavaDebugger] car elle peut
     * détecter certains débogueurs qui masquent leur présence aux APIs standard.
     *
     * @return `true` si le temps CPU dépasse le seuil, indiquant un débogueur probable.
     */
    fun detectDebuggerByTiming(): Boolean {
        val start = Debug.threadCpuTimeNanos()
        repeat(TIMING_LOOP_ITERATIONS) { /* boucle de référence */ }
        val elapsed = Debug.threadCpuTimeNanos() - start
        return elapsed >= TIMING_THRESHOLD_NS
    }
}