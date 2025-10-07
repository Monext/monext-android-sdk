package com.monext.sdk.internal.preview

import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.MnxtSDKContext
import com.monext.sdk.SdkTestHelper
import com.monext.sdk.internal.api.AvailableCardNetworksRequest
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * C'est de la preview, on fait des tests not null seulement...
 */
class PaymentAPIPreviewSuccessTest {

    private val underTest : PaymentAPIPreviewSuccess = PaymentAPIPreviewSuccess

    @Test
    fun payment() = runBlocking {
        assertEquals(PreviewSamples.sessionStateSuccess, underTest.payment("aa", SdkTestHelper.createPaymentRequestCB()))
    }

    @Test
    fun sdkPaymentRequest() = runBlocking {
        assertEquals(PreviewSamples.sessionStateSuccess, underTest.sdkPaymentRequest("aa", SdkTestHelper.createAuthenticationResponse()))
    }

    @Test
    fun stateCurrent() = runBlocking {
        assertEquals(PreviewSamples.sessionStatePaymentMethodsList, underTest.stateCurrent("aa"))
    }

    @Test
    fun updateContext() = runBlocking {
        assertDoesNotThrow { underTest.updateContext(InternalSDKContext(MnxtSDKContext(MnxtEnvironment.Sandbox))) }
    }

    @Test
    fun walletPayment() = runBlocking {
        assertEquals(PreviewSamples.sessionStateSuccess, underTest.walletPayment("aa", SdkTestHelper.createWalletPaymentRequest()))
    }

    @Test
    fun securedPayment() = runBlocking {
        assertEquals(PreviewSamples.sessionStateSuccess, underTest.securedPayment("aa", SdkTestHelper.createSecuredPaymentRequestCB()))
    }

    @Test
    fun availableCardNetworks() = runBlocking {
        assertEquals(SdkTestHelper.createAvailableCardNetworksResponse(), underTest.availableCardNetworks("aa", SdkTestHelper.createAvailableCardNetworksRequest()))
    }

    @Test
    fun fetchDirectoryServerSdkKeys() = runBlocking {
        assertEquals(SdkTestHelper.createDirectoryServerSdkKeyResponse(), underTest.fetchDirectoryServerSdkKeys("aa"))
    }

}