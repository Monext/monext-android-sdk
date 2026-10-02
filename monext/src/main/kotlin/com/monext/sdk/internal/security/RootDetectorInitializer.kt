package com.monext.sdk.internal.security

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri

/**
 * Initialise automatiquement [RootDetector] au démarrage de l'application.
 *
 * Ce [ContentProvider] est déclaré dans le manifest du SDK et fusionné
 * automatiquement dans le manifest de l'application intégratrice lors
 * de la compilation. Aucune configuration n'est requise côté intégrateur.
 *
 * ### Mécanisme
 * Android instancie tous les [ContentProvider] déclarés avant d'appeler
 * [android.app.Application.onCreate]. Cela garantit que [RootDetector]
 * est initialisé avant toute interaction utilisateur.
 *
 * ### Inspiration
 * Ce pattern est utilisé par Firebase (`FirebaseInitProvider`),
 * Timber, et Jetpack App Startup.
 *
 * @see RootDetector
 */
class RootDetectorInitializer : ContentProvider() {

    /**
     * Point d'initialisation automatique.
     *
     * Appelé par Android avant [android.app.Application.onCreate].
     * Initialise [RootDetector] avec l'ApplicationContext.
     *
     * @return `true` si l'initialisation s'est bien déroulée.
     */
    override fun onCreate(): Boolean {
        context?.applicationContext?.let { appContext ->
            RootDetector.init(appContext)
        }
        return true
    }

    // ContentProvider stubs — non utilisés
    override fun query(uri: Uri, p: Array<String>?, s: String?, sA: Array<String>?, so: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, s: String?, sA: Array<String>?): Int = 0
    override fun update(uri: Uri, v: ContentValues?, s: String?, sA: Array<String>?): Int = 0
}