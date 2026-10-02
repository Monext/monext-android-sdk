package com.monext.sdk.internal.security.check

import android.os.Debug
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DebuggerCheckTest {

    private val debuggerCheck = DebuggerCheck()

    @BeforeEach
    fun setup() {
        mockkStatic(Debug::class)
        every { Debug.isDebuggerConnected() } returns false
        every { Debug.waitingForDebugger() } returns false
        // timing par défaut : sous le seuil (10ms)
        every { Debug.threadCpuTimeNanos() } returnsMany listOf(0L, 1_000_000L)
    }

    @AfterEach
    fun teardown() = unmockkAll()

    @Test
    fun checkReturnsFalseWhenNoDebuggerAndFastTiming() {
        assertFalse(debuggerCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenJavaDebuggerConnected() {
        every { Debug.isDebuggerConnected() } returns true
        assertTrue(debuggerCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenTimingExceedsThreshold() {
        every { Debug.threadCpuTimeNanos() } returnsMany listOf(0L, 50_000_000L)
        assertTrue(debuggerCheck.check())
    }

    @Test
    fun detectJavaDebuggerReturnsFalseWhenNeitherFlagSet() {
        assertFalse(debuggerCheck.detectJavaDebugger())
    }

    @Test
    fun detectJavaDebuggerReturnsTrueWhenDebuggerConnected() {
        every { Debug.isDebuggerConnected() } returns true
        assertTrue(debuggerCheck.detectJavaDebugger())
    }

    @Test
    fun detectJavaDebuggerReturnsTrueWhenWaitingForDebugger() {
        every { Debug.waitingForDebugger() } returns true
        assertTrue(debuggerCheck.detectJavaDebugger())
    }

    @Test
    fun detectJavaDebuggerReturnsTrueWhenBothFlagsSet() {
        every { Debug.isDebuggerConnected() } returns true
        every { Debug.waitingForDebugger() } returns true
        assertTrue(debuggerCheck.detectJavaDebugger())
    }

    @Test
    fun detectDebuggerByTimingReturnsFalseWhenElapsedBelowThreshold() {
        every { Debug.threadCpuTimeNanos() } returnsMany listOf(0L, 9_999_999L)
        assertFalse(debuggerCheck.detectDebuggerByTiming())
    }

    @Test
    fun detectDebuggerByTimingReturnsTrueWhenElapsedEqualsThreshold() {
        every { Debug.threadCpuTimeNanos() } returnsMany listOf(0L, 10_000_000L)
        assertTrue(debuggerCheck.detectDebuggerByTiming())
    }

    @Test
    fun detectDebuggerByTimingReturnsTrueWhenElapsedFarAboveThreshold() {
        every { Debug.threadCpuTimeNanos() } returnsMany listOf(0L, 500_000_000L)
        assertTrue(debuggerCheck.detectDebuggerByTiming())
    }

    @Test
    fun detectDebuggerByTimingHandlesNonZeroStartTime() {
        // Start à 1 000 000, end à 11 000 000 → elapsed = 10 000 000 (= seuil)
        every { Debug.threadCpuTimeNanos() } returnsMany listOf(1_000_000L, 11_000_000L)
        assertTrue(debuggerCheck.detectDebuggerByTiming())
    }
}