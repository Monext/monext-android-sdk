package com.monext.sdk.internal.presentation.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.presentation.paymentmethods.PaymentMethodFormContainer
import com.monext.sdk.internal.preview.PreviewSamples
import com.monext.sdk.internal.preview.PreviewWrapper

@Composable
internal fun CardForm(cards: PaymentMethod.Cards, onFormValidated: (FormData?) -> Unit) {

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        AcceptedCardsSection(cards.paymentMethods)

        FormFieldsSection(cards, onFormValidated)

        CompliancyNoticeSection()
    }
}

@Preview
@Composable
internal fun CardFormPreview() {
    PreviewWrapper {
        PaymentMethodFormContainer(
            PreviewSamples.paymentMethodsList.paymentMethods[0],
            onFormValidated = {}
        )
    }
}