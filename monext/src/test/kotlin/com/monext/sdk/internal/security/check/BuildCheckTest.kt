package com.monext.sdk.internal.security.check

import android.os.Build
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.reflect.Field

class BuildCheckTest {

    private val buildCheck = BuildCheck()

    private val originalValues = BUILD_FIELDS.associateWith { readBuildField(it) }

    @BeforeEach
    fun setSafeDefaults() {
        setBuildField("TAGS", "release-keys")
        setBuildField("FINGERPRINT", "google/walleye/walleye:11/RP1A.201005.004/1234:user/release-keys")
        setBuildField("PRODUCT", "walleye")
        setBuildField("HARDWARE", "walleye")
        setBuildField("MANUFACTURER", "Google")
        setBuildField("MODEL", "Pixel 2")
        setBuildField("BRAND", "google")
    }

    @AfterEach
    fun restoreOriginalBuildValues() {
        originalValues.forEach { (name, value) -> setBuildField(name, value) }
    }

    @Test
    fun checkReturnsFalseOnLegitimateDevice() {
        assertFalse(buildCheck.check())
    }

    // --- detectTestKeys ---

    @Test
    fun checkReturnsTrueWhenTagsContainsTestKeys() {
        setBuildField("TAGS", "test-keys")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsFalseWhenTagsIsNull() {
        setBuildField("TAGS", null)
        assertFalse(buildCheck.check())
    }

    // --- detectCustomRom ---

    @Test
    fun checkReturnsTrueWhenFingerprintContainsCustom() {
        setBuildField("FINGERPRINT", "lineageos/custom_rom/build:11/...:user/release-keys")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenFingerprintContainsTestKeys() {
        setBuildField("FINGERPRINT", "generic/sdk/sdk:11/...:userdebug/test-keys")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenFingerprintContainsCustomUppercase() {
        setBuildField("FINGERPRINT", "Some/CUSTOM/Build:11/...:user/release-keys")
        assertTrue(buildCheck.check())
    }

    // --- detectEmulator ---

    @Test
    fun checkReturnsTrueWhenProductContainsSdk() {
        setBuildField("PRODUCT", "sdk_gphone_x86")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenProductContainsSimulator() {
        setBuildField("PRODUCT", "android_simulator")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenProductContainsVbox86p() {
        setBuildField("PRODUCT", "vbox86p")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenHardwareIsGoldfish() {
        setBuildField("HARDWARE", "goldfish")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenHardwareIsRanchu() {
        setBuildField("HARDWARE", "ranchu")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenManufacturerIsGenymotion() {
        setBuildField("MANUFACTURER", "Genymotion")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenManufacturerIsGenymotionLowercase() {
        setBuildField("MANUFACTURER", "genymotion")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenModelContainsEmulator() {
        setBuildField("MODEL", "Android Emulator")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenModelContainsEmulatorLowercase() {
        setBuildField("MODEL", "some emulator device")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenModelIsAndroidSdkBuiltForX86() {
        setBuildField("MODEL", "Android SDK built for x86")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenBrandIsGeneric() {
        setBuildField("BRAND", "generic")
        assertTrue(buildCheck.check())
    }

    @Test
    fun checkReturnsTrueWhenBrandIsGenericUppercase() {
        setBuildField("BRAND", "GENERIC")
        assertTrue(buildCheck.check())
    }

    companion object {
        private val BUILD_FIELDS = listOf(
            "TAGS", "FINGERPRINT", "PRODUCT", "HARDWARE",
            "MANUFACTURER", "MODEL", "BRAND"
        )

        private val unsafeClass: Class<*> = Class.forName("sun.misc.Unsafe")

        private val unsafe: Any = run {
            val theUnsafe = unsafeClass.getDeclaredField("theUnsafe")
            theUnsafe.isAccessible = true
            theUnsafe.get(null)
        }

        private val staticFieldBase =
            unsafeClass.getMethod("staticFieldBase", Field::class.java)

        private val staticFieldOffset =
            unsafeClass.getMethod("staticFieldOffset", Field::class.java)

        private val putObject = unsafeClass.getMethod(
            "putObject",
            Any::class.java,
            java.lang.Long.TYPE,
            Any::class.java
        )

        private fun readBuildField(name: String): Any? =
            Build::class.java.getField(name).get(null)

        private fun setBuildField(name: String, value: Any?) {
            val field = Build::class.java.getField(name)
            val base = staticFieldBase.invoke(unsafe, field)
            val offset = staticFieldOffset.invoke(unsafe, field) as Long
            putObject.invoke(unsafe, base, offset, value)
        }
    }
}