package com.monext.sdk.internal.presentation.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.monext.sdk.internal.ext.s18
import com.monext.sdk.internal.ext.s24
import com.monext.sdk.internal.presentation.common.AppButtonSecondaryOutlined
import com.monext.sdk.internal.presentation.common.PopImage
import com.monext.sdk.internal.presentation.common.PopImageStyle
import com.monext.sdk.internal.preview.PreviewWrapper

@Composable
internal fun PaymentFailureScreen(amount: String, onRetry: () -> Unit, onExit: () -> Unit) {

    val theme = LocalAppearance.current

    Column(Modifier.background(theme.backgroundColor)) {

        val foregroundStyle = theme.baseTextStyle.bold().s18()
            .foreground(theme.onBackgroundColor)
            .copy(textAlign = TextAlign.Center)

        Column(
            Modifier
                .weight(1f)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            PopImage(
                style = theme.failureImage?.let { PopImageStyle.Custom(it) } ?: PopImageStyle.Failure
            )

            Text(
                stringResource(R.string.payment_failure_header),
                modifier= Modifier.testTag("failure_header"),
                style = foregroundStyle.s24()
            )

            Text(
                stringResource(R.string.payment_failure_title1, amount),
                style = foregroundStyle
            )

            Text(
                stringResource(R.string.payment_failure_title2),
                style = foregroundStyle
            )
        }

        Column(
            Modifier.background(theme.surfaceColor)
                .padding(16.dp)
                .testTag("back_button"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppButtonSecondaryOutlined(onExit) {
                Text(
                    stringResource(R.string.button_return_to_app_title),
                    style = foregroundStyle.foreground(theme.onSurfaceColor)
                )
            }

            // TODO: Implement retry
//            AppButtonSecondaryFilled(onRetry) {
//                Text(
//                    stringResource(R.string.button_retry_title),
//                    style = theme.baseTextStyle.bold().s18()
//                )
//            }
        }
    }
}

@Preview
@Composable
internal fun FailureScreenPreview() {
    PreviewWrapper {
        PaymentFailureScreen("EUR 123.97", {}) {}
    }
}