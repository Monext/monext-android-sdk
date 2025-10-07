package com.monext.sdk.internal.threeds.model

import com.monext.sdk.SdkTestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SdkChallengeDataTest {
    @Test
    fun toSdkChallengeParameters() {
        val sdkChallengeData = SdkTestHelper.createSdkChallengeData()

        val response = sdkChallengeData.toSdkChallengeParameters()

        assertEquals("threeDSServerTransID", response.get3DSServerTransactionID())
        assertEquals("acsTransID", response.acsTransactionID)
        assertEquals("acsReferenceNumber", response.acsRefNumber)
        assertEquals("acsSignedContent", response.acsSignedContent)
    }

    @Test
    fun toAuthenticationResponse() {
        val sdkChallengeData = SdkTestHelper.createSdkChallengeData()

        val response = sdkChallengeData.toAuthenticationResponse("HH")

        assertEquals("acsReferenceNumber", response.acsReferenceNumber)
        assertEquals("acsTransID", response.acsTransID)
        assertEquals("threeDSVersion", response.threeDSVersion)
        assertEquals("threeDSServerTransID", response.threeDSServerTransID)
        assertEquals("HH", response.transStatus)
    }

}
