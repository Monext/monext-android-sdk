package com.monext.sdk.internal.presentation.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.data.sessionstate.PaymentOnholdPartner
import com.monext.sdk.internal.ext.bold
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s16
import com.monext.sdk.internal.ext.s18
import com.monext.sdk.internal.ext.s24
import com.monext.sdk.internal.presentation.common.AppButtonSecondaryFilled
import com.monext.sdk.internal.presentation.common.HtmlWebView
import com.monext.sdk.internal.presentation.common.PopImage
import com.monext.sdk.internal.presentation.common.PopImageStyle

@Composable
internal fun PaymentPendingScreen(paymentOnholdPartner: PaymentOnholdPartner, onExit: () -> Unit) {

    val theme = LocalAppearance.current

    Column {

        Column(
            Modifier.background(theme.backgroundColor).padding(16.dp).weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            PopImage(
                style = theme.pendingImage?.let { PopImageStyle.Custom(it) } ?: PopImageStyle.Pending
            )

            Text(
                stringResource(R.string.pending_title),
                style = theme.baseTextStyle.bold().s24()
                    .foreground(theme.onBackgroundColor),
                modifier = Modifier.testTag("pending_header")
            )

            if (!paymentOnholdPartner.message?.localizedMessage.isNullOrBlank()) {
                HtmlWebView(
                    paymentOnholdPartner.message.localizedMessage,
                    transparent = true,
                    fontSizePx = 16
                )
            } else {
                Text(
                    stringResource(R.string.pending_description),
                    style = theme.baseTextStyle.s16()
                        .foreground(theme.onBackgroundColor),
                    modifier = Modifier.testTag("pending_description")
                )
            }
        }

        Box(
            Modifier.Companion.background(theme.surfaceColor)
                .fillMaxWidth().padding(16.dp)
                .testTag("back_button")
        ) {
            AppButtonSecondaryFilled(onExit) {
                Text(
                    stringResource(R.string.button_return_to_app_title),
                    style = theme.baseTextStyle.bold().s18()
                )
            }
        }
    }
}