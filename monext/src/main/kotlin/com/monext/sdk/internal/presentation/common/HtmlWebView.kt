package com.monext.sdk.internal.presentation.common

import android.graphics.Color
import android.view.View
import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Locale
import androidx.compose.ui.graphics.Color as ComposeColor

@Composable
fun HtmlWebView(
    html: String,
    enableJs: Boolean = false,
    transparent: Boolean = true,
    textColor: ComposeColor? = null,
    fontSizePx: Int? = null,
) {
    val colorCss = textColor?.let { composeColorToCss(it) }

    val css = buildString {
        append("<style>")
        append("html,body{background:transparent;margin:0;padding:0;}")
        if (colorCss != null || fontSizePx != null) {
            append("body{")
            colorCss?.let { append("color:$it;") }
            fontSizePx?.let { append("font-size:${it}px;") }
            append("}")
        }
        append("</style>")
    }

    val content = if (transparent) {
        "<html><head>$css</head><body>$html</body></html>"
    } else {
        "<html><head>$css</head><body style=\"background:white\">$html</body></html>"
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = enableJs
                renderHtml(content, transparent)
            }
        },
        update = { webView ->
            webView.renderHtml(content, transparent)
        }
    )
}

/**
 * Conversion utilitaire : Compose Color -> CSS color string.
 * internal pour permettre les tests unitaires.
 */
internal fun composeColorToCss(c: ComposeColor): String {
    val r = (c.red * 255.0f).toInt().coerceIn(0, 255)
    val g = (c.green * 255.0f).toInt().coerceIn(0, 255)
    val b = (c.blue * 255.0f).toInt().coerceIn(0, 255)
    val a = c.alpha
    return if (a >= 0.999f) {
        "rgb($r,$g,$b)"
    } else {
        val alphaStr = String.format(Locale.US, "%.2f", a)
        "rgba($r,$g,$b,$alphaStr)"
    }
}

/**
 * Extension helper pour centraliser la logique de rendu :
 * - appliquer la transparence / layer type
 * - charger le contenu HTML
 *
 * internal pour permettre les tests unitaires.
 */
internal fun WebView.renderHtml(content: String, transparent: Boolean) {
    if (transparent) {
        setBackgroundColor(Color.TRANSPARENT)
        // Sur certains devices, la transparence peut nécessiter le rendu logiciel.
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    } else {
        // Si on souhaite explicitement un fond blanc, on peut forcer le layer hardware.
        setBackgroundColor(Color.WHITE)
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }
    loadDataWithBaseURL(null, content, "text/html", "utf-8", null)
}