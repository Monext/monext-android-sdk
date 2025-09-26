package com.monext.sdk.internal.presentation.paymentmethods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monext.sdk.R
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.sessionstate.FormOption
import com.monext.sdk.internal.presentation.common.SaveCardCheckbox

@Composable
internal fun AlternativePaymentMethodForm(
    paymentMethod: PaymentMethod.AlternativePaymentMethod,
    onFormValidated: (FormData?) -> Unit
) {

    var saveCard by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(saveCard) {
        onFormValidated(FormData.AlternativePaymentMethodForm(saveCard))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("AlternativePaymentMethodForm")

    ) {

        if (paymentMethod.data.options?.contains(FormOption.SAVE_PAYMENT_DATA) == true) {
            SaveCardCheckbox(stringResource(R.string.payment_method_form_save), saveCard) {
                saveCard = !saveCard
            }
        }
    }
}