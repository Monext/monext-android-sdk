package com.monext.sdk.internal.presentation.status

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import com.monext.sdk.LocalEnvironment
import com.monext.sdk.internal.data.sessionstate.RedirectionData

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
@Composable
internal fun PaymentRedirectionScreen(data: RedirectionData, onComplete: () -> Unit) {

    val targetUrl = data.requestUrl
    val postData = data.requestFields?.entries
        ?.joinToString("&") { (k, v) -> "$k=$v" }
        ?.toByteArray()

    val redirectionUrl: String = Uri.Builder()
        .scheme("https")
        .authority(LocalEnvironment.current.host)
        .toString()

    Box(Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    settings.javaScriptEnabled = true
                    settings.useWideViewPort = true
                    settings.loadWithOverviewMode = true
                    webViewClient = redirectionWebClient(redirectionUrl, onComplete)
                    setOnTouchListener { v, event ->
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                        v.onTouchEvent(event)
                    }
                }
            },
            update = { webView ->
                when (data.requestType.uppercase()) {
                    "POST" -> {
                        if (postData != null && postData.isNotEmpty()) {
                            webView.postUrl(targetUrl, postData)
                        } else {
                            webView.loadUrl(targetUrl)
                        }
                    }
                    else -> webView.loadUrl(targetUrl)
                }
            }
        )
    }
}

internal fun redirectionWebClient(redirectUrl: String, onFoundRedirect: () -> Unit) = object : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url?.startsWith(redirectUrl) == true) {
            view?.post { view.visibility = View.GONE }
            onFoundRedirect()
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return shouldOverrideUrlLoadingCompat(request?.url, view)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return shouldOverrideUrlLoadingCompat(url?.toUri(), view)
    }

    private fun shouldOverrideUrlLoadingCompat(url: Uri?, view: WebView?): Boolean {
        if (url == null) return false
        val context = view?.context ?: return false

        if (isPaylineCallback(url, redirectUrl)) {
            view.post { view.visibility = View.GONE }
            onFoundRedirect()
            return true
        }

        if (isExternalScheme(url.scheme)) {
            return handleDeeplink(context, url)
        }

        return false
    }
}

private fun isPaylineCallback(url: Uri, redirectUrl: String): Boolean {
    return url.toString().startsWith(redirectUrl)
            && url.getQueryParameter("paylinetoken") != null
            && url.getQueryParameter("paymentEndpoint") == "1"
}

private fun isExternalScheme(scheme: String?): Boolean {
    return scheme != null && scheme != "http" && scheme != "https"
}

private fun handleDeeplink(context: Context, uri: Uri): Boolean {
    return when (uri.scheme?.lowercase()) {
        "intent" -> handleIntentScheme(context, uri)
        else -> launchExternalApp(context, uri)
    }
}

private fun handleIntentScheme(context: Context, uri: Uri): Boolean {
    val intent = runCatching {
        Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME)
            .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    }.getOrElse { return false }

    return runCatching { context.startActivity(intent); true }
        .getOrElse { intent.`package`?.let { openPlayStore(context, it) } ?: false }
}

private fun launchExternalApp(context: Context, uri: Uri): Boolean {
    return try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        )
        true
    } catch (e: ActivityNotFoundException) {
        true
    }
}

private fun openPlayStore(context: Context, packageName: String): Boolean {
    return try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
                .apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        )
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}