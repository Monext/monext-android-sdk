package com.monext.sdk.internal.presentation.status

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.monext.sdk.internal.data.sessionstate.FormScript
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Écran utilisé pour l'état [com.monext.sdk.internal.api.model.response.SessionStateType.PAYMENT_REDIRECT_WITH_JAVASCRIPT].
 *
 * Cas d'usage : exécuter un script Javascript fourni par le backend (ex : empreinte device PayPal)
 * SANS aucune interaction de l'acheteur. On affiche uniquement un spinner, on charge le script dans
 * une WebView invisible, on attend un court délai, puis on déclenche automatiquement le paiement.
 */
const val JAVASCRIPT_EXECUTION_DELAY_MS = 500L

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun PaymentJavascriptRedirectionScreen(
    formScript: FormScript,
    onScriptExecuted: () -> Unit
) {
    val htmlContent = remember(formScript) { formScript.htmlContent() }
    var pageFinished by remember { mutableStateOf(false) }
    val currentOnScriptExecuted by rememberUpdatedState(onScriptExecuted)

    // Si le script est absent, on déclenche directement le paiement
    LaunchedEffect(htmlContent) {
        if (htmlContent == null) {
            currentOnScriptExecuted()
        }
    }

    // Une fois la page chargée, on attend un court délai pour laisser le JS s'exécuter,
    // puis on déclenche le paiement programmatiquement.
    LaunchedEffect(pageFinished) {
        if (pageFinished) {
            delay(JAVASCRIPT_EXECUTION_DELAY_MS.milliseconds)
            currentOnScriptExecuted()
        }
    }

    // Spinner affiché à l'acheteur, centré au-dessus de la WebView (invisible) pendant
    // l'exécution du Javascript (seul élément visible pour l'acheteur).
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (htmlContent != null) {
            // WebView invisible (taille 0) : on ne veut rien afficher à l'acheteur.
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(0, 0)
                        settings.javaScriptEnabled = true
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                pageFinished = true
                            }
                        }
                    }
                },
                update = { webView ->
                    webView.loadDataWithBaseURL(
                        null,
                        htmlContent,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            )
        }

        // Loader circulaire affiché pendant l'exécution du Javascript.
        LoadingSection()
    }
}
