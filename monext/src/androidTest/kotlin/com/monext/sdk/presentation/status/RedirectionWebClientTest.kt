package com.monext.sdk.presentation.status

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.core.net.toUri
import com.monext.sdk.internal.presentation.status.redirectionWebClient
import io.mockk.*
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class RedirectionWebClientTest {

    private lateinit var mockWebView: WebView
    private lateinit var mockContext: Context
    private lateinit var mockPackageManager: PackageManager
    private lateinit var onFoundRedirect: () -> Unit
    private val redirectUrl = "https://sandbox.payline.com"

    @Before
    fun setup() {
        mockWebView = mockk(relaxed = true)
        mockContext = mockk(relaxed = true)
        mockPackageManager = mockk(relaxed = true)
        onFoundRedirect = mockk(relaxed = true)

        every { mockWebView.context } returns mockContext
        every { mockContext.packageManager } returns mockPackageManager
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun onPageStarted_sets_visibility_to_GONE_when_URL_matches_redirect_URL() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val runnableSlot = slot<Runnable>()
        every { mockWebView.post(capture(runnableSlot)) } answers {
            runnableSlot.captured.run()
            true
        }

        client.onPageStarted(mockWebView, "https://sandbox.payline.com/callback", null)

        verify { mockWebView.post(any()) }
        verify { mockWebView.visibility = View.GONE }
        verify { onFoundRedirect() }
    }

    @Test
    fun onPageStarted_does_not_set_visibility_when_URL_does_not_match() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)

        client.onPageStarted(mockWebView, "https://other-domain.com", null)

        verify(exactly = 0) { mockWebView.post(any()) }
        verify(exactly = 0) { mockWebView.visibility = View.GONE }
        verify(exactly = 0) { onFoundRedirect() }
    }

    @Test
    fun onPageStarted_handles_null_WebView_gracefully() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)

        client.onPageStarted(null, "https://sandbox.payline.com/callback", null)

        verify { onFoundRedirect() }
    }

    @Test
    fun onPageStarted_handles_null_URL_gracefully() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)

        client.onPageStarted(mockWebView, null, null)

        verify(exactly = 0) { mockWebView.post(any()) }
        verify(exactly = 0) { onFoundRedirect() }
    }

    @Test
    fun shouldOverrideUrlLoading_sets_visibility_to_GONE_with_valid_redirect_parameters() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val uri = Uri.parse("https://sandbox.payline.com?paylinetoken=abc123&paymentEndpoint=1")
        val mockRequest = mockk<WebResourceRequest> { every { url } returns uri }
        val runnableSlot = slot<Runnable>()
        every { mockWebView.post(capture(runnableSlot)) } answers {
            runnableSlot.captured.run()
            true
        }

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertTrue(result)
        verify { mockWebView.visibility = View.GONE }
        verify { onFoundRedirect() }
    }

    @Test
    fun shouldOverrideUrlLoading_does_not_override_when_token_is_missing() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val uri = Uri.parse("https://sandbox.payline.com?paymentEndpoint=1")
        val mockRequest = mockk<WebResourceRequest> { every { url } returns uri }

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertFalse(result)
        verify(exactly = 0) { onFoundRedirect() }
    }

    @Test
    fun shouldOverrideUrlLoading_does_not_override_when_paymentEndpoint_is_not_1() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val uri = Uri.parse("https://sandbox.payline.com?paylinetoken=abc123&paymentEndpoint=0")
        val mockRequest = mockk<WebResourceRequest> { every { url } returns uri }

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertFalse(result)
        verify(exactly = 0) { onFoundRedirect() }
    }

    @Test
    fun shouldOverrideUrlLoading_string_variant_sets_visibility_correctly() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val testUrl = "https://sandbox.payline.com?paylinetoken=abc123&paymentEndpoint=1"


        val runnableSlot = slot<Runnable>()
        every { mockWebView.post(capture(runnableSlot)) } answers {
            runnableSlot.captured.run()
            true
        }

        val result = client.shouldOverrideUrlLoading(mockWebView, testUrl)

        assertTrue(result)
        verify { mockWebView.visibility = View.GONE }
        verify { onFoundRedirect() }
    }

    @Test
    fun shouldOverrideUrlLoading_returns_false_when_url_is_null() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val mockRequest = mockk<WebResourceRequest> { every { url } returns null }

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertFalse(result)
        verify(exactly = 0) { onFoundRedirect() }
    }

    @Test
    fun shouldOverrideUrlLoading_launches_app_when_custom_scheme_is_resolved() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val uri = Uri.parse("alipays://platformapi/startapp")
        val mockRequest = mockk<WebResourceRequest> { every { url } returns uri }

        val resolveInfo = ResolveInfo().apply { activityInfo = ActivityInfo().apply { packageName = "com.eg.android.AlipayGphone" } }
        every { mockPackageManager.resolveActivity(any(), PackageManager.MATCH_DEFAULT_ONLY) } returns resolveInfo

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertTrue(result)
        verify { mockContext.startActivity(any()) }
        verify(exactly = 0) { onFoundRedirect() }
    }

    @Test
    fun shouldOverrideUrlLoading_opens_play_store_when_app_not_installed() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val uri = Uri.parse("alipays://platformapi/startapp")
        val mockRequest = mockk<WebResourceRequest> { every { url } returns uri }

        every { mockPackageManager.resolveActivity(any(), PackageManager.MATCH_DEFAULT_ONLY) } returns null

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertTrue(result)
        // Vérifie que le Play Store est ouvert avec une recherche sur le schéma
        verify {
            mockContext.startActivity(match {
                it.data?.toString()?.contains("alipays") == true
            })
        }
    }

    @Test
    fun shouldOverrideUrlLoading_returns_true_when_app_not_found_and_play_store_absent() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val uri = Uri.parse("unknownscheme://action")
        val mockRequest = mockk<WebResourceRequest> { every { url } returns uri }

        every { mockContext.startActivity(any()) } throws ActivityNotFoundException()

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertTrue(result)
    }

    @Test
    fun shouldOverrideUrlLoading_does_not_intercept_plain_https_url() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val uri = Uri.parse("https://external-payment-page.com")
        val mockRequest = mockk<WebResourceRequest> { every { url } returns uri }

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertFalse(result)
        verify(exactly = 0) { mockContext.startActivity(any()) }
    }

    @Test
    fun shouldOverrideUrlLoading_does_not_intercept_plain_http_url() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val uri = Uri.parse("http://insecure-page.com")
        val mockRequest = mockk<WebResourceRequest> { every { url } returns uri }

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertFalse(result)
        verify(exactly = 0) { mockContext.startActivity(any()) }
    }

    @Test
    fun shouldOverrideUrlLoading_handles_intent_scheme_when_app_is_installed() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val uri = Uri.parse("intent://platformapi/startapp#Intent;scheme=alipays;package=com.eg.android.AlipayGphone;end")
        val mockRequest = mockk<WebResourceRequest> { every { url } returns uri }

        mockkStatic(Intent::class)
        val mockIntent = mockk<Intent>(relaxed = true)
        every { Intent.parseUri(any(), Intent.URI_INTENT_SCHEME) } returns mockIntent
        every { mockIntent.resolveActivity(mockPackageManager) } returns mockk()

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertTrue(result)
        verify { mockContext.startActivity(mockIntent) }
    }

    @Test
    fun shouldOverrideUrlLoading_handles_intent_scheme_fallback_to_play_store_when_app_not_installed() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val uri = Uri.parse("intent://platformapi/startapp#Intent;scheme=alipays;package=com.eg.android.AlipayGphone;end")
        val mockRequest = mockk<WebResourceRequest> { every { url } returns uri }

        mockkStatic(Intent::class)
        val mockIntent = mockk<Intent>(relaxed = true)
        every { Intent.parseUri(any(), Intent.URI_INTENT_SCHEME) } returns mockIntent
        every { mockIntent.`package` } returns "com.eg.android.AlipayGphone"

        // Premier startActivity (app) → lève ActivityNotFoundException
        // Deuxième startActivity (Play Store) → succès
        val intentSlot = slot<Intent>()
        every { mockContext.startActivity(capture(intentSlot)) } throws ActivityNotFoundException() andThen Unit

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertTrue(result)
        // Vérifie que le dernier intent est bien le Play Store
        assertEquals(
            "market://details?id=com.eg.android.AlipayGphone",
            intentSlot.captured.data?.toString()
        )
    }

    @Test
    fun shouldOverrideUrlLoading_returns_false_when_intent_scheme_is_malformed() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val uri = Uri.parse("intent://malformed_intent_without_end")
        val mockRequest = mockk<WebResourceRequest> { every { url } returns uri }

        mockkStatic(Intent::class)
        every { Intent.parseUri(any(), Intent.URI_INTENT_SCHEME) } throws Exception("malformed")

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertFalse(result)
    }

    @Test
    fun shouldOverrideUrlLoading_returns_false_when_context_is_null() {
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val uri = Uri.parse("alipays://platformapi/startapp")
        val mockRequest = mockk<WebResourceRequest> { every { url } returns uri }

        every { mockWebView.context } returns null

        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        assertFalse(result)
    }
}