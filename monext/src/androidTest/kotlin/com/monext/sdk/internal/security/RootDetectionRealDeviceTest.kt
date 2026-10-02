package com.monext.sdk.internal.security

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.monext.sdk.internal.security.check.BinaryCheck
import com.monext.sdk.internal.security.check.BuildCheck
import com.monext.sdk.internal.security.check.DebuggerCheck
import com.monext.sdk.internal.security.check.FridaCheck
import com.monext.sdk.internal.security.check.PackageCheck
import com.monext.sdk.internal.security.check.ProcessCheck
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RootDetectionRealDeviceTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun buildCheck_shouldTriggerOnEmulator() {
        assertTrue(BuildCheck().check())
    }

    @Test
    fun binaryCheck_shouldNotTriggerOnCleanEmulator() {
        assertTrue(BinaryCheck().check())
    }

    @Test
    fun packageCheck_shouldNotTriggerOnCleanEmulator() {
        assertFalse(PackageCheck(context.packageManager).check())
    }

    @Test
    fun processCheck_shouldNotTriggerOnCleanEmulator() {
        assertFalse(ProcessCheck(context).check())
    }

    @Test
    fun fridaCheck_shouldNotTriggerOnCleanEmulator() {
        assertFalse(FridaCheck().check())
    }

    @Test
    fun debuggerCheck_jdwpOnly_shouldNotTrigger() {
        assertFalse(DebuggerCheck().detectJavaDebugger())
    }
}