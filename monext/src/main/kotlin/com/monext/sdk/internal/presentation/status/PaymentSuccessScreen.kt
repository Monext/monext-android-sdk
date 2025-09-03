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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.data.sessionstate.PaymentSuccess
import com.monext.sdk.internal.api.model.SessionInfo
import com.monext.sdk.internal.ext.bold
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s18
import com.monext.sdk.internal.ext.s24
import com.monext.sdk.internal.presentation.common.AppButtonSecondaryFilled
import com.monext.sdk.internal.presentation.common.PopImage
import com.monext.sdk.internal.presentation.common.PopImageStyle
import com.monext.sdk.internal.preview.PreviewSamples
import com.monext.sdk.internal.preview.PreviewWrapper

@Composable
internal fun PaymentSuccessScreen(info: SessionInfo, successData: PaymentSuccess, onExit: () -> Unit) {

    val theme = LocalAppearance.current

    Column {

        Column(
            Modifier.background(theme.backgroundColor).padding(16.dp).weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            PopImage(
                style = theme.successImage?.let { PopImageStyle.Custom(it) } ?: PopImageStyle.Success
            )

            Text(
                stringResource(R.string.payment_success_header),
                style = theme.baseTextStyle.bold().s24()
                    .foreground(theme.onBackgroundColor),
                modifier = Modifier.testTag("success_title")
            )

            Text(
                stringResource(
                    R.string.payment_success_message,
                    info.formattedAmount,
                    info.orderRef
                ),
                style = theme.baseTextStyle.bold().s18()
                    .foreground(theme.onBackgroundColor)
            )

            if (successData.displayTicket) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (successData.ticket != null) {
                        for (ticket in successData.ticket) {
                            TicketItem(ticket)
                        }
                    }
                }
            }
        }

        Box(
            Modifier.Companion.background(theme.surfaceColor)
                .fillMaxWidth().padding(16.dp)
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

@Preview
@Composable
internal fun PaymentSuccessSectionPreview() {
    PreviewWrapper {
        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            PaymentSuccessScreen(
                PreviewSamples.Companion.sessionStateSuccess.info!!,
                PreviewSamples.Companion.sessionStateSuccess.paymentSuccess!!
            ) {}
        }
    }
}