package com.monext.sdk.internal.presentation.status

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.monext.sdk.LocalAppearance
import com.monext.sdk.internal.data.sessionstate.Ticket
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s12

@Composable
internal fun TicketItem(ticket: Ticket) {

    val theme = LocalAppearance.current

    Row {

        Text(
            ticket.key ?: "",
            style = theme.baseTextStyle.s12()
                .foreground(theme.onBackgroundColor)
        )

        Spacer(Modifier.Companion.weight(1f))

        Text(
            ticket.value,
            style = theme.baseTextStyle.s12()
                .foreground(theme.onBackgroundColor)
        )
    }
}