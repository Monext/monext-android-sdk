package com.monext.sdk.internal.presentation.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.monext.sdk.R
import com.monext.sdk.internal.api.AvailableCardNetworksRequest
import com.monext.sdk.internal.api.AvailableCardNetworksResponse
import com.monext.sdk.internal.api.HandledContract
import com.monext.sdk.internal.data.CardNetwork
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.Issuer
import com.monext.sdk.internal.data.LocalSessionStateRepo
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.sessionstate.FormOption
import com.monext.sdk.internal.presentation.common.FormTextField
import com.monext.sdk.internal.presentation.common.SaveCardCheckbox
import com.monext.sdk.internal.util.CreditCardAssistant
import com.monext.sdk.internal.util.CvvAssistant
import com.monext.sdk.internal.util.ExpirationDateAssistant
import com.monext.sdk.internal.util.HolderAssistant

@Composable
internal fun FormFieldsSection(cards: PaymentMethod.Cards, onFormValidated: (FormData?) -> Unit) {

    var cardNum by rememberSaveable { mutableStateOf("") }
    val cardLookupNum by remember { derivedStateOf<String?> {
        if (cardNum.length < 6) null
        else cardNum.take(10)
    } }
    var expDate by rememberSaveable { mutableStateOf("") }
    var cvvNum by rememberSaveable { mutableStateOf("") }
    var holder by rememberSaveable { mutableStateOf("") }
    var availableNetworks by remember { mutableStateOf<AvailableCardNetworksResponse?>(null) }
    var selectedNetwork by remember { mutableStateOf<CardNetwork?>(null) }
    var saveCard by rememberSaveable { mutableStateOf(false) }

    val cvvFocus = remember { FocusRequester() }

    val issuer by remember { derivedStateOf {
        Issuer.lookupIssuer(cardNum)
    } }

    val detectedPaymentMethod by remember { derivedStateOf {
        issuer?.paymentMethod(cards.paymentMethods)
    } }

    val showExpiration by remember { derivedStateOf {
        detectedPaymentMethod == null || detectedPaymentMethod?.data?.options?.contains(FormOption.EXPI_DATE) == true
    } }

    val showCvv by remember { derivedStateOf {
        detectedPaymentMethod == null || detectedPaymentMethod?.data?.options?.contains(FormOption.CVV) == true
    } }

    val showHolder by remember { derivedStateOf {
        detectedPaymentMethod == null || detectedPaymentMethod?.data?.options?.contains(FormOption.HOLDER) == true
    } }

    val canSaveCard = cards.paymentMethods
        .mapNotNull { it.data?.options?.contains(FormOption.SAVE_PAYMENT_DATA) }
        .reduce { acc, next -> acc || next }

    val showSaveCard by remember { derivedStateOf {
        canSaveCard && detectedPaymentMethod?.data?.options?.contains(FormOption.SAVE_PAYMENT_DATA) == true
    } }

    val repo = LocalSessionStateRepo.current
    LaunchedEffect(cardLookupNum) {
        val cardLookupNum = cardLookupNum
        if (cardLookupNum != null) {
            availableNetworks = repo.availableCardNetworks(
                AvailableCardNetworksRequest(
                    cardLookupNum,
                    cards.paymentMethods.mapNotNull { paymentMethod ->
                        paymentMethod.cardCode?.let { cardCode ->
                            paymentMethod.data?.contractNumber?.let { contractNumber ->
                                HandledContract(cardCode.name, contractNumber)
                            }
                        }
                    }
                )
            )
        } else {
            availableNetworks = null
        }
    }

    LaunchedEffect(availableNetworks) {
        selectedNetwork = availableNetworks?.defaultCardNetwork
    }

    LaunchedEffect(expDate) {
        val dateValid = ExpirationDateAssistant.validator.validate(expDate, issuer)
        if (dateValid == null) {
            cvvFocus.requestFocus()
        }
    }

    LaunchedEffect(cardNum, detectedPaymentMethod, expDate, cvvNum, holder, saveCard, selectedNetwork) {

        val paymentMethod = detectedPaymentMethod
        if (paymentMethod == null) {
            onFormValidated(null)
            return@LaunchedEffect
        }

        var formValidation = CreditCardAssistant.validator.validate(cardNum, issuer)
            ?: (if (showExpiration) ExpirationDateAssistant.validator.validate(expDate, issuer) else null)
            ?: (if (showCvv) CvvAssistant.validator.validate(cvvNum, issuer) else null)
            ?: (if (showHolder) HolderAssistant.validator.validate(holder, issuer) else null)

        if (formValidation != null) {
            onFormValidated(null)
        } else {
            onFormValidated(
                FormData.Card(
                    paymentMethod = paymentMethod,
                    cardNum = cardNum,
                    expDate = expDate,
                    cvvNum = cvvNum,
                    holder = holder,
                    cardNetwork = selectedNetwork,
                    saveCard = saveCard
                )
            )
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        FormTextField(
            text = cardNum,
            onTextChanged = { cardNum = it },
            labelText = stringResource(R.string.payment_card_form_field_card),
            Modifier.fillMaxWidth(),
            assistant = CreditCardAssistant,
            issuer = issuer,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        )

        if (showExpiration || showCvv) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                if (showExpiration) {
                    FormTextField(
                        text = expDate,
                        onTextChanged = { expDate = it },
                        labelText = stringResource(R.string.payment_card_form_field_expiration),
                        Modifier.weight(1f),
                        assistant = ExpirationDateAssistant,
                        issuer = issuer,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                if (showCvv) {
                    FormTextField(
                        text = cvvNum,
                        onTextChanged = { cvvNum = it },
                        labelText = stringResource(R.string.payment_card_form_field_cvv),
                        Modifier.weight(1f).focusRequester(cvvFocus),
                        assistant = CvvAssistant,
                        showsAccessory = true,
                        issuer = issuer,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.None,
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Number,
                            imeAction = if (showHolder) ImeAction.Next else ImeAction.Done
                        )
                    )
                }
            }
        }

        if (showHolder) {
            FormTextField(
                text = holder,
                onTextChanged = { holder = it },
                labelText = stringResource(R.string.payment_card_form_field_holder),
                Modifier.fillMaxWidth(),
                assistant = HolderAssistant,
                issuer = issuer,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                )
            )
        }

        availableNetworks?.let { networks ->
            networks.defaultCardNetwork?.let { defNet ->
                networks.altCardNetwork?.let { altNet ->
                    CardNetworkSelector(defNet, altNet, selectedNetwork) {
                        selectedNetwork = it
                    }
                }
            }
        }

        if (showSaveCard) {
            SaveCardCheckbox(stringResource(R.string.payment_card_form_save), saveCard) {
                saveCard = it
            }
        }
    }
}