package com.monext.sdk.internal.presentation.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monext.sdk.Appearance
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.Issuer
import com.monext.sdk.internal.data.sessionstate.FormOption
import com.monext.sdk.internal.data.sessionstate.PaymentMethodCardCode
import com.monext.sdk.internal.data.sessionstate.Wallet
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s14
import com.monext.sdk.internal.presentation.common.FormTextField
import com.monext.sdk.internal.presentation.common.PaymentMethodChip
import com.monext.sdk.internal.preview.PreviewSamples
import com.monext.sdk.internal.preview.PreviewWrapper
import com.monext.sdk.internal.util.CvvAssistant
import java.time.LocalDate
import java.time.YearMonth

@Composable
internal fun WalletItem(
    wallet: Wallet,
    isSelected: Boolean,
    onSelectWallet: (Wallet) -> Unit,
    onWalletFormDataValid: (FormData.Wallet?) -> Unit
) {

    val theme = LocalAppearance.current
    var cvvText by rememberSaveable { mutableStateOf("") }
    var issuer = Issuer.lookupIssuer(wallet)

    LaunchedEffect(isSelected, cvvText) {
        if (isSelected) {
            if (FormOption.CVV !in wallet.confirm) {
                onWalletFormDataValid(FormData.Wallet())
            } else {
                if (CvvAssistant.validator.validate(cvvText, issuer) == null) {
                    onWalletFormDataValid(FormData.Wallet(cvvText))
                } else {
                    onWalletFormDataValid(null)
                }
            }
        }
    }

    Column(
        Modifier
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) theme.primaryColor else theme.onSurfaceAlpha,
                shape = RoundedCornerShape(theme.cardRadius)
            )
            .clip(RoundedCornerShape(theme.cardRadius))
            .background(theme.surfaceColor)
            .background(if (isSelected) theme.primaryAlpha else Color.Transparent)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        WalletRow(wallet, isSelected, onSelectWallet)

        val focusManager = LocalFocusManager.current
        if (FormOption.CVV in wallet.confirm && isSelected) {
            FormTextField(
                cvvText, { cvvText = it },
                labelText = stringResource(R.string.payment_card_form_field_cvv),
                Modifier.fillMaxWidth(),
                useOnSurfaceStyle = true,
                assistant = CvvAssistant,
                issuer = issuer,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions {
                    focusManager.clearFocus()
                }
            )
        }
    }
}

@Composable
internal fun WalletRow(wallet: Wallet, isSelected: Boolean, onSelectWallet: (Wallet) -> Unit) {

    val theme = LocalAppearance.current

    val topText = wallet.additionalData.holder?.let {
        if (it.isEmpty()) null else it
    } ?: wallet.additionalData.email

    val maskedNumber: String? = wallet.additionalData.pan?.replace(Regex("""\D"""), "\u2022")

    val cardDate by remember { derivedStateOf<String?> {
        val rawDateStr = wallet.additionalData.date ?: return@derivedStateOf null
        val date = YearMonth.parse(rawDateStr, Appearance.cardNetworkFormat) ?: return@derivedStateOf null
        Appearance.cardPresentationFormat.format(date)
    } }

    Row(
        Modifier.clickable { onSelectWallet(wallet) },
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val code = if (wallet.cardCode in listOf(
                PaymentMethodCardCode.CB,
                PaymentMethodCardCode.MCVISA
            )
        ) {
            wallet.cardType
        } else {
            wallet.cardCode
        }

        PaymentMethodChip(
            code,
            isExpanded = false,
            showsBack = false
        )

        Column(Modifier.weight(1f)) {

            topText?.let {
                Text(
                    it,
                    style = theme.baseTextStyle.s14().foreground(theme.onSurfaceColor)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                maskedNumber?.let {
                    Text(
                        it,
                        style = theme.baseTextStyle.s14()
                            .foreground(theme.onSurfaceCardNumber)
                    )
                }

                Spacer(Modifier.weight(1f))

                cardDate?.let {
                    Text(
                        stringResource(R.string.wallet_item_expiration, it),
                        style = theme.baseTextStyle.s14().foreground(theme.onSurfaceColor)
                    )
                }
            }
        }

        Icon(
            if (isSelected) {
                painterResource(R.drawable.ic_radio_selected)
            } else {
                painterResource(R.drawable.ic_radio_unselected)
            },
            contentDescription = null,
            tint = theme.primaryColor
        )
    }
}

@Preview
@Composable
internal fun WalletItemPreview() {
    PreviewWrapper {
        for ((idx, wallet) in PreviewSamples.wallets.withIndex()) {
            WalletItem(wallet, idx == 1, {}) {}
            Spacer(Modifier.height(20.dp))
        }
    }
}