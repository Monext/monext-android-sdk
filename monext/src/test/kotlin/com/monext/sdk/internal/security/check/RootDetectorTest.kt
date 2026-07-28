package com.monext.sdk.internal.security

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RootDetectorTest {

    private val context: Context = mockk(relaxed = true)

    @BeforeEach
    fun setup() {
        every { context.applicationContext } returns context
        mockkConstructor(RootDetectorImpl::class)
        every { anyConstructed<RootDetectorImpl>().isDeviceCompromised() } returns false
        resetSingletonState()
    }

    @AfterEach
    fun teardown() {
        resetSingletonState()
        unmockkAll()
    }

    @Test
    fun isCompromisedReturnsFalseBeforeInit() {
        assertFalse(RootDetector.isCompromised())
    }

    @Test
    fun isCompromisedReturnsFalseOnCleanDevice() {
        every { anyConstructed<RootDetectorImpl>().isDeviceCompromised() } returns false
        RootDetector.init(context)
        assertFalse(RootDetector.isCompromised())
    }

    @Test
    fun isCompromisedReturnsTrueOnRootedDevice() {
        every { anyConstructed<RootDetectorImpl>().isDeviceCompromised() } returns true
        RootDetector.init(context)
        assertTrue(RootDetector.isCompromised())
    }

    @Test
    fun initIsIdempotent() {
        RootDetector.init(context)
        RootDetector.init(context)
        RootDetector.init(context)
        // 1 seul refreshCheck déclenché par init
        verify(exactly = 1) { anyConstructed<RootDetectorImpl>().isDeviceCompromised() }
    }

    @Test
    fun isCompromisedUsesCacheWithinTtl() {
        RootDetector.init(context) // refresh #1
        repeat(5) { RootDetector.isCompromised() }
        verify(exactly = 1) { anyConstructed<RootDetectorImpl>().isDeviceCompromised() }
    }

    @Test
    fun isCompromisedRefreshesAfterTtlExpires() {
        RootDetector.init(context) // refresh #1
        setLastCheckTime(System.currentTimeMillis() - 31_000L)
        RootDetector.isCompromised() // refresh #2
        verify(exactly = 2) { anyConstructed<RootDetectorImpl>().isDeviceCompromised() }
    }

    @Test
    fun isCompromisedReflectsLatestStateAfterTtlExpires() {
        every { anyConstructed<RootDetectorImpl>().isDeviceCompromised() } returns false
        RootDetector.init(context)
        assertFalse(RootDetector.isCompromised())

        every { anyConstructed<RootDetectorImpl>().isDeviceCompromised() } returns true
        setLastCheckTime(System.currentTimeMillis() - 31_000L)
        assertTrue(RootDetector.isCompromised())
    }

    @Test
    fun forceRefreshAlwaysRecomputes() {
        RootDetector.init(context) // refresh #1
        RootDetector.forceRefresh() // #2
        RootDetector.forceRefresh() // #3
        verify(exactly = 3) { anyConstructed<RootDetectorImpl>().isDeviceCompromised() }
    }

    @Test
    fun forceRefreshReturnsUpdatedValue() {
        every { anyConstructed<RootDetectorImpl>().isDeviceCompromised() } returns false
        RootDetector.init(context)
        assertFalse(RootDetector.isCompromised())

        every { anyConstructed<RootDetectorImpl>().isDeviceCompromised() } returns true
        assertTrue(RootDetector.forceRefresh())
    }

    private fun resetSingletonState() {
        setField("initialized", false)
        setField("compromised", false)
        setField("lastCheckTime", 0L)
        setField("detectorImpl", null)
    }

    private fun setLastCheckTime(time: Long) = setField("lastCheckTime", time)

    private fun setField(name: String, value: Any?) {
        RootDetector::class.java.getDeclaredField(name).apply {
            isAccessible = true
            set(RootDetector, value)
        }
    }
}