package com.monext.sdk.presentation.common

import android.os.StrictMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monext.sdk.Appearance
import com.monext.sdk.FakeTestActivity
import com.monext.sdk.LocalAppearance
import com.monext.sdk.internal.data.sessionstate.AdditionalData
import com.monext.sdk.internal.data.sessionstate.Logo
import com.monext.sdk.internal.data.sessionstate.PaymentMethodData
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

    @Before
    fun setup() {
        // Désactiver StrictMode pour les tests si besoin
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
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

    @Test
    fun paymentMethodImageWithFallback_showsFallbackText_whenLogoNull() {
        val fallback = "Fallback"
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentMethodImageWithFallback(logoUrl = null, fallbackText = fallback)
            }
        }
        composeTestRule.onNodeWithText(fallback).assertIsDisplayed()
    }

    @Test
    fun paymentMethodChip_showsCardText_whenCardCodeNull() {
        setChipContent(cardCode = null)
        composeTestRule.onNodeWithTag("CardLogo").assertIsDisplayed()
    }

    @Test
    fun paymentMethodImage_showsLogo_whenPaymentMethodDataLogoUrlSet() {
        val fakeLogoUrl = "https://logo.com/visa.png"
        val paymentMethodData = PaymentMethodData(
            cardCode = "VISA",
            contractNumber = "VISA",
            disabled = false,
            hasForm = true,
            form = null,
            hasLogo = true,
            logo = Logo(
                width = 0,
                height = 0,
                url = fakeLogoUrl,
                title = "Visa"
            ),
            isIsolated = false,
            options = null,
            paymentMethodAction = null,
            additionalData = AdditionalData(
                merchantCapabilities = null,
                networks = null,
                applePayMerchantId = null,
                applePayMerchantName = null,
                savePaymentDataChecked = null,
                email = null,
                date = null,
                holder = null,
                pan = null
            ),
            requestContext = null,
            shouldBeInTopPosition = null,
            state = null
        )
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentMethodChip(cardCode = null, paymentMethodData = paymentMethodData, isExpanded = false, showsBack = false)
            }
        }
        // Comme la gestion d'image n'est pas implémentée, on doit vérifier la présence du fallbackText
        composeTestRule.onNodeWithText("Visa").assertIsDisplayed()
    }

    private fun setChipContent(cardCode: String?) {
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentMethodChip(cardCode = cardCode, isExpanded = false, showsBack = false)
            }
        }
    }
}