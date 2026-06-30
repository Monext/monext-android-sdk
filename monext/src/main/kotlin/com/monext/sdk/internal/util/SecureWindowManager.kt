package com.monext.sdk.internal.util

import android.app.Activity
import android.view.WindowManager

/**
 * Gestionnaire centralisé de la protection d'écran via [WindowManager.LayoutParams.FLAG_SECURE].
 *
 * Ce singleton maintient un compteur de références par [Activity] afin d'appliquer
 * [FLAG_SECURE][WindowManager.LayoutParams.FLAG_SECURE] uniquement lorsqu'au moins un
 * consommateur est actif, et de le retirer proprement dès que tous les consommateurs
 * ont libéré leur référence.
 *
 * Ce mécanisme est nécessaire lorsque plusieurs composants (ex: plusieurs champs de
 * saisie sensibles) peuvent demander simultanément la protection d'écran sur la même
 * [Activity] hôte. Sans comptage de références, le premier composant démonté retirerait
 * le flag même si d'autres composants sensibles restent visibles à l'écran.
 *
 * **Utilisation typique :**
 * ```kotlin
 * // Lors de l'affichage d'un écran contenant des données sensibles
 * SecureWindowManager.acquire(activity)
 *
 * // Lors de la fermeture ou du démontage du composant
 * SecureWindowManager.release(activity)
 * ```
 *
 * Toutes les méthodes sont thread-safe via [@Synchronized][Synchronized].
 *
 * @see WindowManager.LayoutParams.FLAG_SECURE
 */
internal object SecureWindowManager {
    private val activeCount = mutableMapOf<Activity, Int>()

    @Synchronized
    fun acquire(activity: Activity) {
        val count = activeCount.getOrDefault(activity, 0)
        if (count == 0) {
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        }
        activeCount[activity] = count + 1
    }

    @Synchronized
    fun release(activity: Activity) {
        val count = activeCount.getOrDefault(activity, 1) - 1
        if (count <= 0) {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            activeCount.remove(activity)
        } else {
            activeCount[activity] = count
        }
    }
}