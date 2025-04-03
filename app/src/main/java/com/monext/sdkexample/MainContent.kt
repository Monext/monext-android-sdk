package com.monext.sdkexample

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.MnxtSDKConfiguration
import com.monext.sdk.MnxtSDKContext
import com.monext.sdk.PaymentBox
import com.monext.sdk.PaymentResult
import kotlinx.coroutines.launch

@Composable
fun MainContent(modifier: Modifier = Modifier, sessionManager: SessionManager) {
    val scope = rememberCoroutineScope()

    val sessionToken by sessionManager.sessionToken.collectAsStateWithLifecycle()
    val savedEnvironment by sessionManager.savedEnvironment.collectAsStateWithLifecycle(initialValue = "Sandbox")
    val savedCustomHostname by sessionManager.savedCustomHostname.collectAsStateWithLifecycle(initialValue = "")
    val savedLanguage by sessionManager.savedLanguage.collectAsStateWithLifecycle(initialValue = "EN")

    val themeOpts = listOf(Theme.DEFAULT, Theme.DARK)
    var selectedTheme by remember { mutableStateOf(themeOpts.first()) }

    var customHostname by remember { mutableStateOf("") }
    val envOpts = remember(customHostname) {
        listOf(
            MnxtEnvironment.Sandbox,
            MnxtEnvironment.Production,
            MnxtEnvironment.Custom(customHostname)
        )
    }
    var selectedEnv by remember { mutableStateOf(envOpts.first()) }

    val langOpts = listOf("EN", "FR")
    var selectedLang by remember { mutableStateOf("EN") }

    var tokenInput by remember { mutableStateOf(sessionToken ?: "") }

    // Initialiser les valeurs sauvegardées au démarrage
    LaunchedEffect(savedEnvironment, savedCustomHostname, savedLanguage) {
        customHostname = savedCustomHostname
        selectedLang = savedLanguage

        selectedEnv = when (savedEnvironment) {
            "Production" -> MnxtEnvironment.Production
            "Custom" -> MnxtEnvironment.Custom(savedCustomHostname)
            else -> MnxtEnvironment.Sandbox
        }
    }

    Column(
        modifier = Modifier
            .then(modifier
                )
            .padding(WindowInsets.systemBars.asPaddingValues())
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text("Thème", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    themeOpts.forEach { theme ->
                        Row(
                            Modifier
                                .selectable(
                                    selected = (theme == selectedTheme),
                                    onClick = { selectedTheme = theme },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (theme == selectedTheme), onClick = null)
                            Text(theme.name)
                        }
                    }
                }

                Text("Environnement", style = MaterialTheme.typography.titleMedium)
                Column {
                    envOpts.forEach { env ->
                        val isSelected = when {
                            env is MnxtEnvironment.Sandbox && selectedEnv is MnxtEnvironment.Sandbox -> true
                            env is MnxtEnvironment.Production && selectedEnv is MnxtEnvironment.Production -> true
                            env is MnxtEnvironment.Custom && selectedEnv is MnxtEnvironment.Custom -> true
                            else -> false
                        }

                        Row(
                            Modifier
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        selectedEnv = if (env is MnxtEnvironment.Custom) {
                                            MnxtEnvironment.Custom(customHostname)
                                        } else {
                                            env
                                        }
                                        // Sauvegarder l'environnement sélectionné
                                        scope.launch {
                                            sessionManager.saveEnvironment(selectedEnv)
                                        }
                                    },
                                    role = Role.RadioButton
                                )
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = isSelected, onClick = null)
                            Text(
                                when (env) {
                                    is MnxtEnvironment.Sandbox -> "Sandbox"
                                    is MnxtEnvironment.Production -> "Production"
                                    is MnxtEnvironment.Custom -> "Custom"
                                }
                            )
                        }
                    }
                    if (selectedEnv is MnxtEnvironment.Custom) {
                        OutlinedTextField(
                            value = customHostname,
                            onValueChange = { newValue ->
                                customHostname = newValue
                                selectedEnv = MnxtEnvironment.Custom(newValue)
                                // Sauvegarder l'hostname et l'environnement
                                scope.launch {
                                    sessionManager.saveCustomHostname(newValue)
                                    sessionManager.saveEnvironment(selectedEnv)
                                }
                            },
                            label = { Text("Hostname") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }

                Text("Langue", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    langOpts.forEach { lang ->
                        Row(
                            Modifier
                                .selectable(
                                    selected = (lang == selectedLang),
                                    onClick = {
                                        selectedLang = lang
                                        // Sauvegarder la langue sélectionnée
                                        scope.launch {
                                            sessionManager.saveLanguage(lang)
                                        }
                                    },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (lang == selectedLang), onClick = null)
                            Text(lang)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { newValue ->
                        tokenInput = newValue
                        scope.launch { sessionManager.setSessionToken(newValue) }
                    },
                    label = { Text("Session Token") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        sessionManager.resetSession()
                        tokenInput = ""
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Reset Session")
                }
            }
        }

        if (!sessionToken.isNullOrEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Paiement", style = MaterialTheme.typography.titleMedium)
                    PaymentBox(
                        sessionToken,
                        sdkContext = MnxtSDKContext(
                            environment = selectedEnv,
                            config = MnxtSDKConfiguration(language = selectedLang),
                            appearance = selectedTheme.appearance()
                        ),
                        onResult = { result ->
                            when (result) {
                                is PaymentResult.SheetDismissed ->
                                 Log.d("APP", "SheetDismissed: " + (result.currentState?.toString() ?: "TransactionState UNKNOWN"))
                                is PaymentResult.PaymentCompleted ->
                                 Log.d("APP", "PaymentCompleted: " + (result.finalState?.toString() ?: "TransactionState UNKNOWN"))
                            }
                        }
                    ) { showPaymentSheet ->
                        Button(showPaymentSheet) {
                            Text("Checkout")
                        }
                    }
                }
            }
        }
    }
}