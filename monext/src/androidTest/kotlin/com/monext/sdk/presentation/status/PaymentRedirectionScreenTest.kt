package com.monext.sdk.presentation.status

import android.os.Environment
import android.os.StrictMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.monext.sdk.FakeTestActivity
import com.monext.sdk.LocalEnvironment
import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.internal.data.sessionstate.RedirectionData
import com.monext.sdk.internal.presentation.status.PaymentRedirectionScreen
import junit.framework.TestCase.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PaymentRedirectionScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    @Before
    fun setup() {
        // Désactiver StrictMode pour les tests si besoin
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
    }

    @Test
    fun paymentRedirectionScreen_displaysWebView() {
        // Given
        val redirectionData =  RedirectionData(
            requestType = "GET",
            requestUrl = "https://homologation-payment.payline.com",
            requestFields = mapOf(),
            iframeEmbeddable = true,
            iframeHeight = 1,
            iframeWidth = 1,
            timeoutInMs = 10_000,
            hasPartnerLogo = true,
            partnerLogoKey = "cb",
            isCompletionMethod = true
        )
        var onCompleteCalled = false

        // When
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(
                LocalEnvironment provides MnxtEnvironment.Sandbox
            ) {
                PaymentRedirectionScreen(
                    data = redirectionData,
                    onComplete = { onCompleteCalled = true }
                )
            }
        }

        // Then
        composeTestRule.waitForIdle()
        // Le composable se charge sans crash
        assertFalse(onCompleteCalled)
    }

}