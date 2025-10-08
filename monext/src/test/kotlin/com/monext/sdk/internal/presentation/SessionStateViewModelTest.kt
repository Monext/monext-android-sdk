package com.monext.sdk.internal.presentation

import android.app.Application
import android.content.Context
import com.monext.sdk.SdkTestHelper
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.api.model.request.SecuredPaymentRequest
import com.monext.sdk.internal.api.model.request.WalletPaymentRequest
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.data.SessionStateRepository
import com.monext.sdk.internal.preview.PreviewSamples
import com.monext.sdk.internal.threeds.ThreeDSManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class SessionStateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @RelaxedMockK
    internal lateinit var sessionStateRepositoryMock: SessionStateRepository
    @RelaxedMockK
    internal lateinit var appMock: Application

    @RelaxedMockK
    private lateinit var contextMock: Context
    @RelaxedMockK
    internal lateinit var threeDSManagerMock: ThreeDSManager

    internal val captureSecuredPaymentRequest = slot<SecuredPaymentRequest>()
    internal val captureWalletPaymentRequest = slot<WalletPaymentRequest>()

    internal var internalSDKContext: InternalSDKContext = SdkTestHelper.createInternalSDKContext()

    internal lateinit var underTest: SessionStateViewModel
    internal val sessionStateFlow = MutableStateFlow<SessionState?>(null)

    @BeforeEach
    fun setUp() {
        every { sessionStateRepositoryMock.sessionState } returns sessionStateFlow
        every { sessionStateRepositoryMock.threeDSManager } returns threeDSManagerMock
        every { sessionStateRepositoryMock.internalSDKContext } returns internalSDKContext

        underTest = spyk(SessionStateViewModel(sessionStateRepositoryMock, appMock, dispatcher = testDispatcher))
    }

    @Test
    fun makeCardPaymentShouldMakeCardPaymentWithThreeDsContextData() = runTest(testDispatcher) {
        // Data
        val paymentAttemptCB = SdkTestHelper.createPaymentAttemptCB()
        var isFinish = false;
        val cardType = "CB"
        val sdkContextData = SdkTestHelper.createSdkContextData()
        val paymentMethodList = PreviewSamples.sessionStatePaymentMethodsList
        sessionStateFlow.value = paymentMethodList

        // Mock
        every { threeDSManagerMock.isInitialized } returns false
        every { threeDSManagerMock.generateSDKContextData(cardType) } returns sdkContextData


        launch {
            // Test
            underTest.makeCardPayment(paymentAttemptCB.paymentFormData, context = contextMock) {
                isFinish = true;
            }
        }

        // On attends les coroutines
        advanceUntilIdle()


        // Verif
        assertTrue { isFinish }

        coVerify { threeDSManagerMock.startInitialize("fake_token", cardType) }
        coVerify { threeDSManagerMock.generateSDKContextData(cardType) }
        coVerify { sessionStateRepositoryMock.makeSecuredPayment(params = capture(captureSecuredPaymentRequest)) }

        assertEquals("CB", captureSecuredPaymentRequest.captured.cardCode)
        assertEquals("CB_01", captureSecuredPaymentRequest.captured.contractNumber)
        assertNotNull(captureSecuredPaymentRequest.captured.deviceInfo)
        assertTrue(captureSecuredPaymentRequest.captured.isEmbeddedRedirectionAllowed)
        // params
        assertEquals("2", captureSecuredPaymentRequest.captured.paymentParams.network)
        assertEquals("Jean michel", captureSecuredPaymentRequest.captured.paymentParams.holderName)
        assertEquals("1230", captureSecuredPaymentRequest.captured.paymentParams.expirationDate)
        assertTrue(captureSecuredPaymentRequest.captured.paymentParams.savePaymentData!!)
        assertEquals("{\"deviceRenderingOptionsIF\":\"01\",\"deviceRenderOptionsUI\":\"03\",\"maxTimeout\":60,\"referenceNumber\":\"refNumber_xx\",\"ephemPubKey\":\"ephemPubKey_yyy\",\"appID\":\"sdkAppID_pp\",\"transID\":\"sdkTransactionID_kk\",\"encData\":\"deviceData_qq\"}",
            captureSecuredPaymentRequest.captured.paymentParams.sdkContextData)
        // SecuredParams
        assertEquals("4970100000000000", captureSecuredPaymentRequest.captured.securedPaymentParams.pan)
        assertEquals("123", captureSecuredPaymentRequest.captured.securedPaymentParams.cvv)
    }

    @Test
    fun makeWalletPaymentShouldMakeCardPaymentWithThreeDsContextData() = runTest(testDispatcher) {
        // Data
        val wallet = SdkTestHelper.createWallet()
        val walletFormData = SdkTestHelper.createWalletFormData()
        var isFinish = false;
        val cardType = "CB"
        val sdkContextData = SdkTestHelper.createSdkContextData()
        val paymentMethodList = PreviewSamples.sessionStatePaymentMethodsList
        sessionStateFlow.value = paymentMethodList

        // Mock
        every { threeDSManagerMock.isInitialized } returns true
        every { threeDSManagerMock.generateSDKContextData(cardType) } returns sdkContextData

        // Test
        launch {
            // Test
            underTest.makeWalletPayment(selectedWallet = wallet,walletFormData = walletFormData) {
                isFinish = true;
            }
        }

        // On attends les coroutines
        advanceUntilIdle()

        // Verif
        assertTrue { isFinish }

        coVerify { threeDSManagerMock.closeTransaction() }
        coVerify { threeDSManagerMock.startInitialize("fake_token", cardType) }
        coVerify { threeDSManagerMock.generateSDKContextData(cardType) }
        coVerify { sessionStateRepositoryMock.makeWalletPayment(params = capture(captureWalletPaymentRequest)) }

        assertEquals("CB", captureWalletPaymentRequest.captured.cardCode.name)
        assertEquals(2, captureWalletPaymentRequest.captured.index)
        assertTrue(captureWalletPaymentRequest.captured.isEmbeddedRedirectionAllowed)
        assertEquals("", captureWalletPaymentRequest.captured.merchantReturnUrl)
        kotlin.test.assertNotNull(captureWalletPaymentRequest.captured.securedPaymentParams)
        // params
        assertEquals("{\"deviceRenderingOptionsIF\":\"01\",\"deviceRenderOptionsUI\":\"03\",\"maxTimeout\":60,\"referenceNumber\":\"refNumber_xx\",\"ephemPubKey\":\"ephemPubKey_yyy\",\"appID\":\"sdkAppID_pp\",\"transID\":\"sdkTransactionID_kk\",\"encData\":\"deviceData_qq\"}",
            captureWalletPaymentRequest.captured.paymentParams.sdkContextData)
        // SecuredParams
        assertEquals("123", captureWalletPaymentRequest.captured.securedPaymentParams.cvv)
    }


    @Test
    fun makePaymentShouldCallMakeCardPayment() = runTest(testDispatcher) {
        // Data
        val paymentAttemptCB = SdkTestHelper.createPaymentAttemptCB()
        val paymentMethodList = PreviewSamples.sessionStatePaymentMethodsList
        sessionStateFlow.value = paymentMethodList

        // Mock
        coEvery { underTest.makeCardPayment(any(), any(), any()) } returns Unit

        // Test
        underTest.makePayment(paymentAttempt = paymentAttemptCB, context = contextMock) {}

        // Verif
        coVerify { underTest.makeCardPayment(paymentAttemptCB.paymentFormData, contextMock, any()) }
        assertFalse(underTest.sessionLoading.value)
    }

    @Test
    fun makePaymentShouldCallMakeWalletPayment() = runTest(testDispatcher) {
        // Data
        val paymentAttemptWalletCB = SdkTestHelper.createPaymentAttemptWalletCB()
        val paymentMethodList = PreviewSamples.sessionStatePaymentMethodsList
        sessionStateFlow.value = paymentMethodList

        // Mock
        coEvery { underTest.makeWalletPayment(any(), any(), any()) } returns Unit

        // Test
        underTest.makePayment(paymentAttempt = paymentAttemptWalletCB, context = contextMock) {}

        // Verif
        coVerify { underTest.makeWalletPayment(paymentAttemptWalletCB.selectedWallet, paymentAttemptWalletCB.walletFormData, any()) }
        assertFalse(underTest.sessionLoading.value)
    }

}