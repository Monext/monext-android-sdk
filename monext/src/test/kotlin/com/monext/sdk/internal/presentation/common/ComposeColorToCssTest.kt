package com.monext.sdk.internal.presentation.common

import kotlin.test.Test
import kotlin.test.assertEquals
import androidx.compose.ui.graphics.Color as ComposeColor

class ComposeColorToCssTest {

    @Test
    fun composeColorToCss_opaqueColor_returnsRgb() {
        // Given
        val red = ComposeColor(1f, 0f, 0f, 1f)

        // When
        val result = composeColorToCss(red)

        // Then
        assertEquals("rgb(255,0,0)", result)
    }

    @Test
    fun composeColorToCss_opaqueBlack_returnsRgb() {
        // Given
        val black = ComposeColor(0f, 0f, 0f, 1f)

        // When
        val result = composeColorToCss(black)

        // Then
        assertEquals("rgb(0,0,0)", result)
    }

    @Test
    fun composeColorToCss_opaqueWhite_returnsRgb() {
        // Given
        val white = ComposeColor(1f, 1f, 1f, 1f)

        // When
        val result = composeColorToCss(white)

        // Then
        assertEquals("rgb(255,255,255)", result)
    }

    @Test
    fun composeColorToCss_opaqueBlue_returnsRgb() {
        // Given
        val blue = ComposeColor(0f, 0f, 1f, 1f)

        // When
        val result = composeColorToCss(blue)

        // Then
        assertEquals("rgb(0,0,255)", result)
    }

    @Test
    fun composeColorToCss_transparentColor_returnsRgba() {
        // Given
        val transparentRed = ComposeColor(1f, 0f, 0f, 0.5f)

        // When
        val result = composeColorToCss(transparentRed)

        // Then
        assertEquals("rgba(255,0,0,0.50)", result)
    }

    @Test
    fun composeColorToCss_fullyTransparent_returnsRgba() {
        // Given
        val transparent = ComposeColor(0f, 0f, 0f, 0f)

        // When
        val result = composeColorToCss(transparent)

        // Then
        assertEquals("rgba(0,0,0,0.00)", result)
    }

    @Test
    fun composeColorToCss_almostOpaque_returnsRgba() {
        // Given - alpha < 0.999f doit donner rgba
        val almostOpaque = ComposeColor(0.5f, 0.5f, 0.5f, 0.998f)

        // When
        val result = composeColorToCss(almostOpaque)

        // Then
        assertEquals("rgba(128,128,128,1.00)", result)
    }

    @Test
    fun composeColorToCss_exactThreshold_returnsRgb() {
        // Given - alpha >= 0.999f doit donner rgb
        val exactThreshold = ComposeColor(0.5f, 0.5f, 0.5f, 0.999f)

        // When
        val result = composeColorToCss(exactThreshold)

        // Then
        assertEquals("rgb(128,128,128)", result)
    }

    @Test
    fun composeColorToCss_partialTransparency_returnsRgba() {
        // Given
        val semiTransparent = ComposeColor(0.2f, 0.4f, 0.6f, 0.75f)

        // When
        val result = composeColorToCss(semiTransparent)

        // Then
        assertEquals("rgba(51,102,153,0.75)", result)
    }

    @Test
    fun composeColorToCss_lowAlpha_returnsRgbaWithTwoDecimals() {
        // Given
        val lowAlpha = ComposeColor(1f, 1f, 1f, 0.01f)

        // When
        val result = composeColorToCss(lowAlpha)

        // Then
        assertEquals("rgba(255,255,255,0.01)", result)
    }

    @Test
    fun composeColorToCss_highAlpha_returnsRgbaWithTwoDecimals() {
        // Given
        val highAlpha = ComposeColor(0f, 0f, 0f, 0.99f)

        // When
        val result = composeColorToCss(highAlpha)

        // Then
        assertEquals("rgba(0,0,0,0.99)", result)
    }

    @Test
    fun composeColorToCss_intermediateValues_returnsCorrectRgb() {
        // Given
        val color = ComposeColor(0.5f, 0.25f, 0.75f, 1f)

        // When
        val result = composeColorToCss(color)

        // Then
        // 0.5 * 255 = 127.5 -> 128
        // 0.25 * 255 = 63.75 -> 64
        // 0.75 * 255 = 191.25 -> 191
        assertEquals("rgb(128,64,191)", result)
    }

    @Test
    fun composeColorToCss_edgeCaseRounding_returnsCorrectRgb() {
        // Given
        val color = ComposeColor(1.1f, -0.1f, 0.5f, 1f)

        // When
        val result = composeColorToCss(color)

        // Then
        assertEquals("rgb(255,0,128)", result)
    }
}