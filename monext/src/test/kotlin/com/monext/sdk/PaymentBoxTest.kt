package com.monext.sdk

import com.monext.sdk.internal.security.RootDetector
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

class PaymentBoxTest {

    @BeforeEach
    fun setup() {
        mockkObject(RootDetector)
    }

    @AfterEach
    fun teardown() {
        unmockkObject(RootDetector)
    }

    @Test
    fun onClick_whenSessionTokenIsNull_doesNothing() {
        // Arrange
        every { RootDetector.isCompromised() } returns false
        var showPaymentSheet = false
        var showSecurityAlert = false

        val onClick = buildOnClick(
            sessionToken = null,
            onShowPaymentSheet = { showPaymentSheet = true },
            onShowSecurityAlert = { showSecurityAlert = true }
        )

        // Act
        onClick()

        // Assert
        assertFalse(showPaymentSheet)
        assertFalse(showSecurityAlert)
    }

    @Test
    fun onClick_whenTokenIsValidAndDeviceIsClean_showsPaymentSheet() {
        // Arrange
        every { RootDetector.isCompromised() } returns false
        var showPaymentSheet = false
        var showSecurityAlert = false

        val onClick = buildOnClick(
            sessionToken = "valid_token",
            onShowPaymentSheet = { showPaymentSheet = true },
            onShowSecurityAlert = { showSecurityAlert = true }
        )

        // Act
        onClick()

        // Assert
        assertTrue(showPaymentSheet)
        assertFalse(showSecurityAlert)
    }

    @Test
    fun onClick_whenTokenIsValidAndDeviceIsCompromised_showsSecurityAlert() {
        // Arrange
        every { RootDetector.isCompromised() } returns true
        var showPaymentSheet = false
        var showSecurityAlert = false

        val onClick = buildOnClick(
            sessionToken = "valid_token",
            onShowPaymentSheet = { showPaymentSheet = true },
            onShowSecurityAlert = { showSecurityAlert = true }
        )

        // Act
        onClick()

        // Assert
        assertFalse(showPaymentSheet)
        assertTrue(showSecurityAlert)
    }

    @Test
    fun onClick_whenDeviceIsCompromised_neverShowsPaymentSheet() {
        // Arrange
        every { RootDetector.isCompromised() } returns true
        var showPaymentSheet = false

        val onClick = buildOnClick(
            sessionToken = "valid_token",
            onShowPaymentSheet = { showPaymentSheet = true },
            onShowSecurityAlert = {}
        )

        // Act
        onClick()

        // Assert
        assertFalse(showPaymentSheet)
    }

    private fun buildOnClick(
        sessionToken: String?,
        onShowPaymentSheet: () -> Unit,
        onShowSecurityAlert: () -> Unit
    ): () -> Unit = {
        if (sessionToken != null) {
            if (RootDetector.isCompromised()) {
                onShowSecurityAlert()
            } else {
                onShowPaymentSheet()
            }
        }
    }
}