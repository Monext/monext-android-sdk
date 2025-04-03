package com.monext.sdk.internal.presentation.common

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.ext.bold
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s16
import com.monext.sdk.internal.ext.s18

@Composable
internal fun ExitPaymentDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {

    val theme = LocalAppearance.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onDismiss,
                shape = RoundedCornerShape(theme.buttonRadius),
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = theme.secondaryColor,
                    contentColor = theme.onSecondaryColor
                )
            ) {
                Text(
                    stringResource(R.string.dialog_exit_payment_dismiss),
                    style = theme.baseTextStyle.bold().s16()
                )
            }
        },
        dismissButton = {
            TextButton(
                onConfirm,
                colors = ButtonDefaults.buttonColors().copy(
                    containerColor = Color.Transparent,
                    contentColor = theme.onSurfaceColor
                )
            ) {
                Text(
                    stringResource(R.string.dialog_exit_payment_confirm),
                    style = theme.baseTextStyle.bold().s18()
                )
            }
        },
        icon = {},
        title = {
            Text(
                stringResource(R.string.dialog_exit_payment_title),
                style = theme.baseTextStyle.bold().s16()
                    .foreground(theme.onSurfaceColor)
            )
        },
        text = {
            Text(
                stringResource(R.string.dialog_exit_payment_message),
                style = theme.baseTextStyle.s16().foreground(theme.onSurfaceColor)
            )
        },
        shape = RoundedCornerShape(theme.cardRadius),
        containerColor = theme.surfaceColor
    )
}