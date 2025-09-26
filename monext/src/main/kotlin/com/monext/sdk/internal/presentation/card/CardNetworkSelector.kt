package com.monext.sdk.internal.presentation.card

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.data.CardNetwork
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s14
import com.monext.sdk.internal.presentation.common.NoActionDialog
import com.monext.sdk.internal.presentation.common.PaymentMethodChip

@Composable
internal fun CardNetworkSelector(
    defNet: CardNetwork,
    altNet: CardNetwork,
    selectedCardNetwork: CardNetwork?,
    onSelectedCardNetwork: (CardNetwork) -> Unit
) {

    val theme = LocalAppearance.current
    var showInfoModal by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier
            .border(
                width = theme.textfieldStroke,
                color = theme.textfieldBorderColor,
                shape = RoundedCornerShape(theme.cardRadius)
            )
            .background(
                theme.textfieldBackgroundColor,
                shape = RoundedCornerShape(theme.cardRadius)
            )
            .padding(top = 5.dp, bottom = 16.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.card_network_selector_title),
                style = theme.baseTextStyle.s14()
                    .foreground(theme.onBackgroundColor)
            )

            IconButton(
                { showInfoModal = true },
                colors = IconButtonDefaults.iconButtonColors().copy(
                    contentColor = theme.textfieldAccessoryColor
                )
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
            }
        }

        Row(
            Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Row(
                Modifier.selectable(
                    selected = defNet == selectedCardNetwork,
                    onClick = { onSelectedCardNetwork(defNet) },
                    role = Role.RadioButton
                ),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = defNet == selectedCardNetwork,
                    onClick = null,
                    colors = RadioButtonDefaults.colors().copy(
                        selectedColor = theme.onBackgroundColor,
                        unselectedColor = theme.onBackgroundColor
                    )
                )
                PaymentMethodChip(
                    defNet.network,
                    isExpanded = false,
                    showsBack = false
                )
            }
            Row(
                Modifier.selectable(
                    selected = altNet == selectedCardNetwork,
                    onClick = { onSelectedCardNetwork(altNet) },
                    role = Role.RadioButton
                ),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = altNet == selectedCardNetwork,
                    onClick = null,
                    colors = RadioButtonDefaults.colors().copy(
                        selectedColor = theme.onBackgroundColor,
                        unselectedColor = theme.onBackgroundColor
                    )
                )
                PaymentMethodChip(
                    altNet.network,
                    isExpanded = false,
                    showsBack = false
                )
            }
        }
    }

    if (showInfoModal) {
        NoActionDialog(
            stringResource(R.string.dialog_choose_network_title),
            stringResource(R.string.dialog_choose_network_message)
        ) { showInfoModal = false }
    }
}