package com.monext.sdk.internal.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.data.sessionstate.PaymentMethodCardCode
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s16
import com.monext.sdk.internal.preview.PreviewSamples
import com.monext.sdk.internal.preview.PreviewWrapper

@Composable
internal fun PaymentMethodChip(cardCode: PaymentMethodCardCode?, isExpanded: Boolean, showsBack: Boolean, modifier: Modifier = Modifier) {

    var containerMod = modifier.height(48.dp)
    if (cardCode != null && !isExpanded) {
        containerMod = containerMod.width(70.dp)
    }

    Box(
        containerMod.height(48.dp)
            .clip(RoundedCornerShape(size = 10.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {

        val sMod = if (isExpanded) {
            Modifier.fillMaxWidth()
        } else {
            Modifier
        }

        Box(sMod, contentAlignment = Alignment.Center) {
            PaymentMethodImage(cardCode)
        }

        if (showsBack) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.Black
            )
        }
    }
}

@Preview
@Composable
internal fun PaymentMethodChipPreview() {
    PreviewWrapper {
        Column {
            PaymentMethodChip(
                PreviewSamples.paymentMethodsList.paymentMethods.first().cardCode,
                isExpanded = false,
                showsBack = false
            )

            PaymentMethodChip(
                PreviewSamples.paymentMethodsList.paymentMethods.first().cardCode,
                isExpanded = true,
                showsBack = false
            )

            PaymentMethodChip(
                PreviewSamples.paymentMethodsList.paymentMethods.first().cardCode,
                isExpanded = true,
                showsBack = true
            )
        }
    }
}

@Composable
internal fun PaymentMethodImage(cardCode: PaymentMethodCardCode?) {
    when (cardCode) {
        null -> Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(painterResource(R.drawable.logo_cards), contentDescription = null)
            Text(
                stringResource(R.string.misc_payment_method_card),
                style = LocalAppearance.current.baseTextStyle.s16()
                    .foreground(Color.Black)
            )
        }
        PaymentMethodCardCode.CB -> Image(
            painterResource(R.drawable.logo_cb),
            contentDescription = null
        )
        PaymentMethodCardCode.VISA -> Image(
            painterResource(R.drawable.logo_visa),
            contentDescription = null
        )
        PaymentMethodCardCode.MASTERCARD -> Image(
            painterResource(R.drawable.logo_mastercard),
            contentDescription = null
        )
        PaymentMethodCardCode.AMEX -> Image(
            painterResource(R.drawable.logo_amex),
            contentDescription = null
        )
        PaymentMethodCardCode.PAYPAL -> Image(
            painterResource(R.drawable.logo_paypal),
            contentDescription = null
        )
        PaymentMethodCardCode.IDEAL -> Image(
            painterResource(R.drawable.logo_ideal),
            contentDescription = null
        )

        PaymentMethodCardCode.MCVISA -> {}
        PaymentMethodCardCode.GOOGLE_PAY -> {}
        PaymentMethodCardCode.UNSUPPORTED -> {}
    }
}