package com.monext.sdk.internal.presentation.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.monext.sdk.LocalAppearance
import com.monext.sdk.internal.ext.bold
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s16

@Composable
internal fun NoActionDialog(title: String, message: String, onDismiss: () -> Unit) {

    val theme = LocalAppearance.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        icon = {},
        title = {
            Text(
                title,
                style = theme.baseTextStyle.bold().s16()
                    .foreground(theme.onSurfaceColor)
            )
        },
        text = {
            Text(
                message,
                style = theme.baseTextStyle.s16().foreground(theme.onSurfaceColor)
            )
        },
        shape = RoundedCornerShape(theme.cardRadius),
        containerColor = theme.surfaceColor
    )
}

