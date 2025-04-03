package com.monext.sdk.internal.presentation.common

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monext.sdk.LocalAppearance
import com.monext.sdk.R
import com.monext.sdk.internal.preview.PreviewWrapper

@Composable
internal fun PopImage(style: PopImageStyle) {

    val theme = LocalAppearance.current

    val circleColor = when (style) {
        is PopImageStyle.Success -> theme.confirmationColor
        is PopImageStyle.Failure -> theme.errorColor
        is PopImageStyle.Custom -> Color.Transparent
    }

    val painter = when (style) {
        is PopImageStyle.Success -> painterResource(R.drawable.ic_checkmark_large)
        is PopImageStyle.Failure -> painterResource(R.drawable.ic_exclamationpoint_large)
        is PopImageStyle.Custom -> style.painter
    }

    var imageSizeTarget by remember { mutableStateOf(0.dp) }
    val imageSize by animateDpAsState(
        targetValue = imageSizeTarget,
        animationSpec = keyframes<Dp> {
            durationMillis = 600
            128.dp atFraction 0.75f using EaseInOut
            106.dp atFraction 1f using EaseOut
        }
    )
    SideEffect { imageSizeTarget = 106.dp }

    Box(
        Modifier.height(200.dp).fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter,
            contentDescription = null,
            modifier = Modifier.size(imageSize)
                .background(circleColor, CircleShape)
        )
    }
}

internal sealed interface PopImageStyle {
    data object Success: PopImageStyle
    data object Failure: PopImageStyle
    data class Custom(val painter: Painter): PopImageStyle
}

@Preview
@Composable
internal fun PaymentSuccessSectionPreview() {
    PreviewWrapper {
        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            PopImage(PopImageStyle.Success)

            PopImage(PopImageStyle.Failure)

            PopImage(PopImageStyle.Custom(painterResource(R.drawable.logo_cards)))
        }
    }
}