package com.monext.sdk.internal.presentation.paymentmethods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monext.sdk.R
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.sessionstate.FormOption
import com.monext.sdk.internal.presentation.common.FormTextField
import com.monext.sdk.internal.presentation.common.SaveCardCheckbox
import com.monext.sdk.internal.util.createPatternFieldAssistant
import kotlinx.coroutines.flow.collectLatest

@Composable
internal fun AlternativePaymentMethodForm(
    paymentMethod: PaymentMethod.AlternativePaymentMethod,
    onFormValidated: (FormData?) -> Unit
) {
    var saveCard by rememberSaveable { mutableStateOf(false) }

    val fieldStates = remember { mutableMapOf<String, androidx.compose.runtime.MutableState<String>>() }
    val assistants = remember { mutableMapOf<String, com.monext.sdk.internal.util.FieldAssistant>() }

    LaunchedEffect(saveCard) {
        onFormValidated(FormData.AlternativePaymentMethodForm(saveCard))
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("AlternativePaymentMethodForm")
    ) {
        if (paymentMethod.data.hasForm == true) {
            val form = paymentMethod.data.form
            if (form?.formType == "CUSTOM") {
                val fields = form.formFields

                if (fields.isNotEmpty()) {
                    for (field in fields) {
                        if (field.formFieldType != "INPUT") continue
                        val key = field.key ?: continue

                        val textState = fieldStates.getOrPut(key) {
                            rememberSaveable(key) { mutableStateOf(field.value.orEmpty()) }
                        }

                        val assistant = assistants.getOrPut(key) {
                            createPatternFieldAssistant(
                                patternString = field.validation?.pattern,
                                required = field.required == true,
                                validationErrorMessage = field.validationErrorMessage,
                                requiredErrorMessage = field.requiredErrorMessage,
                                charLimit = 128,
                            )
                        }

                        FormTextField(
                            text = textState.value,
                            onTextChanged = { new -> textState.value = new },
                            labelText = field.label,
                            placeholder = field.placeholder,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(key)
                            ,
                            assistant = assistant,
                            keyboardOptions = KeyboardOptions.Default,
                            issuer = null
                        )
                    }

                    LaunchedEffect(fields, saveCard) {
                        snapshotFlow {
                            fields.mapNotNull { f ->
                                val k = f.key ?: return@mapNotNull null
                                val value = fieldStates[k]?.value.orEmpty()
                                val assistant = assistants[k] ?: return@mapNotNull null
                                Triple(f, k, Pair(value, assistant))
                            }
                        }.collectLatest { entries ->
                            val firstError = entries.firstNotNullOfOrNull { (_, _, pair) ->
                                val (v, assistant) = pair
                                assistant.validator?.validate(v, null)
                            }

                            if (firstError != null) {
                                onFormValidated(null)
                                return@collectLatest
                            }

                            val paramsMap = mutableMapOf<String, String?>()
                            val securedMap = mutableMapOf<String, String?>()
                            for ((fieldObj, key, pair) in entries) {
                                val value = pair.first
                                if (fieldObj.secured == true) {
                                    securedMap[key] = value.ifEmpty { null }
                                } else {
                                    paramsMap[key] = value.ifEmpty { null }
                                }
                            }

                            onFormValidated(
                                FormData.AlternativePaymentMethodForm(
                                    saveCard = saveCard,
                                    params = paramsMap.toMap(),
                                    securedParams = securedMap.toMap()
                                )
                            )
                        }
                    }
                }
            }
        }

        if (paymentMethod.data.options?.contains(FormOption.SAVE_PAYMENT_DATA) == true) {
            SaveCardCheckbox(stringResource(R.string.payment_method_form_save), saveCard) {
                saveCard = !saveCard
            }
        }
    }
}