package com.monext.sdk.internal.threeds

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ThreeDSBusinessTest {

    private val underTest: ThreeDSBusiness = ThreeDSBusiness()

    @Test
    fun convertValueIfCB_shouldReturnValue() {
        assertEquals("visa", underTest.convertValueIfCB("visa"))
    }

    @Test
    fun convertValueIfCB_shouldConvert() {
        assertEquals("cartesBancaires", underTest.convertValueIfCB("CB"))
    }

    @Test
    fun createConfigParameters() {
        val configParameters = underTest.createConfigParameters()
        assertNotNull(configParameters)
    }

}