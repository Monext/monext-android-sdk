package com.monext.sdk.internal.presentation.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.data.sessionstate.Wallet
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s11
import com.monext.sdk.internal.ext.s14
import com.monext.sdk.internal.preview.PreviewSamples
import com.monext.sdk.internal.preview.PreviewWrapper

@Composable
internal fun WalletsView(wallets: List<Wallet>, items: @Composable ColumnScope.(List<Wallet>) -> Unit) {

    val theme = LocalAppearance.current

    Column(
        Modifier.Companion
            .background(theme.surfaceColor)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp),
    ) {

        Row(verticalAlignment = Alignment.Companion.Bottom) {

            Text(
                stringResource(R.string.wallet_list_header),
                Modifier.Companion.padding(bottom = 4.dp),
                style = theme.baseTextStyle.s14()
                    .foreground(theme.onSurfaceColor)
            )

            Spacer(Modifier.Companion.weight(1f))

            if (wallets.firstOrNull()?.isDefault == true) {
                Text(
                    stringResource(R.string.wallet_list_default_label),
                    Modifier.Companion
                        .padding(end = 16.dp)
                        .clip(
                            RoundedCornerShape(
                                topStart = theme.paymentMethodRadius,
                                topEnd = theme.paymentMethodRadius
                            )
                        )
                        .background(theme.primaryColor)
                        .padding(horizontal = 8.dp)
                        .padding(top = 4.dp),
                    style = theme.baseTextStyle.s11().foreground(theme.onPrimaryColor)
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(wallets)
        }
    }
}

@Preview
@Composable
internal fun WalletsViewPreview() {
    PreviewWrapper {
        val wallets = PreviewSamples.wallets
        WalletsView(PreviewSamples.wallets) {
            for (wallet in wallets) {
                WalletItem(wallet, false, {}) {}
            }
        }
    }
}