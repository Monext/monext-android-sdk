package com.monext.sdk

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class FakeTestActivity : AppCompatActivity() {
    var testComposable: (@Composable () -> Unit)? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(androidx.appcompat.R.style.Theme_AppCompat)
        super.onCreate(savedInstanceState)

        setContent {
            // Thème par défaut ou votre thème de test
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Affiche le composable de test s'il est défini
                    testComposable?.invoke()
                }
            }
        }
    }

    fun setTestComposable(composable: @Composable () -> Unit) {
        testComposable = composable
        // Recomposer avec le nouveau contenu
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    composable()
                }
            }
        }
    }
}