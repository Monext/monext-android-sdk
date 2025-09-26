package com.monext.sdk.internal.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.pay.button.PayButton
import com.monext.sdk.GooglePayConfiguration
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.ext.bold
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s18

@Composable
internal fun PayButtonsContainer(
    amount: String,
    selectedPaymentMethod: PaymentMethod?,
    canPay: Boolean,
    isLoading: Boolean,
    showsGooglePay: Boolean,
    gPayConfig: GooglePayConfiguration,
    allowedPaymentMethods: String,
    onClickGooglePay: () -> Unit,
    onClick: () -> Unit
) {

    val theme = LocalAppearance.current

    Column(
        Modifier
            .background(theme.surfaceColor)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        PaymentButton(amount, selectedPaymentMethod, canPay, isLoading, onClick)

        if (showsGooglePay) {
            PayButton(
                onClick = onClickGooglePay,
                allowedPaymentMethods = allowedPaymentMethods,
                modifier = Modifier.testTag("payButton")
                    .fillMaxWidth(),
                theme = gPayConfig.theme,
                type = gPayConfig.type,
                radius = LocalAppearance.current.buttonRadius
            )
        }
    }
}

@Composable
internal fun PaymentButton(amount: String, selectedPaymentMethod: PaymentMethod?, canPay: Boolean, isLoading: Boolean, onClick: () -> Unit) {

    val theme = LocalAppearance.current
    val text = selectedPaymentMethod?.data?.form?.buttonText
        ?: stringResource(R.string.button_pay_title, amount)

    Surface(
        onClick,
        Modifier
            .height(48.dp)
            .testTag("PaymentButton"),
        enabled = canPay,
        shape = RoundedCornerShape(theme.buttonRadius),
        color = if (canPay) theme.secondaryColor else theme.secondaryColor.copy(alpha = 0.3f)
    ) {

        Box(contentAlignment = Alignment.Center) {

            if (isLoading) {

                CircularProgressIndicator(
                    Modifier
                        .size(24.dp)
                        .testTag("PaymentButtonLoader"),
                    color = theme.onSecondaryColor,
                    strokeWidth = 2.dp
                )

            } else {
                Text(
                    text,
                    style = theme.baseTextStyle.bold().s18().foreground(theme.onSecondaryColor),
                    modifier = Modifier
                        .testTag("PaymentButtonText")
                )
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = theme.onSecondaryColor
                )
            }
        }
    }
}