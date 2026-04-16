package com.monext.sdk.internal.service

import com.monext.sdk.MnxtEnvironment
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection

@OptIn(ExperimentalCoroutinesApi::class)
class CustomLoggerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private lateinit var mockConnection: HttpURLConnection
    private lateinit var customLogger: CustomLogger

    private val capturedUrls = mutableListOf<String>()
    private val capturedOutput = ByteArrayOutputStream()

    companion object {
        private const val TEST_TAG = "TestTag"
        private const val TEST_MESSAGE = "Test message"
    }

    @BeforeEach
    fun setUp() {
        mockkStatic("android.util.Log")

        // Mocker TOUTES les surcharges de Log pour éviter les crashes silencieux
        // dans les blocs onFailure et dans postForm
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.w(any(), any<String>(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.e(any(), any<String>(), any()) } returns 0

        capturedUrls.clear()
        capturedOutput.reset()

        mockConnection = mockk(relaxed = true)
        every { mockConnection.outputStream } returns capturedOutput
        every { mockConnection.responseCode } returns 200

        customLogger = buildLogger(MnxtEnvironment.Sandbox)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    /** Fabrique un CustomLogger avec ConnectionFactory et appVersion mockés */
    private fun buildLogger(
        environment: MnxtEnvironment,
        scope: CoroutineScope = testScope
    ) = CustomLogger(
        environment = environment,
        reportingScope = scope,
        appVersionName = "1.0",
        appVersionCode = "1122",
        isSendRemoteLogs = true,
        connectionFactory = CustomLogger.ConnectionFactory { url ->
            capturedUrls.add(url)
            mockConnection
        }
    )

    // -----------------------------------------------------------------------
    // Logs basiques
    // -----------------------------------------------------------------------

    @Nested
    inner class BasicLogging {

        @Test
        fun `debug should call Log-d`() {
            customLogger.d(TEST_TAG, TEST_MESSAGE)
            verify(exactly = 1) { android.util.Log.d(TEST_TAG, TEST_MESSAGE) }
        }

        @Test
        fun `info should call Log-i`() {
            customLogger.i(TEST_TAG, TEST_MESSAGE)
            verify(exactly = 1) { android.util.Log.i(TEST_TAG, TEST_MESSAGE) }
        }

        @Test
        fun `warn without throwable should call Log-w`() {
            customLogger.w(TEST_TAG, TEST_MESSAGE, null)
            verify(exactly = 1) { android.util.Log.w(TEST_TAG, TEST_MESSAGE, null) }
        }

        @Test
        fun `warn with throwable should call Log-w with throwable`() {
            val exception = RuntimeException("Test exception")
            customLogger.w(TEST_TAG, TEST_MESSAGE, exception)
            verify(exactly = 1) { android.util.Log.w(TEST_TAG, TEST_MESSAGE, exception) }
        }

        @Test
        fun `error without throwable should call Log-e`() {
            customLogger.e(TEST_TAG, TEST_MESSAGE, null)
            verify(exactly = 1) { android.util.Log.e(TEST_TAG, TEST_MESSAGE, null) }
        }

        @Test
        fun `error with throwable should call Log-e with throwable`() {
            val exception = IllegalStateException("Test error")
            customLogger.e(TEST_TAG, TEST_MESSAGE, exception)
            verify(exactly = 1) { android.util.Log.e(TEST_TAG, TEST_MESSAGE, exception) }
        }

        @Test
        fun `should handle empty message`() {
            customLogger.i(TEST_TAG, "")
            verify(exactly = 1) { android.util.Log.i(TEST_TAG, "") }
        }
    }

    // -----------------------------------------------------------------------
    // Remote reporting
    // -----------------------------------------------------------------------

    @Nested
    inner class RemoteReporting {

        @Test
        fun `error should trigger a POST to the remote server`() = testScope.runTest {
            customLogger.e(TEST_TAG, TEST_MESSAGE, null)
            advanceUntilIdle()

            verify(exactly = 1) { mockConnection.requestMethod = "POST" }
        }

        @Test
        fun `error should set content-type to application x-www-form-urlencoded`() = testScope.runTest {
            customLogger.e(TEST_TAG, TEST_MESSAGE, null)
            advanceUntilIdle()

            verify { mockConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded") }
        }

        @Test
        fun `error should post to URL ending with log path`() = testScope.runTest {
            customLogger.e(TEST_TAG, TEST_MESSAGE, null)
            advanceUntilIdle()

            val calledUrl = capturedUrls.firstOrNull()
            assertTrue(calledUrl != null, "Aucun appel HTTP effectué")
            assertTrue(calledUrl!!.endsWith("/log"), "L'URL doit se terminer par /log — reçu : $calledUrl")
        }

        @Test
        fun `error should write data param in request body`() = testScope.runTest {
            customLogger.e(TEST_TAG, TEST_MESSAGE, null)
            advanceUntilIdle()

            val body = capturedOutput.toString(Charsets.UTF_8)
            assertTrue(body.startsWith("data="), "Le body doit commencer par 'data=' — reçu : '$body'")
        }

        @Test
        fun `error body should contain the tag and message`() = testScope.runTest {
            customLogger.e(TEST_TAG, TEST_MESSAGE, null)
            advanceUntilIdle()

            val body = capturedOutput.toString(Charsets.UTF_8)
            val decoded = java.net.URLDecoder.decode(body.removePrefix("data="), "UTF-8")
            assertTrue(decoded.contains(TEST_TAG), "Le body doit contenir le tag — reçu : $decoded")
            assertTrue(decoded.contains(TEST_MESSAGE), "Le body doit contenir le message — reçu : $decoded")
        }

        @Test
        fun `error body should contain exception info when throwable is provided`() = testScope.runTest {
            val exception = RuntimeException("Boom")
            customLogger.e(TEST_TAG, TEST_MESSAGE, exception)
            advanceUntilIdle()

            val body = capturedOutput.toString(Charsets.UTF_8)
            val decoded = java.net.URLDecoder.decode(body.removePrefix("data="), "UTF-8")
            assertTrue(decoded.contains("RuntimeException"), "Le body doit contenir le type d'exception — reçu : $decoded")
            assertTrue(decoded.contains("Boom"), "Le body doit contenir le message d'exception — reçu : $decoded")
        }

        @Test
        fun `error whith 'isSendRemoteLogs' = false should NOT trigger remote reporting`() = testScope.runTest {
            val currentLogger = CustomLogger(
                environment = MnxtEnvironment.Sandbox,
                isSendRemoteLogs = false,
                reportingScope = testScope,
                appVersionName = "1.0",
                appVersionCode = "1122"
            )

            val exception = RuntimeException("Boom")
            currentLogger.e(TEST_TAG, TEST_MESSAGE, exception)
            advanceUntilIdle()

            assertEquals(0, capturedUrls.size, "error whith 'isSendRemoteLogs' = false ne doit pas déclencher d'appel HTTP")
        }

        @Test
        fun `debug should NOT trigger remote reporting`() = testScope.runTest {
            customLogger.d(TEST_TAG, TEST_MESSAGE)
            advanceUntilIdle()

            assertEquals(0, capturedUrls.size, "debug ne doit pas déclencher d'appel HTTP")
        }

        @Test
        fun `info should NOT trigger remote reporting`() = testScope.runTest {
            customLogger.i(TEST_TAG, TEST_MESSAGE)
            advanceUntilIdle()

            assertEquals(0, capturedUrls.size, "info ne doit pas déclencher d'appel HTTP")
        }

        @Test
        fun `warn should NOT trigger remote reporting`() = testScope.runTest {
            customLogger.w(TEST_TAG, TEST_MESSAGE, null)
            advanceUntilIdle()

            assertEquals(0, capturedUrls.size, "warn ne doit pas déclencher d'appel HTTP")
        }

        @Test
        fun `network failure should not propagate and should log a warning`() = testScope.runTest {
            val failingLogger = CustomLogger(
                environment = MnxtEnvironment.Sandbox,
                isSendRemoteLogs = true,
                reportingScope = testScope,
                appVersionName = "1.0",
                appVersionCode = "1122",
                connectionFactory = CustomLogger.ConnectionFactory {
                    throw RuntimeException("Network unavailable")
                }
            )

            failingLogger.e(TEST_TAG, TEST_MESSAGE, null)
            advanceUntilIdle()

            // onFailure doit appeler Log.w à 2 arguments
            verify(atLeast = 1) { android.util.Log.w(any(), any<String>()) }
        }

        @Test
        fun `connection should always be disconnected even on HTTP error`() = testScope.runTest {
            every { mockConnection.responseCode } returns 500

            customLogger.e(TEST_TAG, TEST_MESSAGE, null)
            advanceUntilIdle()

            verify(exactly = 1) { mockConnection.disconnect() }
        }
    }

    // -----------------------------------------------------------------------
    // Construction de l'URL
    // -----------------------------------------------------------------------

    @Nested
    inner class UrlBuilding {

        @Test
        fun `sandbox environment should build URL with sandbox host`() = testScope.runTest {
            buildLogger(MnxtEnvironment.Sandbox).e(TEST_TAG, TEST_MESSAGE, null)
            advanceUntilIdle()

            val url = capturedUrls.firstOrNull()
            assertTrue(url?.contains(MnxtEnvironment.Sandbox.host) == true,
                "URL attendue avec host sandbox — reçu : $url")
        }

        @Test
        fun `production environment should build URL with production host`() = testScope.runTest {
            buildLogger(MnxtEnvironment.Production).e(TEST_TAG, TEST_MESSAGE, null)
            advanceUntilIdle()

            val url = capturedUrls.firstOrNull()
            assertTrue(url?.contains(MnxtEnvironment.Production.host) == true,
                "URL attendue avec host production — reçu : $url")
        }

        @Test
        fun `built URL should use HTTPS scheme`() = testScope.runTest {
            customLogger.e(TEST_TAG, TEST_MESSAGE, null)
            advanceUntilIdle()

            val url = capturedUrls.firstOrNull()
            assertTrue(url?.startsWith("https://") == true,
                "URL doit utiliser HTTPS — reçu : $url")
        }
    }
}