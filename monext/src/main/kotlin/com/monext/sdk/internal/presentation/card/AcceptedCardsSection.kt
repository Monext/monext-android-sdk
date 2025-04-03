package com.monext.sdk.internal.presentation.card

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.sessionstate.PaymentMethodCardCode
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s14
import com.monext.sdk.internal.presentation.common.PaymentMethodChip

@Composable
internal fun AcceptedCardsSection(cardsPaymentMethods: List<PaymentMethod>) {

    val theme = LocalAppearance.current

    Column(
        Modifier.clip(RoundedCornerShape(theme.cardRadius))
            .background(Color.White).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            stringResource(R.string.accepted_cards_title),
            Modifier.fillMaxWidth(),
            style = theme.baseTextStyle.s14()
                .foreground(Color.Black)
                .copy(textAlign = TextAlign.Center)
        )

        Row(
            Modifier.scrollable(
                state = rememberScrollState(),
                orientation = Orientation.Horizontal
            ),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            for (pm in cardsPaymentMethods) {
                when (pm) {
                    is PaymentMethod.CB -> {
                        PaymentMethodChip(
                            PaymentMethodCardCode.CB,
                            isExpanded = false,
                            showsBack = false
                        )
                        PaymentMethodChip(
                            PaymentMethodCardCode.VISA,
                            isExpanded = false,
                            showsBack = false
                        )
                        PaymentMethodChip(
                            PaymentMethodCardCode.MASTERCARD,
                            isExpanded = false,
                            showsBack = false
                        )
                    }
                    is PaymentMethod.MCVisa -> {
                        PaymentMethodChip(
                            PaymentMethodCardCode.VISA,
                            isExpanded = false,
                            showsBack = false
                        )
                        PaymentMethodChip(
                            PaymentMethodCardCode.MASTERCARD,
                            isExpanded = false,
                            showsBack = false
                        )
                    }
                    else -> PaymentMethodChip(
                        pm.cardCode,
                        isExpanded = false,
                        showsBack = false
                    )
                }
            }
        }
    }
}