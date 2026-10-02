package com.monext.sdk.internal.security.ui

import android.os.StrictMode
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monext.sdk.FakeTestActivity
import io.mockk.MockKAnnotations
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecurityAlertDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
    }

    private fun setDialog(onDismiss: () -> Unit) {
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(
                LocalActivity provides composeTestRule.activity
            ) {
                SecurityAlertDialog(onDismiss = onDismiss)
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun securityDialog_whenShowing_displaysTitle() {
        setDialog(onDismiss = {})

        composeTestRule
            .onNodeWithText("Appareil non sécurisé")
            .assertIsDisplayed()
    }

    @Test
    fun securityDialog_whenShowing_displaysMessage() {
        setDialog(onDismiss = {})

        composeTestRule
            .onNodeWithText(
                "Votre appareil ne répond pas aux exigences de sécurité " +
                        "requises pour effectuer un paiement."
            )
            .assertIsDisplayed()
    }

    @Test
    fun securityDialog_whenShowing_displaysCloseButton() {
        setDialog(onDismiss = {})

        composeTestRule
            .onNodeWithText("Fermer")
            .assertIsDisplayed()
    }

    @Test
    fun securityDialog_whenCloseButtonClicked_callsOnDismiss() {
        var dismissed = false

        setDialog(onDismiss = { dismissed = true })

        composeTestRule
            .onNodeWithText("Fermer")
            .performClick()

        composeTestRule.waitUntil(5000) { dismissed }
        assertTrue(dismissed)
    }
}