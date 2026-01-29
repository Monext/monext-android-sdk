package com.monext.sdk.presentation.status

import android.net.Uri
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import androidx.core.net.toUri
import com.monext.sdk.internal.presentation.status.redirectionWebClient
import io.mockk.*
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class RedirectionWebClientTest {

    private lateinit var mockWebView: WebView
    private lateinit var onFoundRedirect: () -> Unit
    private val redirectUrl = "https://sandbox.payline.com"

    @Before
    fun setup() {
        mockWebView = mockk(relaxed = true)
        onFoundRedirect = mockk(relaxed = true)

        // Mock des méthodes statiques Android si nécessaire
        mockkStatic(Uri::class)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun onPageStarted_sets_visibility_to_GONE_when_URL_matches_redirect_URL() {
        // Given
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val testUrl = "https://sandbox.payline.com/callback"

        // Slot pour capturer le Runnable passé à post()
        val runnableSlot = slot<Runnable>()
        every { mockWebView.post(capture(runnableSlot)) } answers {
            // Exécuter immédiatement le runnable pour simuler le comportement
            runnableSlot.captured.run()
            true
        }

        // When
        client.onPageStarted(mockWebView, testUrl, null)

        // Then
        verify { mockWebView.post(any()) }
        verify { mockWebView.visibility = View.GONE }
        verify { onFoundRedirect() }
    }

    @Test
    fun onPageStarted_does_not_set_visibility_when_URL_does_not_match() {
        // Given
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val testUrl = "https://other-domain.com"

        // When
        client.onPageStarted(mockWebView, testUrl, null)

        // Then
        verify(exactly = 0) { mockWebView.post(any()) }
        verify(exactly = 0) { mockWebView.visibility = View.GONE }
        verify(exactly = 0) { onFoundRedirect() }
    }

    @Test
    fun shouldOverrideUrlLoading_sets_visibility_to_GONE_with_valid_redirect_parameters() {
        // Given
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val testUrl = "https://sandbox.payline.com?paylinetoken=abc123&paymentEndpoint=1"

        val realUri = Uri.parse(testUrl)

        val mockRequest = mockk<WebResourceRequest> {
            every { url } returns realUri
        }

        val runnableSlot = slot<Runnable>()
        every { mockWebView.post(capture(runnableSlot)) } answers {
            runnableSlot.captured.run()
            true
        }

        every { mockWebView.visibility = any() } just Runs

        // When
        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        // Then
        assertTrue(result)
        verify { mockWebView.post(any()) }
        verify { mockWebView.visibility = View.GONE }
        verify { onFoundRedirect() }
    }

    @Test
    fun shouldOverrideUrlLoading_does_not_override_when_token_is_missing() {
        // Given
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val testUrl = "https://sandbox.payline.com?paymentEndpoint=1"

        val realUri = Uri.parse(testUrl)

        val mockRequest = mockk<WebResourceRequest> {
            every { url } returns realUri
        }

        // When
        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        // Then
        assertFalse(result)
        verify(exactly = 0) { mockWebView.post(any()) }
        verify(exactly = 0) { mockWebView.visibility = View.GONE }
        verify(exactly = 0) { onFoundRedirect() }
    }

    @Test
    fun shouldOverrideUrlLoading_does_not_override_when_paymentEndpoint_is_not_1() {
        // Given
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val testUrl = "https://sandbox.payline.com?paylinetoken=abc123&paymentEndpoint=0"

        val realUri = Uri.parse(testUrl)

        val mockRequest = mockk<WebResourceRequest> {
            every { url } returns realUri
        }

        // When
        val result = client.shouldOverrideUrlLoading(mockWebView, mockRequest)

        // Then
        assertFalse(result)
        verify(exactly = 0) { mockWebView.post(any()) }
        verify(exactly = 0) { onFoundRedirect() }
    }

    @Test
    fun shouldOverrideUrlLoading_string_variant_sets_visibility_correctly() {
        // Given
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val testUrl = "https://sandbox.payline.com?paylinetoken=abc123&paymentEndpoint=1"

        val realUri = Uri.parse(testUrl)

        every { testUrl.toUri() } returns realUri

        val runnableSlot = slot<Runnable>()
        every { mockWebView.post(capture(runnableSlot)) } answers {
            runnableSlot.captured.run()
            true
        }

        // When
        val result = client.shouldOverrideUrlLoading(mockWebView, testUrl)

        // Then
        assertTrue(result)
        verify { mockWebView.post(any()) }
        verify { mockWebView.visibility = View.GONE }
        verify { onFoundRedirect() }
    }

    @Test
    fun onPageStarted_handles_null_WebView_gracefully() {
        // Given
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)
        val testUrl = "https://sandbox.payline.com/callback"

        // When
        client.onPageStarted(null, testUrl, null)

        // Then
        verify { onFoundRedirect() }
        // Pas de crash
    }

    @Test
    fun onPageStarted_handles_null_URL_gracefully() {
        // Given
        val client = redirectionWebClient(redirectUrl, onFoundRedirect)

        // When
        client.onPageStarted(mockWebView, null, null)

        // Then
        verify(exactly = 0) { mockWebView.post(any()) }
        verify(exactly = 0) { onFoundRedirect() }
    }
}