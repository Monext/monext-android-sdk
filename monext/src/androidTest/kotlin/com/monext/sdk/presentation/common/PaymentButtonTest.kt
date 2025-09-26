package com.monext.sdk.presentation.common

import android.os.StrictMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monext.sdk.Appearance
import com.monext.sdk.FakeTestActivity
import com.monext.sdk.LocalAppearance
import com.monext.sdk.SdkTestHelper.Companion.createPaymentMethodData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.sessionstate.PaymentForm
import com.monext.sdk.internal.presentation.common.PaymentButton
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentButtonTest {

    private val appearance = Appearance(headerTitle = "Monext Demo")

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    @Before
    fun setup() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
    }

    @Test
    fun paymentButton_without_selectedPaymentMethod_disabled_noLoader() {
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentButton("100€", null, canPay = false, isLoading = false) {}
            }
        }
        composeTestRule.onNodeWithTag("PaymentButtonText", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextContains("Pay 100€")
        composeTestRule.onNodeWithTag("PaymentButton").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("PaymentButtonLoader").assertIsNotDisplayed()
    }

    @Test
    fun paymentButton_without_selectedPaymentMethod_enabled_noLoader() {
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentButton("100€", null, canPay = true, isLoading = false) {}
            }
        }
        composeTestRule.onNodeWithTag("PaymentButtonText", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextContains("Pay 100€")
        composeTestRule.onNodeWithTag("PaymentButton").assertIsEnabled()
        composeTestRule.onNodeWithTag("PaymentButtonLoader").assertDoesNotExist()
    }

    @Test
    fun paymentButton_without_selectedPaymentMethod_enabled_withLoader() {
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentButton("100€", null, canPay = true, isLoading = true) {}
            }
        }
        composeTestRule.onNodeWithTag("PaymentButtonLoader").assertIsDisplayed()
        composeTestRule.onNodeWithTag("PaymentButtonText", useUnmergedTree = true).assertIsNotDisplayed()
    }

    @Test
    fun paymentButton_with_selectedPaymentMethod_enabled_noLoader() {
        val paymentForm = PaymentForm(
            displayButton = true,
            buttonText = "Continue with Paypal"
        )
        val paymentMethodData = createPaymentMethodData("PAYPAL", true, paymentForm)
        val paymentMethod = PaymentMethod.AlternativePaymentMethod(paymentMethodData)
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentButton("100€", paymentMethod, canPay = true, isLoading = false) {}
            }
        }
        composeTestRule.onNodeWithTag("PaymentButtonText", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextContains("Continue with Paypal")
        composeTestRule.onNodeWithTag("PaymentButton").assertIsEnabled()
        composeTestRule.onNodeWithTag("PaymentButtonLoader").assertDoesNotExist()
    }

    @Test
    fun paymentButton_with_selectedPaymentMethod_disabled_noLoader() {
        val paymentForm = PaymentForm(
            displayButton = true,
            buttonText = "Continue with Paypal"
        )
        val paymentMethodData = createPaymentMethodData("PAYPAL", true, paymentForm)
        val paymentMethod = PaymentMethod.AlternativePaymentMethod(paymentMethodData)
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentButton("100€", paymentMethod, canPay = false, isLoading = false) {}
            }
        }
        composeTestRule.onNodeWithTag("PaymentButtonText", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextContains("Continue with Paypal")
        composeTestRule.onNodeWithTag("PaymentButton").assertIsNotEnabled()
    }

    @Test
    fun paymentButton_clickCallback_invoked_whenEnabled() {
        var clicked = false
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentButton("100", null, canPay = true, isLoading = false) { clicked = true }
            }
        }
        composeTestRule.onNodeWithTag("PaymentButton").performClick()
        assert(clicked)
    }
}