package com.monext.sdk.internal.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monext.sdk.LocalAppearance
import com.monext.sdk.LocalEnvironment
import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.R
import com.monext.sdk.internal.data.LocalSessionStateRepo
import com.monext.sdk.internal.api.model.response.SessionStateType
import com.monext.sdk.internal.ext.bold
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s12
import com.monext.sdk.internal.ext.s18
import com.monext.sdk.internal.preview.PreviewWrapper

@Composable
internal fun HeaderSection(onClose: () -> Unit) {

    val theme = LocalAppearance.current
    val sessionState by LocalSessionStateRepo.current.sessionState.collectAsStateWithLifecycle()

    var showingDialog by rememberSaveable { mutableStateOf(false) }

    Box(
        Modifier
            .background(theme.headerBackgroundColor)
            .height(64.dp),
        contentAlignment = Alignment.TopEnd
    ) {

        when (sessionState?.type) {
            SessionStateType.PAYMENT_METHODS_LIST, SessionStateType.PAYMENT_REDIRECT_NO_RESPONSE -> {
                Box(
                    Modifier.padding(start = 15.dp).fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    HeaderActionView {
                        showingDialog = true
                    }
                }
            }
            else -> {}
        }

        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            HeaderTitleView()
        }

        if (LocalEnvironment.current is MnxtEnvironment.Sandbox || LocalEnvironment.current is MnxtEnvironment.Custom) {
            TestBanner()
        }
    }
    
    if (showingDialog) {
        ExitPaymentDialog({
            showingDialog = false
            onClose()
        }) {
            showingDialog = false
        }
    }
}

@Preview
@Composable
internal fun HeaderSectionPreview() {
    PreviewWrapper {
        HeaderSection {}
    }
}

@Composable
internal fun HeaderActionView(onAction: () -> Unit) {
    IconButton(onAction) {
        Icon(
            painterResource(R.drawable.ic_x),
            contentDescription = null,
            tint = LocalAppearance.current.onHeaderBackgroundColor
        )
    }
}

@Composable
internal fun HeaderTitleView() {

    val theme = LocalAppearance.current

    theme.headerImage?.let {
        Image(
            it,
            contentDescription = null,
            colorFilter = ColorFilter.tint(theme.onHeaderBackgroundColor)
        )
    } ?: theme.headerTitle?.let {
        Text(
            it,
            style = theme.baseTextStyle.bold().s18()
                .foreground(theme.onHeaderBackgroundColor)
        )
    }
}

@Composable
internal fun TestBanner() {
    Text(
        stringResource(R.string.misc_test_mode),
        Modifier
            .clip(RoundedCornerShape(bottomStart = 26.dp))
            .background(Color.Red)
            .padding(8.dp),
        style = LocalAppearance.current
            .baseTextStyle.s12().bold()
            .foreground(Color.White)
            .copy(textAlign = TextAlign.Center)
    )
}