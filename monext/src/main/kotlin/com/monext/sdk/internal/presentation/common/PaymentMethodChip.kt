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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.api.model.PaymentMethodCardCode
import com.monext.sdk.internal.data.sessionstate.PaymentMethodData
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s16
import com.monext.sdk.internal.preview.PreviewSamples
import com.monext.sdk.internal.preview.PreviewWrapper

@Composable
internal fun PaymentMethodChip(cardCode: String?, paymentMethodData: PaymentMethodData? = null, isExpanded: Boolean, showsBack: Boolean, modifier: Modifier = Modifier) {

    var containerMod = modifier.height(48.dp)

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
            PaymentMethodImage(
                cardCode,
                paymentMethodData
            )
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
fun PaymentMethodImageWithFallback(logoUrl: String?, fallbackText: String) {
    var hasError by remember { mutableStateOf(true) }

    if (logoUrl != null && !hasError) {
//        TODO: Ajouter la gestion d'image
    } else {
        Text(fallbackText)
    }
}

@Composable
internal fun PaymentMethodImage(cardCode: String?, paymentMethodData: PaymentMethodData?) {
    when {
        paymentMethodData?.logo?.url != null -> {
            PaymentMethodImageWithFallback(
                paymentMethodData.logo.url,
                paymentMethodData.logo.title ?: ""
            )
        }
        cardCode == null -> Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painterResource(R.drawable.logo_cards),
                contentDescription = null,
                modifier = Modifier.testTag("CardLogo")
            )
            Text(
                stringResource(R.string.misc_payment_method_card),
                style = LocalAppearance.current.baseTextStyle.s16()
                    .foreground(Color.Black)
            )
        }
        cardCode == PaymentMethodCardCode.CB -> Image(
            painterResource(R.drawable.logo_cb),
            contentDescription = null,
            modifier = Modifier.testTag("CBLogo")
        )
        cardCode == PaymentMethodCardCode.VISA -> Image(
            painterResource(R.drawable.logo_visa),
            contentDescription = null,
            modifier = Modifier.testTag("VisaLogo")
        )
        cardCode == PaymentMethodCardCode.MASTERCARD -> Image(
            painterResource(R.drawable.logo_mastercard),
            contentDescription = null,
            modifier = Modifier.testTag("MastercardLogo")
        )
        cardCode == PaymentMethodCardCode.AMEX -> Image(
            painterResource(R.drawable.logo_amex),
            contentDescription = null,
            modifier = Modifier.testTag("AmexLogo")
        )
        cardCode == PaymentMethodCardCode.MCVISA || cardCode == PaymentMethodCardCode.GOOGLE_PAY -> {}
    }
}