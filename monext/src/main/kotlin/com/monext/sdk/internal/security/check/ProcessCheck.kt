package com.monext.sdk.internal.security.check

import android.content.Context

class ProcessCheck(private val context: Context) : SecurityCheck {

    private val suspiciousProcessNames = listOf(
        "supersu",
        "superuser",
        "daemonsu",
        "magisk",
        "su_daemon"
    )

    override fun check(): Boolean = detectSuspiciousProcesses()

    private fun detectSuspiciousProcesses(): Boolean = runCatching {
        // Lecture de /proc au lieu de getRunningServices (déprécié API 26+)
        val procDir = java.io.File("/proc")
        procDir.listFiles { file ->
            file.isDirectory && file.name.all { it.isDigit() }
        }?.any { pidDir ->
            val cmdlineFile = java.io.File(pidDir, "cmdline")
            if (cmdlineFile.exists()) {
                val processName = cmdlineFile.readText()
                    .replace("\u0000", "")
                    .trim()
                    .lowercase()
                isSuspiciousProcess(processName)
            } else {
                false
            }
        } ?: false
    }.getOrDefault(false)

    private fun isSuspiciousProcess(processName: String): Boolean =
        suspiciousProcessNames.any { indicator ->
            processName.contains(indicator, ignoreCase = true)
        }
}