package com.monext.sdk.internal.presentation.paymentmethods

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.presentation.card.CardForm

@Composable
internal fun PaymentMethodFormContainer(paymentMethod: PaymentMethod?, onFormValidated: (FormData?) -> Unit, modifier: Modifier = Modifier) {

    val theme = LocalAppearance.current

    Box(modifier.background(theme.backgroundColor).padding(16.dp)) {
        when (paymentMethod) {
            is PaymentMethod.Cards -> CardForm(paymentMethod, onFormValidated)
            is PaymentMethod.AlternativePaymentMethod -> AlternativePaymentMethodForm(paymentMethod, onFormValidated)
            else -> UnimplementedPaymentMethod()
        }
    }
}

@Composable
internal fun UnimplementedPaymentMethod() {
    Text("Selected payment method not implemented.")
}