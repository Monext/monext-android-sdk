package com.monext.sdk.internal.presentation.status

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
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
                    webViewClient = redirectionWebClient(redirectionUrl, onComplete)
                    // NOTE: Fix for nested scrolling not working, disables sheet dismissal inside webview
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
                    else -> {
                        webView.loadUrl(targetUrl)
                    }
                }
            }
        )
    }
}

internal fun redirectionWebClient(redirectUrl: String, onFoundRedirect: () -> Unit) = object: WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        if (url?.startsWith(redirectUrl) == true) {
            onFoundRedirect()
        }
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return shouldOverrideUrlLoadingCompat(request?.url)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        return shouldOverrideUrlLoadingCompat(url?.toUri())
    }

    private fun shouldOverrideUrlLoadingCompat(url: Uri?): Boolean {
        val isRedirectUrl = url?.toString()?.startsWith(redirectUrl) == true
        val token = url?.getQueryParameter("paylinetoken")
        val paymentEndpoint = url?.getQueryParameter("paymentEndpoint")

        return if (isRedirectUrl && token != null && paymentEndpoint == "1") {
            onFoundRedirect()
            true
        } else {
            false
        }
    }
}