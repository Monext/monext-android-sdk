package com.monext.sdk.internal.presentation

import android.app.Application
import android.content.Context
import com.monext.sdk.SdkTestHelper
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.api.model.request.SecuredPaymentRequest
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.data.SessionStateRepository
import com.monext.sdk.internal.preview.PreviewSamples
import com.monext.sdk.internal.threeds.ThreeDSManager
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class SessionStateViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @RelaxedMockK
    internal lateinit var sessionStateRepository: SessionStateRepository
    @RelaxedMockK
    internal lateinit var app: Application

    @RelaxedMockK
    private lateinit var context: Context
    @RelaxedMockK
    internal lateinit var threeDSManager: ThreeDSManager

    internal val captureSecuredPaymentRequest = slot<SecuredPaymentRequest>()

    internal var internalSDKContext: InternalSDKContext = SdkTestHelper.createInternalSDKContext()

    internal lateinit var underTest: SessionStateViewModel

    @BeforeEach
    fun setUp() {
        underTest = SessionStateViewModel(sessionStateRepository, app, dispatcher = testDispatcher)

        every { underTest.sessionStateRepository.threeDSManager } returns threeDSManager
        every { underTest.sessionStateRepository.internalSDKContext } returns internalSDKContext
    }

    @Test
    fun makePaymentShouldMakeCardPayment() = runTest(testDispatcher) {
        // Data
        val paymentAttemptCB = SdkTestHelper.createPaymentAttemptCB()
        var isFinish = false;
        val paymentMethodList = PreviewSamples.sessionStatePaymentMethodsList
        val cardType = "CB"
        val sdkContextData = SdkTestHelper.createSdkContextData()

        // Mock
        every { underTest.sessionStateRepository.sessionState } returns MutableStateFlow<SessionState?>(paymentMethodList)
        every { threeDSManager.isInitialized } returns false
        every { threeDSManager.generateSDKContextData(cardType) } returns sdkContextData

        // Test
        underTest.makeCardPayment(paymentAttemptCB.paymentFormData, context = context) {
            isFinish = true;
        }

        // Verif
        assertTrue { isFinish }

        coVerify { threeDSManager.startInitialize("fake_token", cardType) }
        coVerify { threeDSManager.generateSDKContextData(cardType) }
        coVerify { sessionStateRepository.makeSecuredPayment(params = capture(captureSecuredPaymentRequest)) }

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

}