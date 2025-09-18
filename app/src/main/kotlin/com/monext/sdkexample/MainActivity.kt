package com.monext.sdkexample

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.monext.sdk.Appearance
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

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