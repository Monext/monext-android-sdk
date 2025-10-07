package com.monext.sdk.internal.data

import android.app.Activity
import com.monext.sdk.Appearance
import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.MnxtSDKContext
import com.monext.sdk.SdkTestHelper
import com.monext.sdk.internal.api.PaymentAPI
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.preview.PreviewSamples
import com.monext.sdk.internal.threeds.ThreeDSManager
import com.monext.sdk.internal.threeds.model.ChallengeUseCaseCallback
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SessionStateRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()

    @RelaxedMockK
    private lateinit var threeDSManager: ThreeDSManager
    @RelaxedMockK
    private lateinit var paymentAPI: PaymentAPI

    private lateinit var underTest: SessionStateRepository

    var internalSDKContext: InternalSDKContext = InternalSDKContext(MnxtSDKContext(environment = MnxtEnvironment.Sandbox))
    internal val sessionStateMethodList = PreviewSamples.sessionStatePaymentMethodsList

    val token: String = "fake_token"

    @BeforeEach
    fun setUp() {
        underTest = spyk(SessionStateRepository(paymentAPI, internalSDKContext, threeDSManager))

        // Mock
        coEvery { paymentAPI.stateCurrent(any()) } returns sessionStateMethodList
    }

    @Test
    fun updateSessionState() = runTest(testDispatcher) {

        underTest.updateSessionState(token)

        coVerify(exactly = 1) { paymentAPI.stateCurrent(token)  }
        assertEquals(sessionStateMethodList, underTest.sessionState.value)
    }

    @Test
    fun initializeSessionStateWithToken() = runTest(testDispatcher) {

        underTest.initializeSessionState(token)

        coVerify(exactly = 1) { underTest.updateSessionState(token) }
    }

    @Test
    fun makeSdkPayment() = runTest(testDispatcher) {
        val request = SdkTestHelper.createAuthenticationResponse("Y")

        underTest.initializeSessionState(token)
        underTest.makeSdkPayment(request)

        coVerify(exactly = 1) { paymentAPI.sdkPaymentRequest(token, request)  }
    }

    @Test
    fun clearSession() = runTest(testDispatcher) {
        // Init data
        underTest.updateSessionState(token)
        coVerify(exactly = 1) { paymentAPI.stateCurrent(token)  }
        assertEquals(sessionStateMethodList, underTest.sessionState.value)

        // Test
        underTest.clearSession()

        assertNull(underTest.sessionState.value)
    }

    @Test
    fun makeWalletPayment() = runTest(testDispatcher) {
        val request = SdkTestHelper.createWalletPaymentRequest()

        underTest.initializeSessionState(token)
        underTest.makeWalletPayment(request)

        coVerify(exactly = 1) { paymentAPI.walletPayment(token, request)  }
    }

    @Test
    fun updateContext() {
        val internalSDKContextUpdated =
            InternalSDKContext(MnxtSDKContext(environment = MnxtEnvironment.Production))

        underTest.updateContext(internalSDKContextUpdated)

        assertEquals(internalSDKContextUpdated, underTest.internalSDKContext)
        verify { paymentAPI.updateContext(internalSDKContextUpdated) }
    }

    @Test
    fun makeThreeDsChallengeFlow() = runTest {
        val sdkChallengeData = SdkTestHelper.createSdkChallengeData()
        val authenticationResponse = SdkTestHelper.createAuthenticationResponse("X")
        val activity = mockk<Activity>()
        val theme = mockk<Appearance>()
        val useCaseCallbackFromParent = mockk<ChallengeUseCaseCallback>(relaxed = true)
        // Capturer le callback
        val callbackSlot = slot<ChallengeUseCaseCallback>()

        // Mock
        coEvery { threeDSManager.doChallengeFlow(activity, sdkChallengeData = sdkChallengeData, theme = theme, useCaseCallback = capture(callbackSlot))} answers {
            // Simuler l'appel du callback
            callbackSlot.captured.onChallengeCompletion(authenticationResponse)
        }

        // Test
        underTest.makeThreeDsChallengeFlow(activity, sdkChallengeData, theme, useCaseCallbackFromParent)

        // Verif
        coVerify { threeDSManager.doChallengeFlow(activity, sdkChallengeData, theme, any()) }
        verify { threeDSManager.closeTransaction() }
        verify { useCaseCallbackFromParent.onChallengeCompletion(authenticationResponse) }
    }


    @Test
    fun makeSecuredPayment() = runTest(testDispatcher) {
        val request = SdkTestHelper.createSecuredPaymentRequestCB()

        underTest.initializeSessionState(token)
        underTest.makeSecuredPayment(request)

        coVerify(exactly = 1) { paymentAPI.securedPayment(token, request)  }
    }

    @Test
    fun makePayment() = runTest(testDispatcher) {
        val request = SdkTestHelper.createPaymentRequestCB()

        underTest.initializeSessionState(token)
        underTest.makePayment(request)

        coVerify(exactly = 1) { paymentAPI.payment(token, request)  }
    }

    @Test
    fun makeGooglePayPayment() = runTest(testDispatcher) {
        val request = SdkTestHelper.createPaymentRequestGooglePay()

        underTest.initializeSessionState(token)
        underTest.makeGooglePayPayment(request)

        coVerify(exactly = 1) { paymentAPI.payment(token, request)  }
    }

    @Test
    fun getSessionState() = runTest(testDispatcher) {
        // Il est null au départ
        assertNull(underTest.sessionState.value)

        // On met à jour
        underTest.updateSessionState(token)

        coVerify(exactly = 1) { paymentAPI.stateCurrent(token)  }
        // Il ne doit plus etre null
        assertEquals(sessionStateMethodList, underTest.sessionState.value)
    }

    @Test
    fun availableCardNetworks() = runTest(testDispatcher) {
        val request = SdkTestHelper.createAvailableCardNetworksRequest()

        underTest.initializeSessionState(token)
        underTest.availableCardNetworks(request)

        coVerify(exactly = 1) { paymentAPI.availableCardNetworks(token, request)  }
    }

    @Test
    fun getReturnURLString() {
        assertEquals("https:homologation-payment.payline.com", underTest.returnURLString)
    }

    @Test
    fun getInternalSDKContext() {
        assertEquals(internalSDKContext, underTest.internalSDKContext)
    }

    @Test
    fun setInternalSDKContext() {
        val newContext = mockk<InternalSDKContext>()
        assertEquals(internalSDKContext, underTest.internalSDKContext)

        // Test
        underTest.internalSDKContext = newContext
        assertEquals(newContext, underTest.internalSDKContext)
    }

    @Test
    fun getThreeDSManager() {
        val currentThreeDsManager = underTest.threeDSManager
        assertEquals(threeDSManager, currentThreeDsManager)
    }

    @Test
    fun setThreeDSManager() {
        val newThreeDSManager = mockk<ThreeDSManager>()
        assertEquals(threeDSManager, underTest.threeDSManager)

        // Test
        underTest.threeDSManager = newThreeDSManager
        assertEquals(newThreeDSManager, underTest.threeDSManager)
    }

}