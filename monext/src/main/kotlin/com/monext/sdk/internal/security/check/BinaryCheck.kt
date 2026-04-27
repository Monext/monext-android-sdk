package com.monext.sdk.internal.security.check

import com.monext.sdk.internal.security.check.SecurityCheck
import java.io.File

class BinaryCheck : SecurityCheck {

    /**
     * Liste des chemins de binaires et fichiers typiquement présents sur un appareil rooté.
     *
     * Inclut les chemins pour `su`, `busybox`, `daemonsu` et les fichiers
     * associés à SuperSU ou Superuser.
     */
    private val rootBinaryPaths = listOf(
        "/system/app/Superuser.apk",
        "/system/etc/init.d/99SuperSUDaemon",
        "/dev/com.koushikdutta.superuser.daemon/",
        "/system/xbin/daemonsu",
        "/sbin/su",
        "/system/bin/su",
        "/system/bin/failsafe/su",
        "/system/xbin/su",
        "/system/xbin/busybox",
        "/system/sd/xbin/su",
        "/data/local/su",
        "/data/local/xbin/su",
        "/data/local/bin/su"
    )

    /**
     * Exécute les vérifications de présence de binaires root.
     *
     * Vérifie d'abord les chemins connus, puis parcourt les répertoires
     * du `PATH` système à la recherche du binaire `su`.
     *
     * @return `true` si un binaire root est détecté, `false` sinon.
     */
    override fun check(): Boolean =
        checkKnownPaths() || checkSuOnPath()

    /**
     * Vérifie l'existence des chemins de binaires connus.
     *
     * @return `true` si au moins un fichier de la liste existe sur l'appareil.
     */
    private fun checkKnownPaths(): Boolean =
        rootBinaryPaths.any { path -> File(path).exists() }

    /**
     * Vérifie si le binaire `su` est accessible via les répertoires du `PATH` système.
     *
     * Parcourt chaque répertoire défini dans la variable d'environnement `PATH`
     * et recherche la présence d'un exécutable `su`.
     *
     * @return `true` si `su` est trouvé dans l'un des répertoires du `PATH`.
     */
    private fun checkSuOnPath(): Boolean =
        System.getenv("PATH")
            ?.split(":")
            ?.any { dir -> File(dir, "su").exists() }
            ?: false
}