package com.monext.sdk.internal.presentation.paymentmethods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.sessionstate.FormOption
import com.monext.sdk.internal.ext.bold
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s16
import com.monext.sdk.internal.presentation.common.SaveCardCheckbox
import com.monext.sdk.internal.preview.PreviewSamples
import com.monext.sdk.internal.preview.PreviewWrapper

@Composable
internal fun PayPalForm(
    paypal: PaymentMethod.PayPal,
    onFormValidated: (FormData?) -> Unit
) {

    val theme = LocalAppearance.current

    var saveCard by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(saveCard) {
        onFormValidated(FormData.PayPal(saveCard))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (paypal.data.options?.contains(FormOption.SAVE_PAYMENT_DATA) == true) {
            SaveCardCheckbox(stringResource(R.string.payment_method_form_save), saveCard) {
                saveCard = !saveCard
            }
        }

        Text(
            stringResource(R.string.payment_method_paypal_message),
            style = theme.baseTextStyle.bold().s16()
                .foreground(theme.onBackgroundColor)
                .copy(textAlign = TextAlign.Center)
        )
    }
}

@Preview
@Composable
internal fun PayPalFormPreview() {
    PreviewWrapper {
        PaymentMethodFormContainer(
            PreviewSamples.paymentMethodsList.paymentMethods[1],
            onFormValidated = {}
        )
    }
}