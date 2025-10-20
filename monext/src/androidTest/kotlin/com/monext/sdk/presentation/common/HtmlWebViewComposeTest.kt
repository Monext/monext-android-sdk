package com.monext.sdk.presentation.common

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.StrictMode
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.monext.sdk.FakeTestActivity
import com.monext.sdk.LocalEnvironment
import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.internal.presentation.common.HtmlWebView
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import androidx.compose.ui.graphics.Color as ComposeColor

class HtmlWebViewComposeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FakeTestActivity>()

    @Before
    fun setup() {
         StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX)
         StrictMode.setVmPolicy(StrictMode.VmPolicy.LAX)
    }

    @Test
    fun htmlWebView_appliesSettings_transparent() {
        // Given
        val html = "<p>Bonjour</p>"

        // When
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalEnvironment provides MnxtEnvironment.Sandbox) {
                HtmlWebView(
                    html = html,
                    enableJs = true,
                    transparent = true,
                    textColor = ComposeColor(1f, 0f, 0f, 1f),
                    fontSizePx = 16
                )
            }
        }

        composeTestRule.waitForIdle()

        // Then
        val root = composeTestRule.activity.window.decorView
        val webView = findViewOfType<WebView>(root)
        assertNotNull("WebView should be present in activity view hierarchy", webView)

        composeTestRule.runOnIdle {
            assertTrue(webView!!.settings.javaScriptEnabled)
            assertTrue(webView.layerType == View.LAYER_TYPE_SOFTWARE)
            val bgColor = (webView.background as? ColorDrawable)?.color ?: Color.TRANSPARENT
            assertTrue("Expected transparent background", bgColor == Color.TRANSPARENT)
        }
    }

    @Test
    fun htmlWebView_appliesSettings_nonTransparent() {
        // Given
        val html = "<p>Salut</p>"

        // When
        composeTestRule.activity.setTestComposable {
            CompositionLocalProvider(LocalEnvironment provides MnxtEnvironment.Sandbox) {
                HtmlWebView(
                    html = html,
                    enableJs = false,
                    transparent = false,
                    textColor = null,
                    fontSizePx = null
                )
            }
        }

        composeTestRule.waitForIdle()

        // Then
        val root = composeTestRule.activity.window.decorView
        val webView = findViewOfType<WebView>(root)
        assertNotNull("WebView should be present in activity view hierarchy", webView)

        composeTestRule.runOnIdle {
            assertTrue(!webView!!.settings.javaScriptEnabled)
            assertTrue(webView.layerType == View.LAYER_TYPE_HARDWARE)
            val bgColor = (webView.background as? ColorDrawable)?.color ?: Color.WHITE
            assertTrue("Expected white background", bgColor == Color.WHITE)
        }
    }

    private fun <T : View> findViewOfType(root: View?, clazz: Class<T>): T? {
        if (root == null) return null
        if (clazz.isInstance(root)) return clazz.cast(root)
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                val child = root.getChildAt(i)
                val found: T? = findViewOfType(child, clazz)
                if (found != null) return found
            }
        }
        return null
    }

    private inline fun <reified T : View> findViewOfType(root: View?): T? {
        return findViewOfType(root, T::class.java)
    }
}