package com.monext.sdk.internal.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.data.Issuer
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s12
import com.monext.sdk.internal.preview.PreviewWrapper
import com.monext.sdk.internal.util.CvvAssistant
import com.monext.sdk.internal.util.FieldAssistant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FormTextField(
    text: String,
    onTextChanged: (String) -> Unit,
    labelText: String,
    modifier: Modifier = Modifier,
    useOnSurfaceStyle: Boolean = false,
    assistant: FieldAssistant,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    showsAccessory: Boolean = false,
    issuer: Issuer?
) {

    val theme = LocalAppearance.current
    var showInfoDialog by remember { mutableStateOf(false) }

    var isFocused by remember { mutableStateOf(false) }

    var errorMessage: String? by remember { mutableStateOf(null) }
    val isError = !errorMessage.isNullOrBlank()

    val interactionSource = remember { MutableInteractionSource() }

    val colors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = if (useOnSurfaceStyle) theme.textfieldTextOnSurfaceColor else theme.textfieldTextColor,
        unfocusedTextColor = if (useOnSurfaceStyle) theme.textfieldTextOnSurfaceColor else theme.textfieldTextColor,
        errorTextColor = if (useOnSurfaceStyle) theme.textfieldTextOnSurfaceColor else theme.textfieldTextColor,

        focusedContainerColor = if (useOnSurfaceStyle) theme.textfieldBackgroundOnSurfaceColor else theme.textfieldBackgroundColor,
        unfocusedContainerColor = if (useOnSurfaceStyle) theme.textfieldBackgroundOnSurfaceColor else theme.textfieldBackgroundColor,
        errorContainerColor = if (useOnSurfaceStyle) theme.textfieldBackgroundOnSurfaceColor else theme.textfieldBackgroundColor,

        focusedBorderColor = if (useOnSurfaceStyle) theme.textfieldBorderSelectedOnSurfaceColor else theme.textfieldBorderSelectedColor,
        unfocusedBorderColor = if (useOnSurfaceStyle) theme.textfieldBorderOnSurfaceColor else theme.textfieldBorderColor,
        errorBorderColor = theme.errorColor,

        focusedLabelColor = if (useOnSurfaceStyle) theme.textfieldBorderSelectedOnSurfaceColor else theme.textfieldBorderSelectedColor,
        unfocusedLabelColor = if (useOnSurfaceStyle) theme.textfieldLabelOnSurfaceColor else theme.textfieldLabelColor,
        errorLabelColor = theme.errorColor,
    )

//    val keyboard = LocalSoftwareKeyboardController.current

    val visualTransformation = VisualTransformation {
        TransformedText(
            AnnotatedString(assistant.formatter.format(it.toString())),
            assistant.offsetMapping
        )
    }

    val context = LocalContext.current

    val formTextStyle = theme.baseTextStyle.copy(letterSpacing = 3.sp)
        .foreground(colors.focusedTextColor)
    val formTextSize = 20.sp
//    var resizedTextSize by remember { mutableStateOf(formTextSize) }
//    var extraPadding by remember { mutableFloatStateOf(0f) }

//    BoxWithConstraints {

//        val outer = this
//        val maxInputWidth = outer.maxWidth - 32.dp
//        val maxInputWidthPx = with(LocalDensity.current) { maxInputWidth.toPx() }

        BasicTextField(
            value = text,
            onValueChange = { text ->
                val sanitized = assistant.sanitizer.sanitize(text)
                val limited = assistant.charLimit?.let {
                    sanitized.take(it)
                } ?: sanitized
                onTextChanged(limited)
            },
            modifier = modifier
                .onFocusChanged {
                    isFocused = it.isFocused
                    errorMessage = if (!isFocused && text.isNotBlank()) {
                        assistant.validator?.validate(text, issuer)?.errorMessage(context)
                    } else {
                        null
                    }
                },
            textStyle = formTextStyle.copy(fontSize = formTextSize),//.copy(fontSize = resizedTextSize),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
//            keyboardActions = KeyboardActions(onAny = {
//                keyboard?.hide()
//            }),
            singleLine = true,
            visualTransformation = visualTransformation,
//            onTextLayout = { textLayoutResult ->
//                if (textLayoutResult.size.width > (maxInputWidthPx - extraPadding)) {
//                    resizedTextSize *= 0.9
//                }
//            },
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = text,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    isError = isError,
                    label = {
                        Text(labelText, style = theme.baseTextStyle.s12())
                    },
                    trailingIcon = if (showsAccessory) {
                        {
//                            BoxWithConstraints {
//                                val inner = this
//                                val paddingPx = with(LocalDensity.current) { inner.maxWidth.toPx() }
//                                extraPadding = paddingPx
                                IconButton({ showInfoDialog = true }) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (useOnSurfaceStyle) theme.textfieldAccessoryOnSurfaceColor else theme.textfieldAccessoryColor
                                    )
                                }
//                            }
                        }
                    } else {
//                        extraPadding = 0f
                        null
                    },
                    supportingText = {
                        errorMessage?.let {
                            Text(it, style = theme.baseTextStyle.s12())
                        }
                    },
                    colors = colors,
                    container = {
                        OutlinedTextFieldDefaults.Container(
                            enabled = true,
                            isError = isError,
                            interactionSource = interactionSource,
                            colors = colors,
                            shape = RoundedCornerShape(theme.cardRadius),
                            focusedBorderThickness = 2.dp,
                            unfocusedBorderThickness = 1.dp
                        )
                    }
                )
            }
        )
//    }

    if (showInfoDialog) {
        NoActionDialog(
            title = stringResource(R.string.dialog_cvv_title),
            message = stringResource(R.string.dialog_cvv_message)
        ) {
            showInfoDialog = false
        }
    }
}

@Preview
@Composable
internal fun FormTextFieldPreview() {

    PreviewWrapper {

        val (text, txtChanged) = remember { mutableStateOf("") }

        Column(
            Modifier.background(LocalAppearance.current.backgroundColor)
        ) {

            FormTextField(
                text, txtChanged,
                labelText = "CVV",
                useOnSurfaceStyle = false,
                assistant = CvvAssistant,
                issuer = null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            FormTextField(
                text, txtChanged,
                labelText = "CVV",
                useOnSurfaceStyle = false,
                assistant = CvvAssistant,
                issuer = null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            FormTextField(
                text, txtChanged,
                labelText = "CVV",
                useOnSurfaceStyle = true,
                assistant = CvvAssistant,
                showsAccessory = true,
                issuer = null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )

            FormTextField(
                text, txtChanged,
                labelText = "CVV",
                useOnSurfaceStyle = true,
                assistant = CvvAssistant,
                issuer = null,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                )
            )
        }
    }
}