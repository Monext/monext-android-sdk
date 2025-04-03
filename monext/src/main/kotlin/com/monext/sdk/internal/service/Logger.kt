package com.monext.sdk.internal.service

/**
 * Interface pour abstraire le logging.
 * Possibilité d'envoyer des logs serveur si besoin.
 */
interface Logger {

    /**
     * Log Debug
     */
    fun d(tag: String, message: String)

    /**
     * Log Info
     */
    fun i(tag: String, message: String)

    /**
     * Log Warn
     */
    fun w(tag: String, message: String, throwable: Throwable? = null)

    /**
     * Log Error
     */
    fun e(tag: String, message: String, throwable: Throwable? = null)
}