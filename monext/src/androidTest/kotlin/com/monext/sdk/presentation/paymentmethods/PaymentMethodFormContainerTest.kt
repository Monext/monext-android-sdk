package com.monext.sdk.presentation.paymentmethods

import android.os.StrictMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.monext.sdk.Appearance
import com.monext.sdk.FakeTestActivity
import com.monext.sdk.LocalAppearance
import com.monext.sdk.SdkTestHelper.Companion.createPaymentMethodData
import com.monext.sdk.internal.api.model.PaymentMethodCardCode
import com.monext.sdk.internal.data.LocalSessionStateRepo
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.SessionStateRepository
import com.monext.sdk.internal.presentation.paymentmethods.PaymentMethodFormContainer
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PaymentMethodFormContainerTest {

    private val appearance = Appearance(
        headerTitle = "Monext Demo"
    )

    @RelaxedMockK
    private lateinit var sessionStateRepositoryMock : SessionStateRepository

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
    }

    @Test
    fun  paymentMethodFormContainer_displaysAlternativeForm_forPaypal(){
        val paymentMethodData = createPaymentMethodData(
            cardCode = "PAYPAL",
            hasForm = true
        )
        val paymentMethod = PaymentMethod.fromData(paymentMethodData)

        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentMethodFormContainer(paymentMethod, {})
            }
        }

        composeTestRule
            .onNodeWithTag("AlternativePaymentMethodForm")
            .assertIsDisplayed()
    }

    @Test
    fun paymentMethodFormContainer_displaysForm_forCB() {
        val paymentMethodData = createPaymentMethodData(cardCode = PaymentMethodCardCode.CB)
        val paymentMethodCB = PaymentMethod.CB(paymentMethodData)
        val paymentMethod = PaymentMethod.Cards(listOf(paymentMethodCB))

        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(
                LocalAppearance provides appearance,
                LocalSessionStateRepo provides sessionStateRepositoryMock,
            ) {
                PaymentMethodFormContainer(paymentMethod, {})
            }
        }

        composeTestRule
            .onNodeWithTag("CardForm")
            .assertIsDisplayed()

    }

    @Test
    fun paymentMethodFormContainer_displaysForm_forUnimplementedPaymentMethod() {
        val paymentMethodData = createPaymentMethodData(cardCode = "NULL", hasForm = false)
        val paymentMethod = PaymentMethod.fromData(paymentMethodData)

        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(
                LocalAppearance provides appearance,
            ) {
                PaymentMethodFormContainer(paymentMethod, {})
            }
        }

        composeTestRule
            .onNodeWithText("Selected payment method not implemented.")
            .assertIsDisplayed()

    }

}