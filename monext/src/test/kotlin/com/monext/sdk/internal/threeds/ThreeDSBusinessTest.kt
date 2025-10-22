package com.monext.sdk.internal.threeds

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ThreeDSBusinessTest {

    private val underTest: ThreeDSBusiness = ThreeDSBusiness()

    @Test
    fun convertValueIfCB_shouldReturnCardTypeValueToSchemeValue() {
        assertEquals("visa", underTest.convertCardTypeValueToSchemeValue("visa"))
    }

    @Test
    fun convertValueIfCB_shouldConvertCardTypeValueToScheme() {
        assertEquals("cartesBancaires", underTest.convertCardTypeValueToSchemeValue("CB"))
    }

    @Test
    fun createConfigParameters() {
        val configParameters = underTest.createConfigParameters()
        assertNotNull(configParameters)
    }

}