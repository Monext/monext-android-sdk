package com.monext.sdk.presentation.paymentmethods

import android.os.StrictMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.monext.sdk.Appearance
import com.monext.sdk.FakeTestActivity
import com.monext.sdk.LocalAppearance
import com.monext.sdk.SdkTestHelper.Companion.createPaymentMethodData
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.presentation.common.PaymentMethodChip
import com.monext.sdk.internal.presentation.paymentmethods.AlternativePaymentMethodForm
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AlternativePaymentMethodFormTest {

    private val appearance = Appearance(
        headerTitle = "Monext Demo"
    )

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    @Before
    fun setup() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
    }

    @Test
    fun saveCardCheckbox_isDisplayedAndCanBeClicked() {
        val paymentMethodData = createPaymentMethodData(
            cardCode = "PAYPAL",
            hasForm = true
        )
        val paymentMethod = PaymentMethod.AlternativePaymentMethod(paymentMethodData)
        var validatedForm: Any? = null


        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                AlternativePaymentMethodForm(paymentMethod) {
                    validatedForm = it
                }
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag("saveCardCheckbox")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("saveCardCheckbox")
            .performClick()

        composeTestRule.waitForIdle()

        assert(validatedForm != null)
        assert((validatedForm as? FormData.AlternativePaymentMethodForm)?.saveCard == true)
    }
}