package com.monext.sdk.internal.security.check

import android.content.pm.PackageManager
import com.monext.sdk.internal.security.check.SecurityCheck

class PackageCheck(private val packageManager: PackageManager) : SecurityCheck {

    /**
     * Liste des noms de packages associés aux applications de root courantes.
     *
     * Inclut Magisk, SuperSU, Superuser et leurs variantes connues.
     */
    private val knownRootPackages = listOf(
        "com.topjohnwu.magisk",
        "eu.chainfire.supersu",
        "com.noshufou.android.su",
        "com.noshufou.android.su.elite",
        "com.koushikdutta.superuser",
        "com.thirdparty.superuser",
        "com.yellowes.su",
        "com.kingroot.kinguser",
        "com.kingo.root",
        "com.smedialink.oneclickroot",
        "com.zhiqupk.root.global",
        "com.alephzain.framaroot"
    )

    /**
     * Exécute la vérification de présence des applications de root connues.
     *
     * Interroge le [PackageManager] pour chaque package de la liste.
     * Une exception [PackageManager.NameNotFoundException] indique que le package
     * n'est pas installé ou n'est pas visible (Android 11+).
     *
     * @return `true` si au moins une application de root est détectée, `false` sinon.
     */
    override fun check(): Boolean =
        knownRootPackages.any { packageName -> isPackageInstalled(packageName) }

    /**
     * Vérifie si un package donné est installé et visible sur l'appareil.
     *
     * @param packageName Le nom complet du package à vérifier (ex: `com.topjohnwu.magisk`).
     * @return `true` si le package est installé et visible, `false` sinon.
     */
    private fun isPackageInstalled(packageName: String): Boolean =
        runCatching { packageManager.getPackageInfo(packageName, 0) }
            .isSuccess
}