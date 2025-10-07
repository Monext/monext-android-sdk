package com.monext.sdk

import android.os.StrictMode
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PaymentSheetTest {

    @Before
    fun setup() {
        // Désactiver StrictMode pour les tests
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
    }

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    @Test
    fun paymentSheet_whenShowing_displaysBottomSheet() {
        // Arrange
        val testToken = "test-session-token"
        val testContext = MnxtSDKContext(MnxtEnvironment.Sandbox)
        var resultReceived: PaymentResult? = null

        // Act
        composeTestRule.activity.setTestComposable {
            PaymentSheet(
                isShowing = true,
                sessionToken = testToken,
                sdkContext = testContext,
                onResult = { result ->
                    resultReceived = result
                }
            )
        }

        // Assert - Vérifie que le bottom sheet est visible
        composeTestRule
            .onNodeWithTag("payment_bottom_sheet")
            .assertIsDisplayed()
    }

    @Test
    fun paymentSheet_whenNotShowing_displaysNothing() {
        // Arrange
        val testContext = MnxtSDKContext(MnxtEnvironment.Sandbox)

        // Act
        composeTestRule.activity.setTestComposable {
            PaymentSheet(
                isShowing = false,
                sessionToken = "test-token",
                sdkContext = testContext,
                onResult = {}
            )
        }

        // Assert - Vérifie que rien n'est affiché
        composeTestRule
            .onNodeWithTag("payment_bottom_sheet")
            .assertDoesNotExist()
    }

    @Test
    fun paymentSheet_whenDismissed_callsOnResult() {
        // Arrange
        var dismissResult: PaymentResult? = null
        val testContext = MnxtSDKContext(MnxtEnvironment.Sandbox)

        composeTestRule.activity.setTestComposable {
            PaymentSheet(
                isShowing = true,
                sessionToken = "test-token",
                sdkContext = testContext,
                onResult = { result ->
                    dismissResult = result
                }
            )
        }

        // Act - Simule un swipe down pour fermer
        composeTestRule
            .onNodeWithTag("payment_bottom_sheet")
            .performTouchInput {
                swipeDown()
            }

        // Assert
        composeTestRule.waitUntil(5000) {
            dismissResult is PaymentResult.SheetDismissed
        }
    }
}