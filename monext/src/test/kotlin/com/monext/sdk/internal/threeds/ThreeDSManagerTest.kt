package com.monext.sdk.internal.threeds

import ThreeDSConfiguration
import android.app.Activity
import android.content.Context
import com.monext.sdk.Appearance
import com.monext.sdk.SdkTestHelper
import com.monext.sdk.internal.api.PaymentAPI
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.exception.ThreeDsException
import com.monext.sdk.internal.exception.ThreeDsExceptionType
import com.monext.sdk.internal.threeds.model.ChallengeUseCaseCallback
import com.netcetera.threeds.sdk.api.ThreeDS2Service
import com.netcetera.threeds.sdk.api.configparameters.ConfigParameters
import com.netcetera.threeds.sdk.api.configparameters.builder.ConfigurationBuilder
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration
import com.netcetera.threeds.sdk.api.info.SDKInfo
import com.netcetera.threeds.sdk.api.info.SchemeInfo
import com.netcetera.threeds.sdk.api.transaction.AuthenticationRequestParameters
import com.netcetera.threeds.sdk.api.transaction.Transaction
import com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeParameters
import com.netcetera.threeds.sdk.api.ui.ProgressView
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
class ThreeDSManagerTest {
    private val testDispatcher = StandardTestDispatcher()
    var internalSDKContext: InternalSDKContext = SdkTestHelper.createInternalSDKContext()

    // allow to capture parameter with non nullable type `Double`
    val captureChallengeParameters = slot<ChallengeParameters>()

    @RelaxedMockK
    private lateinit var paymentApi: PaymentAPI
    @RelaxedMockK
    private lateinit var context: Context
    @RelaxedMockK
    private lateinit var threeDS2ServiceMock: ThreeDS2Service
    @RelaxedMockK
    private lateinit var schemeConfigurationMock: SchemeConfiguration
    @RelaxedMockK
    private lateinit var configurationBuilderMock: ConfigurationBuilder
    @RelaxedMockK
    private lateinit var configParametersMock: ConfigParameters

    @SpyK
    private var threeDSBusiness: ThreeDSBusiness = ThreeDSBusiness()

    private lateinit var underTest: ThreeDSManager

    @BeforeEach
    fun setUp() {
        underTest = ThreeDSManager(paymentApi, internalSDKContext, context, threeDS2ServiceMock, threeDSBusiness)
    }


    @Test
    fun generateSDKContextDataShouldThrowExceptionCauseNotInitialized() {
        underTest.isInitialized = false;

        // Test
        val exception = assertThrows<ThreeDsException> { underTest.generateSDKContextData(cardType = "CB") }

        // Verif
        assertEquals(ThreeDsExceptionType.NOT_INITIALISED, exception.type)
        assertEquals("Unable to generate 3DS Context", exception.message)
    }

    @Test
    fun generateSDKContextData() {
        underTest.isInitialized = true;

        val transactionMock = mockk<Transaction>(relaxed = true)
        val schemeInfoMock = mockk<SchemeInfo>(relaxed = true)
        val sDKInfoMock = mockk<SDKInfo>(relaxed = true)
        val authenticationRequestParametersMock = mockk<AuthenticationRequestParameters>(relaxed = true)
        val expectedSdkReferenceNumber = "sdkReferenceNumber_ooo"
        val expectedSdkAppID = "sdkAppID_iii"
        val expectedSdkTransactionID = "sdkTransactionID_qqq"
        val expectedDeviceData = "deviceData_ppp"
        underTest.threeDS2Service = threeDS2ServiceMock

        // Mock
        every { threeDS2ServiceMock.createTransaction(any(), any()) } returns transactionMock
        every { threeDS2ServiceMock.sdkInfo } returns sDKInfoMock
        every { sDKInfoMock.schemeConfigurations } returns mutableListOf(schemeInfoMock)
        every { schemeInfoMock.name } returns "CB"
        every { schemeInfoMock.ids } returns mutableListOf("000042")
        every { threeDSBusiness.convertValueIfCB("CB") } returns "CB"
        every { transactionMock.authenticationRequestParameters } returns authenticationRequestParametersMock
        // Base64 YWFhYS1iYmJi = aaaa-bbbb
        // Base64 eHh4eC15eXl5 = xxxx-yyyy
        every { authenticationRequestParametersMock.sdkEphemeralPublicKey } returns "{\"kty\":\"EC\",\"x\":\"YWFhYS1iYmJi\",\"y\":\"eHh4eC15eXl5\",\"crv\":\"P-256\"}"
        every { authenticationRequestParametersMock.sdkReferenceNumber } returns expectedSdkReferenceNumber
        every { authenticationRequestParametersMock.sdkAppID } returns expectedSdkAppID
        every { authenticationRequestParametersMock.sdkTransactionID } returns expectedSdkTransactionID
        every { authenticationRequestParametersMock.deviceData } returns expectedDeviceData

        // Test
        val sdkContextData = underTest.generateSDKContextData(cardType = "CB")

        // Verif

        verify { threeDS2ServiceMock.createTransaction("000042", ThreeDSConfiguration.MESSAGE_VERSION) }
        assertEquals(ThreeDSConfiguration.DEFAULT_DEVICE_RENDERING_OPTIONS_IF, sdkContextData.deviceRenderingOptionsIF)
        assertEquals(ThreeDSConfiguration.DEFAULT_DEVICE_RENDER_OPTIONS_UI, sdkContextData.deviceRenderOptionsUI)
        assertEquals(ThreeDSConfiguration.MAX_TIMEOUT, sdkContextData.maxTimeout)
        assertEquals("P-256;EC;YWFhYS1iYmJi;eHh4eC15eXl5", sdkContextData.ephemPubKey)
        assertEquals(expectedSdkReferenceNumber, sdkContextData.referenceNumber)
        assertEquals(expectedSdkAppID, sdkContextData.appID)
        assertEquals(expectedSdkTransactionID, sdkContextData.transID)
        assertEquals(expectedDeviceData, sdkContextData.encData)
    }

    @Test
    fun initializeInSandboxEnvShouldCallOnCompleted() = runTest(testDispatcher) {

        val sessionToken = "sessionToken_xxx"
        val cardCode = "CB"
        val serverSdkKeyResponse = SdkTestHelper.createDirectoryServerSdkKeyResponse()

        // Mock
        every { threeDSBusiness.createConfigParameters() } returns configurationBuilderMock
        every { configurationBuilderMock.build() } returns configParametersMock
        coEvery { paymentApi.fetchDirectoryServerSdkKeys(any()) } returns serverSdkKeyResponse
        every { threeDSBusiness.createSchemeConfiguration(any(), any()) } returns schemeConfigurationMock
        every { schemeConfigurationMock.schemeName } returns "cartesBancaires"
        every { threeDS2ServiceMock.initialize(any(), any(),any(),any(),any()) } answers {
            // On mock la reponse pour invoquer la callback
            val callback = lastArg<ThreeDS2Service.InitializationCallback>()
            callback.onCompleted()
        }

        // Test
        underTest.startInitialize(sessionToken, cardCode)

        // Verif
        verify { configurationBuilderMock.build() }
        verify { configurationBuilderMock.configureScheme(schemeConfigurationMock) }
        coVerify { paymentApi.fetchDirectoryServerSdkKeys(sessionToken) }
        coVerify { threeDS2ServiceMock.initialize(context, configParametersMock, "EN", any(), any()) }
        coVerify { threeDS2ServiceMock.warnings }
        assertTrue(underTest.isInitialized)
    }

    @Test
    fun doChallengeFlowShouldThrowExceptionCauseServiceNotInitialized()= runBlocking {
        underTest.isInitialized = false;

        // Mock
        val activity = mockk<Activity>()
        val theme = mockk<Appearance>()
        val useCaseCallbackFromParent = mockk<ChallengeUseCaseCallback>(relaxed = true)

        // Test
        val exception = assertThrows<ThreeDsException> {
            underTest.doChallengeFlow(
                activity = activity,
                sdkChallengeData = SdkTestHelper.createSdkChallengeData(),
                theme = theme,
                useCaseCallback = useCaseCallbackFromParent
            )
        }

        // Verif
        assertEquals(ThreeDsExceptionType.NOT_INITIALISED, exception.type)
        assertEquals("Unable to start 3DS Challenge Flow", exception.message)
    }

    @Test
    fun doChallengeFlow() =runBlocking {
        underTest.isInitialized = true;

        val activityMock = mockk<Activity>()
        val themeMock = mockk<Appearance>()
        val progressViewMocK = mockk<ProgressView>(relaxed = true)
        val useCaseCallbackFromParent = mockk<ChallengeUseCaseCallback>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        underTest.currentOnGoingThreeDsTransaction = transactionMock;
        val sdkChallengeData = SdkTestHelper.createSdkChallengeData()
        val expectedChallengeParameters : ChallengeParameters = sdkChallengeData.toSdkChallengeParameters()

        // Mock
        every { transactionMock.getProgressView(any()) } returns progressViewMocK

        // Test
        underTest.doChallengeFlow(activity = activityMock, sdkChallengeData = sdkChallengeData, theme = themeMock, useCaseCallback = useCaseCallbackFromParent)

        // Verif
        verify { transactionMock.getProgressView(activityMock) }
        verify { progressViewMocK.showProgress() }
        verify { transactionMock.doChallenge(activityMock, capture(captureChallengeParameters), any(), 10 ) }
        assertTrue(underTest.isInitialized)

        assertEquals(expectedChallengeParameters.acsSignedContent, captureChallengeParameters.captured.acsSignedContent)
        assertEquals(expectedChallengeParameters.acsRefNumber, captureChallengeParameters.captured.acsRefNumber)
        assertEquals(expectedChallengeParameters.acsTransactionID, captureChallengeParameters.captured.acsTransactionID)
        assertEquals(expectedChallengeParameters.get3DSServerTransactionID(), captureChallengeParameters.captured.get3DSServerTransactionID())
    }

    @Test
    fun closeTransaction() {
        // data
        val mockk = mockk<Transaction>(relaxed = true)
        val threeDS2Service: ThreeDS2Service = mockk<ThreeDS2Service>(relaxed = true)
        underTest.currentOnGoingThreeDsTransaction = mockk
        underTest.threeDS2Service = threeDS2Service

        // Test
        underTest.closeTransaction()

        // Verif
        verify { mockk.close() }
        assertNull(underTest.currentOnGoingThreeDsTransaction)
        verify { threeDS2Service.cleanup(context) }
    }

    @Test
    fun closeTransactionShouldDoNothing() {
        assertDoesNotThrow {
            underTest.closeTransaction()
        }
    }
}