package com.monext.sdk.presentation.paymentmethods

import android.os.StrictMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx. compose.ui.test.assertIsDisplayed
import androidx.compose. ui.test.junit4.createAndroidComposeRule
import androidx. compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose. ui.test.performClick
import androidx.compose.ui. test.performTextInput
import com.monext.sdk. Appearance
import com.monext.sdk.FakeTestActivity
import com.monext.sdk.LocalAppearance
import com.monext.sdk.SdkTestHelper. Companion.createPaymentMethodData
import com.monext.sdk.internal.data.FormData
import com.monext. sdk.internal.data.PaymentMethod
import com.monext. sdk.internal.data.sessionstate.PaymentForm
import com.monext.sdk.internal.data.sessionstate.PaymentMethodFieldValidation
import com.monext. sdk.internal.data.sessionstate.PaymentMethodFormField
import com.monext.sdk.internal.presentation.paymentmethods.AlternativePaymentMethodForm
import junit.framework.TestCase. assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AlternativePaymentMethodFormTest {

    companion object {
        private const val CARD_CODE_PAYPAL = "PAYPAL"
        private const val CARD_CODE_MBWAY = "MBWAY_MNXT"
        private const val PHONE_FIELD_KEY = "PHONENUMBER"
        private const val VALID_PHONE = "+331456067264"
        private const val INVALID_PHONE = "AAA"
        private const val PHONE_REGEX = "^(\\+)[1-9]\\d{1,18}$"
        private const val PHONE_PLACEHOLDER = "+351XXXXXXXXXX"
        private const val FORM_DESCRIPTION = "Voici une description"
        private const val BUTTON_TEXT = "Continuer vers MBWay"
        private const val FORM_TYPE = "CUSTOM"
        private const val FIELD_LABEL = "Numéro de téléphone mobile"
        private const val ERROR_MESSAGE = "Numéro de téléphone incorrect"
        private const val FIELD_ICON = "phone"
        private const val INPUT_TYPE = "TEL"
        private const val FORM_INPUT_FIELD_TYPE = "TEXT"
        private const val FORM_FIELD_TYPE = "INPUT"
        private const val SAVE_CARD_CHECKBOX_TAG = "saveCardCheckbox"
    }

    private val appearance = Appearance(
        headerTitle = "Monext Demo"
    )

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    @Before
    fun setup() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode. setVmPolicy(StrictMode.VmPolicy.LAX)
    }

    private fun createPhoneNumberField(value: String = "", label: String): PaymentMethodFormField {
        return PaymentMethodFormField(
            validation = PaymentMethodFieldValidation(
                pattern = PHONE_REGEX
            ),
            value = value,
            placeholder = PHONE_PLACEHOLDER,
            inputType = INPUT_TYPE,
            fieldIcon = FIELD_ICON,
            key = PHONE_FIELD_KEY,
            label = label,
            required = true,
            requiredErrorMessage = ERROR_MESSAGE,
            formInputFieldType = FORM_INPUT_FIELD_TYPE,
            formFieldType = FORM_FIELD_TYPE,
            secured = true
        )
    }

    private fun createPayPhoneForm(value: String = "", label: String = FIELD_LABEL): PaymentForm {
        return PaymentForm(
            displayButton = true,
            description = FORM_DESCRIPTION,
            buttonText = BUTTON_TEXT,
            formType = FORM_TYPE,
            formFields = listOf(createPhoneNumberField(value, label))
        )
    }

    private fun setupComposeTest(
        paymentMethod: PaymentMethod.AlternativePaymentMethod,
        onFormValidated: (FormData?) -> Unit
    ) {
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                AlternativePaymentMethodForm(paymentMethod) { formData ->
                    onFormValidated(formData)
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun assertFormDataValue(
        formData: FormData?,
        expectedValue: String?,
        fieldKey: String = PHONE_FIELD_KEY
    ) {
        assertNotNull(formData)
        val value = (formData as? FormData. AlternativePaymentMethodForm)?.securedParams?.get(fieldKey)
        assertEquals(expectedValue, value)
    }

    // Tests

    @Test
    fun saveCardCheckbox_isDisplayedAndCanBeClicked() {
        val paymentMethodData = createPaymentMethodData(
            cardCode = CARD_CODE_PAYPAL,
            hasForm = true,
            form = PaymentForm()
        )
        val paymentMethod = PaymentMethod. AlternativePaymentMethod(paymentMethodData)
        var validatedFormData: FormData? = null

        setupComposeTest(paymentMethod) { formData ->
            validatedFormData = formData
        }

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithTag(SAVE_CARD_CHECKBOX_TAG)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(SAVE_CARD_CHECKBOX_TAG)
            .performClick()

        composeTestRule.waitForIdle()

        assertNotNull(validatedFormData)
        assert(validatedFormData?.paymentParams()?.savePaymentData == true)
    }

    @Test
    fun formInputText_isDisplayedWithNoLabel() {
        val paymentMethodData = createPaymentMethodData(
            cardCode = CARD_CODE_MBWAY,
            hasForm = true,
            form = createPayPhoneForm("", "")
        )

        val paymentMethod = PaymentMethod.AlternativePaymentMethod(paymentMethodData)
        var validatedFormData: FormData? = null

        setupComposeTest(paymentMethod) { formData ->
            validatedFormData = formData
        }

        composeTestRule
            .onNodeWithTag(PHONE_FIELD_KEY)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(PHONE_PLACEHOLDER)
            .assertIsDisplayed()
    }

    @Test
    fun formInputText_isDisplayedAndPreFilled() {
        val paymentMethodData = createPaymentMethodData(
            cardCode = CARD_CODE_MBWAY,
            hasForm = true,
            form = createPayPhoneForm(value = VALID_PHONE)
        )

        val paymentMethod = PaymentMethod.AlternativePaymentMethod(paymentMethodData)
        var validatedFormData: FormData? = null

        setupComposeTest(paymentMethod) { formData ->
            validatedFormData = formData
        }

        composeTestRule
            .onNodeWithTag(PHONE_FIELD_KEY)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(FIELD_LABEL)
            .assertIsDisplayed()

        assertFormDataValue(validatedFormData, VALID_PHONE)
    }

    @Test
    fun formInputText_userCanTypeAndValidate() {
        val paymentMethodData = createPaymentMethodData(
            cardCode = CARD_CODE_MBWAY,
            hasForm = true,
            form = createPayPhoneForm(value = "")
        )

        val paymentMethod = PaymentMethod.AlternativePaymentMethod(paymentMethodData)
        var validatedFormData: FormData? = null

        setupComposeTest(paymentMethod) { formData ->
            validatedFormData = formData
        }

        val input = composeTestRule.onNodeWithTag(PHONE_FIELD_KEY)
        input.assertIsDisplayed()

        input.performTextInput(VALID_PHONE)

        composeTestRule.waitForIdle()

        assertFormDataValue(validatedFormData, VALID_PHONE)
    }

    @Test
    fun formInputText_regexPattern() {
        val paymentMethodData = createPaymentMethodData(
            cardCode = CARD_CODE_MBWAY,
            hasForm = true,
            form = createPayPhoneForm(value = "")
        )

        val paymentMethod = PaymentMethod.AlternativePaymentMethod(paymentMethodData)
        var validatedFormData: FormData?  = null

        setupComposeTest(paymentMethod) { formData ->
            validatedFormData = formData
        }

        val input = composeTestRule.onNodeWithTag(PHONE_FIELD_KEY)
        input. assertIsDisplayed()

        input.performTextInput(INVALID_PHONE)

        composeTestRule.waitForIdle()

        assertNull(validatedFormData)
    }
}