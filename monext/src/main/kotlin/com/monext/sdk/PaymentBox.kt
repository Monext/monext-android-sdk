package com.monext.sdk

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * This composable handles payment sheet presentation upon user interaction.
 * Show it in your app when the payment session token as been created and the user is ready to begin the payment process.
 *
 * The lambda returned by the `clickableContent` composable must be invoked to show the `PaymentSheet`
 */
@Composable
fun PaymentBox(
    sessionToken: String?,
    sdkContext: MnxtSDKContext,
    modifier: Modifier = Modifier,
    onResult: (PaymentResult) -> Unit,
    clickableContent: @Composable BoxScope.(() -> Unit) -> Unit
) {

    var showPaymentSheet by rememberSaveable { mutableStateOf(false) }

    val onClick: () -> Unit = {
        if (sessionToken != null) {
            showPaymentSheet = true
        }
    }

    Box(modifier) {

        clickableContent(onClick)

        PaymentSheet(showPaymentSheet, sessionToken ?: "", sdkContext) { result ->
            showPaymentSheet = false
            onResult(result)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
internal fun PaymentButtonPreview() {

    Box(Modifier.fillMaxSize()) {
        PaymentBox(
            sessionToken = "fake_session_token",
            sdkContext = MnxtSDKContext(
                environment = MnxtEnvironment.Sandbox,
                appearance = Appearance(headerTitle = "Monext SDK Preview")
            ),
            onResult = {}
        ) {
            Text("CHECKOUT")
        }
    }
}