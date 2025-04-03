package com.monext.sdk.internal.presentation.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.internal.preview.PreviewWrapper

@Composable
internal fun LoadingSection() {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            Modifier.padding(16.dp),
            color = LocalAppearance.current.onHeaderBackgroundColor
        )
    }
}

@Preview
@Composable
internal fun LoadingSectionPreview() {
    PreviewWrapper {
        LoadingSection()
    }
}