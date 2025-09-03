package com.monext.sdk.internal.presentation.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.ext.bold
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s16
import com.monext.sdk.internal.ext.s18
import com.monext.sdk.internal.presentation.common.AppButtonSecondaryOutlined
import com.monext.sdk.internal.preview.PreviewWrapper

@Composable
internal fun PaymentCanceledScreen(onExit: () -> Unit) {

    val theme = LocalAppearance.current

    Column {
        Column(
            Modifier
                .background(theme.headerBackgroundColor)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.payment_canceled_message),
                modifier = Modifier.testTag("cancel_header"),
                style = theme.baseTextStyle.bold().s16()
                    .foreground(theme.onHeaderBackgroundColor)
            )
        }

        val foregroundStyle = theme.baseTextStyle.bold().s18()
            .foreground(theme.onBackgroundColor)
            .copy(textAlign = TextAlign.Center)

        Column(
            Modifier.background(theme.surfaceColor)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppButtonSecondaryOutlined(onExit) {
                Text(
                    stringResource(R.string.button_return_to_app_title),
                    style = foregroundStyle.foreground(theme.onSurfaceColor)
                )
            }
        }
    }
}

@Preview
@Composable
internal fun PaymentCanceledScreenPreview() {
    PreviewWrapper {
        PaymentCanceledScreen {}
    }
}