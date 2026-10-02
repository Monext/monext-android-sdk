package com.monext.sdk.internal.security

import android.content.Context
import android.util.Log

/**
 * Point d'entrée public pour la détection de root du SDK Monext.
 *
 * Cette classe est un singleton initialisé automatiquement au démarrage
 * de l'application via [RootDetectorInitializer] (ContentProvider).
 * Aucun appel manuel à [init] n'est nécessaire côté application intégratrice.
 *
 * ### Usage
 * ```kotlin
 * if (RootDetector.isCompromised()) {
 *     // Bloquer l'action sensible
 * }
 * ```
 *
 * ### Cycle de vie
 * - **Init** : déclenché automatiquement par [RootDetectorInitializer] au boot de l'app.
 * - **Résultat** : mis en cache, recalculé toutes les [CACHE_TTL_MS] millisecondes.
 * - **Thread-safety** : le champ [compromised] est marqué `@Volatile`.
 */
object RootDetector {

    private const val TAG = "RootDetector"

    /**
     * Durée de validité du cache en millisecondes (30 secondes).
     * Évite de relancer tous les checks à chaque appel.
     */
    private const val CACHE_TTL_MS = 30_000L

    @Volatile private var initialized = false
    @Volatile private var compromised = false
    @Volatile private var lastCheckTime = 0L

    private var detectorImpl: RootDetectorImpl? = null

    /**
     * Initialise le détecteur avec le contexte de l'application.
     *
     * Appelé automatiquement par [RootDetectorInitializer].
     * Un second appel est ignoré (idempotent).
     *
     * @param context Le contexte de l'application (ApplicationContext).
     */
    internal fun init(context: Context) {
        if (initialized) return

        detectorImpl = RootDetectorImpl(context.applicationContext)
        refreshCheck()
        initialized = true

        Log.d(TAG, "RootDetector initialized — compromised=$compromised")
    }

    /**
     * Indique si l'appareil est considéré comme compromis.
     *
     * Le résultat est mis en cache pendant [CACHE_TTL_MS] ms.
     * Si [init] n'a pas encore été appelé, retourne `false` par défaut (fail-open).
     *
     * @return `true` si un indicateur de root ou de compromission a été détecté.
     */
    fun isCompromised(): Boolean {
        if (!initialized) {
            Log.w(TAG, "isCompromised() called before init()")
            return false
        }

        val now = System.currentTimeMillis()
        if (now - lastCheckTime > CACHE_TTL_MS) {
            refreshCheck()
        }

        return compromised
    }

    /**
     * Force un recalcul immédiat de l'état de sécurité, en ignorant le cache.
     *
     * À utiliser avec précaution car les checks natifs peuvent être coûteux.
     *
     * @return `true` si l'appareil est compromis après recalcul.
     */
    fun forceRefresh(): Boolean {
        refreshCheck()
        return compromised
    }

    /**
     * Recalcule l'état de sécurité et met à jour le cache.
     */
    private fun refreshCheck() {
        compromised = detectorImpl?.isDeviceCompromised() ?: false
        lastCheckTime = System.currentTimeMillis()
    }
}