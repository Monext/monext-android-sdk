package com.monext.sdkexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge


class MainActivity: ComponentActivity() {

    private val sessionKey = "SAVED_SESSION_TOKEN"

    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Passer le contexte au SessionManager
        sessionManager = SessionManager(
            context = this,
            restoredToken = savedInstanceState?.getString(sessionKey)
        )

        setContent { AppContainer(sessionManager) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(sessionKey, sessionManager.sessionToken.value)
        super.onSaveInstanceState(outState)
    }
}