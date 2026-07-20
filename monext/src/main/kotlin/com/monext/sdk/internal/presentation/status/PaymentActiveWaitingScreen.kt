package com.monext.sdk.internal.presentation.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.data.LocalSessionStateRepo
import com.monext.sdk.internal.data.sessionstate.ActiveWaiting
import com.monext.sdk.internal.ext.bold
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s24
import com.monext.sdk.internal.presentation.common.HtmlWebView

@Composable
internal fun ActiveWaitingScreen(activeWaiting: ActiveWaiting) {

    val theme = LocalAppearance.current
    val sessionStore = LocalSessionStateRepo.current

    Column {

        Column(
            Modifier
                .background(theme.headerBackgroundColor)
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                Modifier
                    .padding(16.dp)
                    .testTag("active_waiting_loader"),
                color = LocalAppearance.current.onHeaderBackgroundColor
            )

            Text(
                stringResource(R.string.active_waiting_title),
                style = theme.baseTextStyle.bold().s24()
                    .foreground(theme.onBackgroundColor),
                modifier = Modifier.testTag("active_waiting_header")
            )

            if (!activeWaiting.message?.localizedMessage.isNullOrBlank()) {
                HtmlWebView(
                    activeWaiting.message.localizedMessage,
                    transparent = true,
                    fontSizePx = 16,
                    testTag = "active_waiting_message"
                )
            }
        }
    }

    LaunchedEffect(activeWaiting) {
        try {
            sessionStore.isDone()
        } catch (e: Throwable) {
            throw e
        }
    }
}