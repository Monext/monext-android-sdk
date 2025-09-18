package com.monext.sdkexample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.monext.sdkexample.ui.theme.MonextTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContainer(sessionManager: SessionManager) {
    MonextTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        ) { innerPadding ->
            MainContent(Modifier.padding(innerPadding), sessionManager)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    val context = LocalContext.current
    AppContainer(SessionManager(
        context = context,
        restoredToken = null
    ))
}