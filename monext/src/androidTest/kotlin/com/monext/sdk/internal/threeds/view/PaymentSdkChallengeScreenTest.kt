package com.monext.sdk.internal.threeds.view

import android.os.StrictMode
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.monext.sdk.Appearance
import com.monext.sdk.FakeTestActivity
import com.monext.sdk.LocalAppearance
import com.monext.sdk.LocalEnvironment
import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.PaymentOverlayToggle
import com.monext.sdk.SdkTestHelper
import com.monext.sdk.internal.data.LocalSessionStateRepo
import com.monext.sdk.internal.data.SessionStateRepository
import com.monext.sdk.internal.threeds.model.AuthenticationResponse
import com.monext.sdk.internal.threeds.model.ChallengeUseCaseCallback
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentSdkChallengeScreenTest {

    private val stateHistory = mutableListOf<PaymentOverlayToggle>()

    @RelaxedMockK
    private lateinit var sessionStateRepositoryMock : SessionStateRepository

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        // Désactiver StrictMode pour les tests
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
        StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
    }

    @Test
    fun paymentSdkChallengeScreen_shouldStartChallengeProcess() {

        // Arrange
        val sdkChallengeData = SdkTestHelper.createSdkChallengeData()
        val testAppearance = Appearance(headerTitle = "Monext SDK Preview")
        val testEnvironment = MnxtEnvironment.Sandbox
        val authenticationResponseMock = mockk<AuthenticationResponse>(relaxed = true)
        val callbackSlot = slot<ChallengeUseCaseCallback>()

        // Mock
        coEvery { sessionStateRepositoryMock.makeThreeDsChallengeFlow(any(), any(),any(),capture(callbackSlot)) } answers {
            // On mock la reponse pour invoquer la callback
            callbackSlot.captured.onChallengeCompletion(authenticationResponseMock)
        }

        // Act
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(
                LocalAppearance provides testAppearance,
                LocalEnvironment provides testEnvironment,
                LocalSessionStateRepo provides sessionStateRepositoryMock,
                LocalActivity provides composeTestRule.activity
            ) {
                PaymentSdkChallengeScreen(sdkChallengeData = sdkChallengeData) { state -> stateHistory.add(state) }
            }
        }

        // Attendre que tous les effets soient exécutés
        composeTestRule.waitForIdle()

        // Assert
        coVerify { sessionStateRepositoryMock.makeThreeDsChallengeFlow(
            activity = composeTestRule.activity,
            sdkChallengeData = sdkChallengeData,
            theme = testAppearance,
            useCaseCallback = any())
        }
        coVerify { sessionStateRepositoryMock.makeSdkPayment(authenticationResponseMock) }

        assertEquals(PaymentOverlayToggle.on(), stateHistory[0])
        assertEquals(PaymentOverlayToggle.off(), stateHistory[1])
    }

}