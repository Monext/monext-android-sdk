package com.monext.sdk.presentation.status

import android.os.StrictMode
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.monext.sdk.Appearance
import com.monext.sdk.FakeTestActivity
import com.monext.sdk.LocalAppearance
import com.monext.sdk.internal.data.LocalSessionStateRepo
import com.monext.sdk.internal.data.SessionStateRepository
import com.monext.sdk.internal.data.sessionstate.ActiveWaiting
import com.monext.sdk.internal.data.sessionstate.CustomMessage
import com.monext.sdk.internal.presentation.status.ActiveWaitingScreen
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ActiveWaitingScreenTest {

    companion object {
        private const val LOADER_TEST_TAG = "active_waiting_loader"
        private const val MESSAGE_TEST_TAG = "active_waiting_message"
        private const val CARD_CODE = "PAYPAL"
        private const val CONTRACT_NUMBER = "CONTRACT_123"
        private const val WALLET_CARD_INDEX = 0
        private const val MERCHANT_RETURN_URL = "https://example. com/return"
        private const val MESSAGE_TEXT = "Votre paiement est en cours de traitement"
    }

    private val appearance = Appearance(
        headerTitle = "Payment Status"
    )

    private val mockSessionStore = mockk<SessionStateRepository>(relaxed = true)

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    @Before
    fun setup() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode. setVmPolicy(StrictMode.VmPolicy.LAX)
        coEvery { mockSessionStore.isDone() } returns Unit
    }

    // Helper

    private fun createCustomMessage(
        message: String,
        type: String = "INFO",
        displayIcon: Boolean = true
    ): CustomMessage {
        return CustomMessage(
            type = type,
            localizedMessage = message,
            displayIcon = displayIcon
        )
    }

    private fun createActiveWaiting(
        message: CustomMessage?  = null,
        needActiveWaitingAction: Boolean = true,
        cardCode: String = CARD_CODE,
        contractNumber: String = CONTRACT_NUMBER,
        walletCardIndex: Int = WALLET_CARD_INDEX,
        merchantReturnUrl: String = MERCHANT_RETURN_URL
    ): ActiveWaiting {
        return ActiveWaiting(
            needActiveWaitingAction = needActiveWaitingAction,
            message = message,
            cardCode = cardCode,
            contractNumber = contractNumber,
            walletCardIndex = walletCardIndex,
            merchantReturnUrl = merchantReturnUrl
        )
    }

    private fun setupComposeTest(activeWaiting: ActiveWaiting) {
        composeTestRule.activity. setTestComposable {
            CompositionLocalProvider(
                LocalAppearance provides appearance,
                LocalSessionStateRepo provides mockSessionStore
            ) {
                ActiveWaitingScreen(activeWaiting)
            }
        }
        composeTestRule.waitForIdle()
    }

    // Tests

    @Test
    fun loaderIsDisplayed() {
        val activeWaiting = createActiveWaiting()

        setupComposeTest(activeWaiting)

        composeTestRule
            .onNodeWithTag(LOADER_TEST_TAG)
            .assertIsDisplayed()
    }

    @Test
    fun messageIsNotDisplayedWhenNull() {
        val activeWaiting = createActiveWaiting(message = null)

        setupComposeTest(activeWaiting)

        composeTestRule
            .onNodeWithTag(MESSAGE_TEST_TAG, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun messageIsNotDisplayedWhenEmpty() {
        val customMessage = createCustomMessage("")
        val activeWaiting = createActiveWaiting(message = customMessage)

        setupComposeTest(activeWaiting)

        composeTestRule
            .onNodeWithTag(MESSAGE_TEST_TAG, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun messageIsNotDisplayedWhenBlank() {
        val customMessage = createCustomMessage("   ")
        val activeWaiting = createActiveWaiting(message = customMessage)

        setupComposeTest(activeWaiting)

        composeTestRule
            .onNodeWithTag(MESSAGE_TEST_TAG, useUnmergedTree = true)
            .assertDoesNotExist()
    }

    @Test
    fun isDoneIsCalledOnLaunchedEffect() {
        val activeWaiting = createActiveWaiting()

        setupComposeTest(activeWaiting)

        coVerify { mockSessionStore.isDone() }
    }
}