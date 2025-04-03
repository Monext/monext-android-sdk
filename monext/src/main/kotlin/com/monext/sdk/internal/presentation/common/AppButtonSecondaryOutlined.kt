package com.monext.sdk.internal.presentation.common

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
internal fun AppButtonSecondaryOutlined(onClick: () -> Unit, title: @Composable BoxScope.() -> Unit) {

    val theme = LocalAppearance.current

    Surface(
        onClick,
        Modifier.fillMaxWidth().height(48.dp)
            .border(2.dp, theme.secondaryColor, RoundedCornerShape(theme.buttonRadius)),
        color = theme.surfaceColor,
        contentColor = theme.onSurfaceColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            title()
        }
    }
}