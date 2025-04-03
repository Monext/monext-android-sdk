package com.monext.sdk.internal.service

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CustomLoggerTest {

    private lateinit var customLogger: CustomLogger

    companion object {
        private const val TEST_TAG = "TestTag"
        private const val TEST_MESSAGE = "Test message"
    }

    @BeforeEach
    fun setUp() {
        // Mock de toutes les méthodes statiques de android.util.Log
        mockkStatic("android.util.Log")

        // Créer l'instance à tester (signature inchangée)
        customLogger = CustomLogger()
    }

    @AfterEach
    fun tearDown() {
        // Nettoyer les mocks
        unmockkAll()
    }

    @Test
    fun testDebugShouldCallLogDebugWhenLoggableLevelIsDebug() {
        // Given
        every { android.util.Log.isLoggable(TEST_TAG, android.util.Log.DEBUG) } returns true
        every { android.util.Log.d(TEST_TAG, TEST_MESSAGE) } returns 0

        // When
        customLogger.d(TEST_TAG, TEST_MESSAGE)

        // Then
        verify(exactly = 1) { android.util.Log.d(TEST_TAG, TEST_MESSAGE) }
    }

    @Test
    fun testInfoShouldCallLogInfoWhenLoggableLevelIsInfo() {
        // Given
        every { android.util.Log.isLoggable(TEST_TAG, android.util.Log.INFO) } returns true
        every { android.util.Log.i(TEST_TAG, TEST_MESSAGE) } returns 0

        // When
        customLogger.i(TEST_TAG, TEST_MESSAGE)

        // Then
        verify(exactly = 1) { android.util.Log.i(TEST_TAG, TEST_MESSAGE) }
    }

    @Test
    fun testWarnShouldCallLogWarnWhenLoggableLevelIsWarnWithoutThrowable() {
        // Given
        every { android.util.Log.isLoggable(TEST_TAG, android.util.Log.WARN) } returns true
        every { android.util.Log.w(TEST_TAG, TEST_MESSAGE, null) } returns 0

        // When
        customLogger.w(TEST_TAG, TEST_MESSAGE, null)

        // Then
        verify(exactly = 1) { android.util.Log.w(TEST_TAG, TEST_MESSAGE, null) }
    }

    @Test
    fun testWarnShouldCallLogWarnWhenLoggableLevelIsWarnWithThrowable() {
        // Given
        val testException = RuntimeException("Test exception")
        every { android.util.Log.isLoggable(TEST_TAG, android.util.Log.WARN) } returns true
        every { android.util.Log.w(TEST_TAG, TEST_MESSAGE, testException) } returns 0

        // When
        customLogger.w(TEST_TAG, TEST_MESSAGE, testException)

        // Then
        verify(exactly = 1) { android.util.Log.w(TEST_TAG, TEST_MESSAGE, testException) }
    }

    @Test
    fun testErrorShouldCallLogErrorWhenLoggableLevelIsErrorWithoutThrowable() {
        // Given
        every { android.util.Log.isLoggable(TEST_TAG, android.util.Log.ERROR) } returns true
        every { android.util.Log.e(TEST_TAG, TEST_MESSAGE, null) } returns 0

        // When
        customLogger.e(TEST_TAG, TEST_MESSAGE, null)

        // Then
        verify(exactly = 1) { android.util.Log.e(TEST_TAG, TEST_MESSAGE, null) }
    }

    @Test
    fun testErrorShouldCallLogErrorWhenLoggableLevelIsErrorWithThrowable() {
        // Given
        val testException = IllegalStateException("Test error")
        every { android.util.Log.isLoggable(TEST_TAG, android.util.Log.ERROR) } returns true
        every { android.util.Log.e(TEST_TAG, TEST_MESSAGE, testException) } returns 0

        // When
        customLogger.e(TEST_TAG, TEST_MESSAGE, testException)

        // Then
        verify(exactly = 1) { android.util.Log.e(TEST_TAG, TEST_MESSAGE, testException) }
    }

    @Test
    fun testShouldHandleEmptyMessage() {
        // Given
        val emptyMessage = ""
        every { android.util.Log.i(TEST_TAG, emptyMessage) } returns 0

        // When
        customLogger.i(TEST_TAG, emptyMessage)

        // Then
        verify(exactly = 1) { android.util.Log.i(TEST_TAG, emptyMessage) }
    }

}