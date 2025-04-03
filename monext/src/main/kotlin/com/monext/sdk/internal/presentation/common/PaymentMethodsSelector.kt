package com.monext.sdk.internal.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s14
import com.monext.sdk.internal.preview.PreviewSamples
import com.monext.sdk.internal.preview.PreviewWrapper

@Composable
internal fun PaymentMethodsSelector(
    paymentMethods: List<PaymentMethod>,
    selectedPaymentMethod: PaymentMethod?,
    onSelect: (PaymentMethod) -> Unit
) {

    val theme = LocalAppearance.current

    Column(
        Modifier
            .background(theme.headerBackgroundColor)
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        if (selectedPaymentMethod == null) {
            Text(
                stringResource(R.string.payment_methods_selector_header),
                style = theme.baseTextStyle.s14()
                    .foreground(theme.onHeaderBackgroundColor)
            )
        }

        if (selectedPaymentMethod != null) {

            PaymentMethodChip(
                selectedPaymentMethod.cardCode,
                isExpanded = true,
                showsBack = paymentMethods.size > 1,
                Modifier
                    .border(
                        width = 1.dp,
                        color = theme.onHeaderBackgroundAlpha,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .clickable {
                        onSelect(selectedPaymentMethod)
                    }
            )

        } else {

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(paymentMethods) {
                    PaymentMethodChip(
                        it.cardCode,
                        isExpanded = false,
                        showsBack = false,
                        Modifier
                            .border(
                                width = 1.dp,
                                color = theme.onHeaderBackgroundAlpha,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelect(it) }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
internal fun PaymentMethodsSelectorPreview() {
    PreviewWrapper {

        val (selectedPaymentMethod, setPm) = remember { mutableStateOf<PaymentMethod?>(null) }

        PaymentMethodsSelector(
            PreviewSamples.paymentMethodsList.paymentMethods,
            selectedPaymentMethod,
            setPm
        )
    }
}