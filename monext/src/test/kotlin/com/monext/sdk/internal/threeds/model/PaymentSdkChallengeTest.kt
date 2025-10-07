package com.monext.sdk.internal.threeds.model

import com.monext.sdk.SdkTestHelper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PaymentSdkChallengeTest {

    @Test
    fun constructor() {

        val sdkChallengeData = SdkTestHelper.createSdkChallengeData()

        val paymentSdkChallenge = PaymentSdkChallenge(sdkChallengeData)

        assertNotNull(paymentSdkChallenge)
        assertEquals(sdkChallengeData, paymentSdkChallenge.sdkChallengeData)
    }
}