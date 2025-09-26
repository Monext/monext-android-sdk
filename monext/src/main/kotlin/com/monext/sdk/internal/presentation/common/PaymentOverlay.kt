package com.monext.sdk.internal.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.PaymentOverlayToggle
import com.monext.sdk.R
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s20

@Composable
internal fun PaymentOverlay(params: PaymentOverlayToggle, modifier: Modifier = Modifier) {

    val theme = LocalAppearance.current
    val cardCode = params.paymentMethodCardCode

    Box(
        modifier
            .background(
                Brush.verticalGradient(
                    0.0f to theme.headerBackgroundColor.copy(alpha = 0f),
                    0.32f to theme.headerBackgroundColor,
                    1.0f to theme.headerBackgroundColor
                )
            )
            .padding(32.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (cardCode != null) {
                PaymentMethodChip(
                    cardCode = cardCode,
                    isExpanded = false,
                    showsBack = false
                )
            }

            CircularProgressIndicator(color = theme.onHeaderBackgroundColor)

            Text(
                stringResource(R.string.payment_overlay_message),
                style = theme.baseTextStyle.s20()
                    .foreground(theme.onHeaderBackgroundColor)
                    .copy(textAlign = TextAlign.Center)
            )
        }
    }
}