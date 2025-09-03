package com.monext.sdk.internal

import com.monext.sdk.MnxtEnvironment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MnxtEnvironmentTest {

    @Test
    fun getHostTestSandbox()
    {
        assertEquals("homologation-payment.payline.com", MnxtEnvironment.Sandbox.host)
    }

    @Test
    fun getHostTestProduction()
    {
        assertEquals("payment.payline.com", MnxtEnvironment.Production.host)
    }

    @Test
    fun getHostTestShouldReturnCustomUrlCorrectly()
    {
        assertEquals(
            "monDomain.com",
            MnxtEnvironment.Custom("http://monDomain.com/path1/path2").host
        )
        assertEquals(
            "/path1/path2",
            MnxtEnvironment.Custom("monDomain.com/path1/path2").path
        )
    }

}