package com.monext.sdkexample

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monext.sdk.MnxtEnvironment
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

// Extension pour créer le DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class SessionManager(
    private val context: Context,
    restoredToken: String?,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        private val SESSION_TOKEN_KEY = stringPreferencesKey("session_token")
        private val ENVIRONMENT_KEY = stringPreferencesKey("environment")
        private val CUSTOM_HOSTNAME_KEY = stringPreferencesKey("custom_hostname")
        private val SELECTED_LANGUAGE_KEY = stringPreferencesKey("selected_language")
    }

    private val _sessionToken = MutableStateFlow<String?>(restoredToken)
    val sessionToken = _sessionToken.asStateFlow()

    // Flow pour l'environnement sauvegardé
    val savedEnvironment: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[ENVIRONMENT_KEY] ?: "Sandbox"
    }

    // Flow pour l'hostname personnalisé sauvegardé
    val savedCustomHostname: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[CUSTOM_HOSTNAME_KEY] ?: ""
    }

    // Flow pour la langue sauvegardée
    val savedLanguage: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[SELECTED_LANGUAGE_KEY] ?: "EN"
    }

    fun resetSession() {
        _sessionToken.value = null
    }

    suspend fun setSessionToken(token: String?) {
        withContext(dispatcher) {
            _sessionToken.value = token
            // Sauvegarder dans DataStore
            context.dataStore.edit { preferences ->
                if (token != null) {
                    preferences[SESSION_TOKEN_KEY] = token
                } else {
                    preferences.remove(SESSION_TOKEN_KEY)
                }
            }
        }
    }

    suspend fun saveEnvironment(environment: MnxtEnvironment) {
        withContext(dispatcher) {
            context.dataStore.edit { preferences ->
                when (environment) {
                    is MnxtEnvironment.Sandbox -> preferences[ENVIRONMENT_KEY] = "Sandbox"
                    is MnxtEnvironment.Production -> preferences[ENVIRONMENT_KEY] = "Production"
                    is MnxtEnvironment.Custom -> {
                        preferences[ENVIRONMENT_KEY] = "Custom"
                        preferences[CUSTOM_HOSTNAME_KEY] = environment.hostname
                    }
                }
            }
        }
    }

    suspend fun saveCustomHostname(hostname: String) {
        withContext(dispatcher) {
            context.dataStore.edit { preferences ->
                preferences[CUSTOM_HOSTNAME_KEY] = hostname
            }
        }
    }

    suspend fun saveLanguage(language: String) {
        withContext(dispatcher) {
            context.dataStore.edit { preferences ->
                preferences[SELECTED_LANGUAGE_KEY] = language
            }
        }
    }
}