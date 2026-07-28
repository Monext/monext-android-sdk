package com.monext.sdk.internal.security

import android.content.Context
import com.monext.sdk.BuildConfig
import com.monext.sdk.internal.security.check.BinaryCheck
import com.monext.sdk.internal.security.check.BuildCheck
import com.monext.sdk.internal.security.check.DebuggerCheck
import com.monext.sdk.internal.security.check.FridaCheck
import com.monext.sdk.internal.security.check.PackageCheck
import com.monext.sdk.internal.security.check.ProcessCheck
import com.monext.sdk.internal.security.check.SecurityCheck

/**
 * Implémentation interne du détecteur de root.
 *
 * Orchestre l'ensemble des [SecurityCheck] et retourne `true`
 * dès qu'un indicateur de compromission est détecté (court-circuit).
 *
 * Cette classe est à usage interne uniquement. L'API publique
 * est exposée via [RootDetector].
 *
 * @param context Le contexte Android (doit être le ApplicationContext).
 */
internal class RootDetectorImpl(context: Context) {

    private val checks: List<SecurityCheck> = listOf(
        BinaryCheck(),
        PackageCheck(context.packageManager),
        BuildCheck(),
        ProcessCheck(context),
        DebuggerCheck(),
        FridaCheck(),
    )

    /**
     * Exécute tous les checks de sécurité.
     *
     * @return `true` si au moins un check détecte une compromission.
     */
    fun isDeviceCompromised(): Boolean =
        checks.any { it.check() }
}