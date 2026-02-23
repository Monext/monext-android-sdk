package com.monext.sdkexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge


class MainActivity: ComponentActivity() {
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Passer le contexte au SessionManager
        sessionManager = SessionManager(
            context = this
        )

        setContent { AppContainer(sessionManager) }
    }
}