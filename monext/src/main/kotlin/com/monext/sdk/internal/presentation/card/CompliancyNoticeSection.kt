package com.monext.sdk.internal.presentation.card

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s12

@Composable
internal fun CompliancyNoticeSection() {

    val theme = LocalAppearance.current

    Row(
        Modifier.Companion
            .border(
                width = 2.dp,
                color = theme.confirmationAlpha,
                shape = RoundedCornerShape(theme.cardRadius)
            )
            .background(
                theme.backgroundColor,
                androidx.compose.foundation.shape.RoundedCornerShape(theme.cardRadius)
            )
            .background(
                theme.confirmationAlpha,
                androidx.compose.foundation.shape.RoundedCornerShape(theme.cardRadius)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {

        Box(Modifier.Companion
            .size(48.dp)
            .padding(horizontal = 12.dp, vertical = 10.dp)) {
            Image(
                painterResource(R.drawable.ic_shield_filled), contentDescription = null,
                Modifier.Companion.fillMaxSize(),
                colorFilter = ColorFilter.Companion.tint(theme.confirmationColor)
            )

            Image(
                painterResource(R.drawable.ic_check_small), contentDescription = null,
                Modifier.Companion.fillMaxSize(),
                colorFilter = ColorFilter.Companion.tint(theme.onConfirmationColor)
            )
        }

        Text(
            stringResource(R.string.compliancy_notice_message),
            Modifier.Companion.weight(1f),
            style = theme.baseTextStyle.s12()
                .foreground(theme.onConfirmationColor)
        )
    }
}