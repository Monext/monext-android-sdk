package com.monext.sdk.presentation

import android.os.StrictMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monext.sdk.*
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.api.model.response.SessionStateType
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.sessionstate.*
import com.monext.sdk.internal.presentation.PaymentAttempt
import com.monext.sdk.internal.presentation.PaymentContainer
import com.monext.sdk.internal.preview.PreviewSamples.Companion.buildSessionState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class PaymentContainerTest {

    private val stateHistory = mutableListOf<PaymentOverlayToggle>()

    @Before
    fun setup() {
        // Désactiver StrictMode pour les tests
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
        stateHistory.clear()
    }
    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    val appearance = Appearance(
        headerTitle = "Monext Demo",
        backButtonText = "Back my friend"
    )

    @Test
    fun withSuccessTicket() {
        val sessionState : SessionState = buildSessionState(false, SessionStateType.PAYMENT_SUCCESS )
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_SUCCESS, mapOf("success_title" to "Congratulations",
                                                                                                                                                    "back_button" to appearance.backButtonText))
    }

    @Test
    fun withSuccessTicketAndRedirect() {
        val sessionState : SessionState = buildSessionState(true, SessionStateType.PAYMENT_SUCCESS)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_SUCCESS, mapOf("success_title" to null,
                                                                                                                                                    "back_button" to null))
    }

    @Test
    fun withPendingTicket() {
        val sessionState : SessionState = buildSessionState(false, SessionStateType.PAYMENT_ONHOLD_PARTNER)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_PENDING, mapOf("pending_header" to "Payment pending",
            "pending_description" to "Your payment is pending. Please contact your merchant for further information.",
            "back_button" to appearance.backButtonText))
    }

    @Test
    fun withPendingTicketAndRedirect() {
        val sessionState : SessionState = buildSessionState(true, SessionStateType.PAYMENT_ONHOLD_PARTNER)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_PENDING, mapOf("pending_header" to null,
            "pending_description" to null,
            "back_button" to null))
    }

    @Test
    fun withPendingTicketWithCustomMessage() {
        val paymentOnholdPartnerToUse = PaymentOnholdPartner(
            message = CustomMessage(
                type = "INFO",
                localizedMessage = "blabla",
                displayIcon = false
            ),
            selectedCardCode = "TEST",
            selectedContractNumber = "TEST"
        )

        val sessionState : SessionState = buildSessionState(false, SessionStateType.PAYMENT_ONHOLD_PARTNER, paymentOnholdPartner = paymentOnholdPartnerToUse)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_PENDING, mapOf("pending_header" to "Payment pending",
            "back_button" to appearance.backButtonText))
    }

    @Test
    fun withPendingTicketWithCustomMessageAndRedirect() {
        val paymentOnholdPartnerToUse = PaymentOnholdPartner(
            message = CustomMessage(
                type = "INFO",
                localizedMessage = "blabla",
                displayIcon = false
            ),
            selectedCardCode = "TEST",
            selectedContractNumber = "TEST"
        )

        val sessionState : SessionState = buildSessionState(true, SessionStateType.PAYMENT_ONHOLD_PARTNER, paymentOnholdPartner = paymentOnholdPartnerToUse)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_PENDING, mapOf("pending_header" to null,
            "back_button" to null))
    }

    @Test
    fun withFailureDisplay() {
        val sessionState : SessionState = buildSessionState(false, SessionStateType.PAYMENT_FAILURE)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_FAILURE, mapOf("failure_header" to "We are sorry",
            "back_button" to appearance.backButtonText))
    }

    @Test
    fun withFailureDisplayAndRedirect() {
        val sessionState : SessionState = buildSessionState(true, SessionStateType.PAYMENT_FAILURE)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_FAILURE, mapOf("failure_header" to null,
            "back_button" to null))
    }

    @Test
    fun withExpiredSessionDisplay() {
        val sessionState : SessionState = buildSessionState(false, SessionStateType.TOKEN_EXPIRED)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.TOKEN_EXPIRED, mapOf("expîred_header" to "Your payment session has expired.",
            "back_button" to appearance.backButtonText))
    }

    @Test
    fun withExpiredSessionAndRedirect() {
        val sessionState : SessionState = buildSessionState(true, SessionStateType.TOKEN_EXPIRED)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.TOKEN_EXPIRED, mapOf("expîred_header" to null,
            "back_button" to null))
    }

    @Test
    fun withCancelSessionDisplay() {
        val sessionState : SessionState = buildSessionState(false, SessionStateType.PAYMENT_CANCELED)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_CANCELED, mapOf("cancel_header" to "Your payment has been canceled.",
            "back_button" to appearance.backButtonText))
    }

    @Test
    fun withCancelSessionAndRedirect() {
        val sessionState : SessionState = buildSessionState(true, SessionStateType.PAYMENT_CANCELED)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_CANCELED, mapOf("cancel_header" to null,
            "back_button" to null))
    }

    @Test
    fun withPaymentRedirectWithJavascript_triggersMakePaymentForLoadedMethod() {
        // Given une réponse PAYMENT_REDIRECT_WITH_JAVASCRIPT contenant un moyen de paiement
        // avec un script à exécuter (ex : empreinte device PayPal).
        val paymentMethodData = PaymentMethodData(
            cardCode = "PAYPAL_APIREST",
            contractNumber = "PAYPAL_APIREST",
            disabled = false,
            hasForm = true,
            form = PaymentForm(
                displayButton = true,
                formScript = FormScript(
                    content = "console.log('fingerprint');",
                    wrapIntoScriptTag = true,
                    formScriptEnum = "CUSTOM"
                ),
                formType = "CUSTOM"
            ),
            hasLogo = false,
            logo = null,
            isIsolated = false,
            options = emptyList(),
            paymentMethodAction = 0,
            additionalData = null,
            requestContext = null,
            shouldBeInTopPosition = false,
            state = "AVAILABLE"
        )
        val sessionState = SessionState(
            token = "fake_token",
            type = SessionStateType.PAYMENT_REDIRECT_WITH_JAVASCRIPT,
            creationDate = "Tue Mar 25 12:33:22 CET 2025",
            cancelUrl = "https://www.payline.com",
            pointOfSale = "POS_Fake",
            language = "fr",
            returnUrl = "https://www.monext.fr",
            automaticRedirectAtSessionsEnd = false,
            isSandbox = true,
            paymentMethodsList = PaymentMethodsList(
                isOriginalCreditTransfer = false,
                needsDeviceFingerprint = true,
                paymentMethodsData = listOf(paymentMethodData),
                scoringNeeded = null,
                sensitiveInputContentMasked = false,
                shouldChangePaymentMethodPosition = false,
                wallets = emptyList()
            )
        )

        var capturedAttempt: PaymentAttempt? = null

        // When
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentContainer(
                    sessionState,
                    { paymentMethodList, sessionInfo -> },
                    {},
                    { attempt -> capturedAttempt = attempt },
                    {},
                    { },
                    { }
                ) { state -> stateHistory.add(state) }
            }
        }

        // Then : le paiement est déclenché automatiquement pour le moyen de paiement chargé
        composeTestRule.waitUntil(timeoutMillis = 10_000) { capturedAttempt != null }
        assertEquals("PAYPAL_APIREST", capturedAttempt?.selectedPaymentMethod?.cardCode)
        assertTrue(capturedAttempt?.paymentFormData is FormData.AlternativePaymentMethodForm)
    }

    private fun executeSessionStateTest(
        sessionState: SessionState,
        expectedTransactionState: PaymentResult.TransactionState,
        expectedTagsAndValue: Map<String,String?>
    ) {
        var paymentResult: PaymentResult? = null
        var showingChange = true
        composeTestRule.activity.setTestComposable {

            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentContainer(
                    sessionState,
                    { paymentMethodList, sessionInfo -> },
                    {},
                    {},
                    {},
                    { result ->
                        paymentResult = result
                    },
                    {
                            result -> showingChange = result
                    }
                ) { state -> stateHistory.add(state) }
            }
        }

        composeTestRule.waitUntil {
            paymentResult is PaymentResult.PaymentCompleted
        }
        val paymentCompleted: PaymentResult.PaymentCompleted = paymentResult as PaymentResult.PaymentCompleted
        assertEquals(expectedTransactionState, paymentCompleted.finalState)

        if (sessionState.automaticRedirectAtSessionsEnd == true) {
            for (tag in expectedTagsAndValue) {
                composeTestRule.onNodeWithTag(tag.key).assertDoesNotExist()
            }
            assertFalse(showingChange)
        } else {
            for (tag in expectedTagsAndValue) {
                composeTestRule.onNodeWithTag(tag.key, useUnmergedTree = true).assertExists()
                composeTestRule.onNodeWithTag(tag.key, useUnmergedTree = true).assertIsDisplayed()
                tag.value?.let {
                    composeTestRule.onNodeWithTag(tag.key, useUnmergedTree = true).assertTextEquals(tag.value!!)
                }
            }
            assertTrue(showingChange)
        }
    }
}