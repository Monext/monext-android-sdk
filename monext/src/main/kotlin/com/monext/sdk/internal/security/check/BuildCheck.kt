package com.monext.sdk.internal.security.check

import android.os.Build
import com.monext.sdk.internal.security.check.SecurityCheck

class BuildCheck : SecurityCheck {

    /**
     * Exécute l'ensemble des vérifications liées au build système.
     *
     * @return `true` si au moins un indicateur suspect est détecté, `false` sinon.
     */
    override fun check(): Boolean =
        detectTestKeys() || detectCustomRom() || detectEmulator()

    /**
     * Vérifie si le build est signé avec des clés de test plutôt que des clés de release.
     *
     * Les builds officiels Android sont signés avec `release-keys`. La présence
     * de `test-keys` dans [Build.TAGS] est un indicateur fort de ROM custom ou rootée.
     *
     * @return `true` si [Build.TAGS] contient `test-keys`.
     */
    private fun detectTestKeys(): Boolean =
        Build.TAGS?.contains("test-keys") == true

    /**
     * Vérifie si le fingerprint du build indique une ROM custom ou non officielle.
     *
     * Analyse [Build.FINGERPRINT] à la recherche de sous-chaînes caractéristiques
     * des builds non officiels.
     *
     * @return `true` si le fingerprint contient `custom` ou `test-keys`.
     */
    private fun detectCustomRom(): Boolean {
        val fingerprint = Build.FINGERPRINT.lowercase()
        return fingerprint.contains("custom") || fingerprint.contains("test-keys")
    }

    /**
     * Vérifie si l'application s'exécute sur un émulateur Android.
     *
     * Inspecte plusieurs propriétés du build pour détecter les émulateurs
     * courants (Android SDK Emulator, Genymotion, etc.).
     *
     * Les indicateurs vérifiés incluent :
     * - [Build.PRODUCT] : valeurs comme `sdk`, `simulator`, `vbox86p`
     * - [Build.HARDWARE] : valeurs comme `goldfish`, `ranchu`
     * - [Build.MANUFACTURER] : `Genymotion`
     * - [Build.MODEL] : `Emulator`, `Android SDK built for x86`
     * - [Build.BRAND] : `generic`
     *
     * @return `true` si l'appareil est identifié comme un émulateur.
     */
    private fun detectEmulator(): Boolean {
        val emulatorIndicators = listOf(
            Build.PRODUCT.contains("sdk"),
            Build.PRODUCT.contains("simulator"),
            Build.PRODUCT.contains("vbox86p"),
            Build.HARDWARE.contains("goldfish"),
            Build.HARDWARE.contains("ranchu"),
            Build.MANUFACTURER.equals("Genymotion", ignoreCase = true),
            Build.MODEL.contains("Emulator", ignoreCase = true),
            Build.MODEL.contains("Android SDK built for x86", ignoreCase = true),
            Build.BRAND.equals("generic", ignoreCase = true)
        )
        return emulatorIndicators.any { it }
    }
}