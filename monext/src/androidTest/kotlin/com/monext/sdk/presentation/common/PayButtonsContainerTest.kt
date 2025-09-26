package com.monext.sdk.presentation.common

import android.os.StrictMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monext.sdk.Appearance
import com.monext.sdk.FakeTestActivity
import com.monext.sdk.GooglePayConfiguration
import com.monext.sdk.LocalAppearance
import com.monext.sdk.SdkTestHelper.Companion.createPaymentMethodData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.presentation.common.PayButtonsContainer
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PayButtonsContainerTest {

    private val appearance = Appearance(headerTitle = "Monext Demo")

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    @Before
    fun setup() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
    }

    @Test
    fun payButton_isDisplayed_andEnabled_whenCanPayIsTrue() {
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PayButtonsContainer(
                    amount = "10.00€",
                    selectedPaymentMethod = PaymentMethod.AlternativePaymentMethod(
                        createPaymentMethodData("PAYPAL")
                    ),
                    canPay = true,
                    isLoading = false,
                    showsGooglePay = false,
                    gPayConfig = GooglePayConfiguration(),
                    allowedPaymentMethods = "",
                    onClickGooglePay = {},
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Pay 10.00€")
            .assertIsDisplayed()
            .assertIsEnabled()
    }

    @Test
    fun payButton_isNotEnabled_whenCanPayIsFalse() {
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PayButtonsContainer(
                    amount = "10.00€",
                    selectedPaymentMethod = PaymentMethod.AlternativePaymentMethod(
                        createPaymentMethodData("PAYPAL")
                    ),
                    canPay = false,
                    isLoading = false,
                    showsGooglePay = false,
                    gPayConfig = GooglePayConfiguration(),
                    allowedPaymentMethods = "",
                    onClickGooglePay = {},
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Pay 10.00€")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    @Test
    fun googlePayButton_isNotDisplayed_whenShowsGooglePayIsFalse() {
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PayButtonsContainer(
                    amount = "10.00€",
                    selectedPaymentMethod = PaymentMethod.AlternativePaymentMethod(
                        createPaymentMethodData("PAYPAL")
                    ),
                    canPay = true,
                    isLoading = false,
                    showsGooglePay = false,
                    gPayConfig = GooglePayConfiguration(),
                    allowedPaymentMethods = "CARD, TOKENIZED_CARD",
                    onClickGooglePay = {},
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("payButton").assertDoesNotExist()
    }
}