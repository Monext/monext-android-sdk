package com.monext.sdk.presentation

import android.os.StrictMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monext.sdk.Appearance
import com.monext.sdk.FakeTestActivity
import com.monext.sdk.LocalAppearance
import com.monext.sdk.PaymentOverlayToggle
import com.monext.sdk.PaymentResult
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.api.model.response.SessionStateType
import com.monext.sdk.internal.presentation.PaymentContainer
import com.monext.sdk.internal.preview.PreviewSamples.Companion.buildSessionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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
        headerTitle = "Monext Demo"
    )

    @Test
    fun withSuccessTicket() {
        val sessionState : SessionState = buildSessionState(false, SessionStateType.PAYMENT_SUCCESS, )
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_SUCCESS, "success_title")
    }

    @Test
    fun withSuccessTicketAndRedirect() {
        val sessionState : SessionState = buildSessionState(true, SessionStateType.PAYMENT_SUCCESS)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_SUCCESS, "success_title")
    }

    @Test
    fun withFailureDisplay() {
        val sessionState : SessionState = buildSessionState(false, SessionStateType.PAYMENT_FAILURE)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_FAILURE, "failure_header")
    }

    @Test
    fun withFailureDisplayAndRedirect() {
        val sessionState : SessionState = buildSessionState(true, SessionStateType.PAYMENT_FAILURE)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_FAILURE, "failure_header")
    }

    @Test
    fun withExpiredSessionDisplay() {
        val sessionState : SessionState = buildSessionState(false, SessionStateType.TOKEN_EXPIRED)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.TOKEN_EXPIRED, "expîred_header")
    }

    @Test
    fun withExpiredSessionAndRedirect() {
        val sessionState : SessionState = buildSessionState(true, SessionStateType.TOKEN_EXPIRED)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.TOKEN_EXPIRED, "expîred_header")
    }

    @Test
    fun withCancelSessionDisplay() {
        val sessionState : SessionState = buildSessionState(false, SessionStateType.PAYMENT_CANCELED)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_CANCELED, "cancel_header")
    }

    @Test
    fun withCancelSessionAndRedirect() {
        val sessionState : SessionState = buildSessionState(true, SessionStateType.PAYMENT_CANCELED)
        executeSessionStateTest(sessionState, PaymentResult.TransactionState.PAYMENT_CANCELED, "cancel_header")
    }

    private fun executeSessionStateTest(sessionState: SessionState, expectedTransactionState: PaymentResult.TransactionState, expectedTag : String) {
        var paymentResult: PaymentResult? = null
        var showingChange = true
        composeTestRule.activity.setTestComposable {

            CompositionLocalProvider(LocalAppearance provides appearance) {
                PaymentContainer(
                    sessionState,
                    { paymentMethodList, sessionInfo -> },
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
            composeTestRule.onNodeWithTag(expectedTag).assertDoesNotExist()
            assertFalse(showingChange)
        } else {
            composeTestRule.onNodeWithTag(expectedTag).assertIsDisplayed()
            assertTrue(showingChange)
        }
    }
}