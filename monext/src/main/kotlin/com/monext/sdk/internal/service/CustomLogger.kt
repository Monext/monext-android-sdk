package com.monext.sdk.internal.service

import android.util.Log


/**
 * Implémentation Android du Logger.
 * Logguer qui utilise le logger Android. Peut-être utilisé pour envoyer les logs au serveur.
 */
class CustomLogger: Logger {

    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
    }
}