package com.monext.sdk.internal.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance

@Composable
internal fun AppButtonSecondaryFilled(onClick: () -> Unit, title: @Composable () -> Unit) {

    val theme = LocalAppearance.current

    Surface(
        onClick,
        Modifier.Companion.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(theme.buttonRadius),
        color = theme.secondaryColor,
        contentColor = theme.onSecondaryColor
    ) {
        Box(contentAlignment = Alignment.Companion.Center) {
            title()
        }
    }
}