package com.monext.sdk.presentation.common

import android.os.StrictMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil3.ColorImage
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.test.FakeImageLoaderEngine
import com.monext.sdk.Appearance
import com.monext.sdk.FakeTestActivity
import com.monext.sdk.LocalAppearance
import com.monext.sdk.internal.presentation.common.PaymentMethodChip
import com.monext.sdk.internal.presentation.common.PaymentMethodImageWithFallback
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentMethodChipTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    private val appearance = Appearance(
        headerTitle = "Monext Demo"
    )

    @OptIn(DelicateCoilApi::class)
    @Before
    fun setup() {
        // Désactiver StrictMode pour les tests si besoin
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
        // Réinitialiser le singleton Coil avant chaque test
        SingletonImageLoader.reset()
    }

    private val cbCode = "CB"
    private val visaCode = "VISA"
    private val mastercardCode = "MASTERCARD"
    private val amexCode = "AMEX"

    @Test
    fun paymentMethodChip_showsCbLogo_whenCardCodeIsCB() {
        setChipContent(cardCode = cbCode)
        composeTestRule.onNodeWithTag("CBLogo").assertIsDisplayed()
    }

    @Test
    fun paymentMethodChip_showsVisaLogo_whenCardCodeIsVisa() {
        setChipContent(cardCode = visaCode)
        composeTestRule.onNodeWithTag("VisaLogo").assertIsDisplayed()
    }

    @Test
    fun paymentMethodChip_showsMastercardLogo_whenCardCodeIsMastercard() {
        setChipContent(cardCode = mastercardCode)
        composeTestRule.onNodeWithTag("MastercardLogo").assertIsDisplayed()
    }

    @Test
    fun paymentMethodChip_showsAmexLogo_whenCardCodeIsAmex() {
        setChipContent(cardCode = amexCode)
        composeTestRule.onNodeWithTag("AmexLogo").assertIsDisplayed()
    }

    @OptIn(ExperimentalCoilApi::class)
    @Test
    fun paymentMethodImageWithFallback_shouldDisplayProgressIndicator() {
        // Given
        val logoUrl = "https://example.com/logo.png"
        val fallbackText = "Logo"

        // When
        composeTestRule.activity.setTestComposable {
            PaymentMethodImageWithFallback(
                logoUrl = logoUrl,
                fallbackText = fallbackText
            )
        }

        // Then
        composeTestRule.onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @OptIn(ExperimentalCoilApi::class)
    @Test
    fun paymentMethodImageWithFallback_shouldDisplayImage() {
        // Given
        val logoUrl = "https://example.com/logo.png"
        val fallbackText = "Logo"
        val context = composeTestRule.activity

        val engine = FakeImageLoaderEngine.Builder()
            .intercept(logoUrl, ColorImage())
            .build()

        val imageLoader = ImageLoader.Builder(context)
            .components { add(engine) }
            .build()

        // When
        composeTestRule.activity.setTestComposable {
            setSingletonImageLoaderFactory { imageLoader }
            PaymentMethodImageWithFallback(
                logoUrl = logoUrl,
                fallbackText = fallbackText
            )
        }
        // Then
        composeTestRule.onNodeWithText(fallbackText).assertDoesNotExist()
        composeTestRule.onNodeWithTag("paymentMethod_image").assertIsDisplayed()

    }

    @Test
    fun paymentMethodImageWithFallback_showsFallbackText_whenLogoNull() {
        val fallbackText = "Logo"
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentMethodImageWithFallback(logoUrl = null, fallbackText = fallbackText)
            }
        }
        composeTestRule.onNodeWithText(fallbackText).assertIsDisplayed()
        composeTestRule.onNodeWithTag("fallback_text").assertIsDisplayed()
    }

    @Test
    fun paymentMethodChip_showsCardText_whenCardCodeNull() {
        setChipContent(cardCode = null)
        composeTestRule.onNodeWithTag("CardLogo").assertIsDisplayed()
    }

    private fun setChipContent(cardCode: String?) {
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentMethodChip(cardCode = cardCode, isExpanded = false, showsBack = false)
            }
        }
    }
}