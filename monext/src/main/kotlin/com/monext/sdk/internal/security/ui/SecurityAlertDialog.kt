package com.monext.sdk.internal.security.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
internal fun SecurityAlertDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { /* non cancelable */ },
        title = { Text("Appareil non sécurisé") },
        text = {
            Text(
                "Votre appareil ne répond pas aux exigences de sécurité " +
                        "requises pour effectuer un paiement."
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        }
    )
}