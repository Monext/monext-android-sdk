package com.monext.sdkexample

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.monext.sdk.Appearance

enum class Theme {

    DEFAULT, DARK;

    @Composable
    fun appearance(): Appearance {
        return when (this) {
            DEFAULT -> Appearance(
                headerTitle = "Monext Demo",
                headerImage = painterResource(R.drawable.logo_monext)
            )
            DARK -> sampleDarkTheme()
        }
    }
}

@Composable
fun sampleDarkTheme(): Appearance {

    val primary = Color.rgba(162, 56, 255)
    val textfield = Color.rgba(170, 164, 175)

    return Appearance(
        primaryColor = primary,
        onPrimaryColor = Color.White,
        secondaryColor = primary,
        onSecondaryColor = Color.White,
        backgroundColor = Color.rgba(15, 13, 19),
        onBackgroundColor = Color.White,
        surfaceColor = Color.rgba(27, 25, 31),
        onSurfaceColor = Color.White,
        confirmationColor = Color.rgba(64, 207, 176),
        onConfirmationColor = Color.White,
        errorColor = Color.rgba(221, 32, 37),

        textfieldLabelColor = textfield,
        textfieldTextColor = Color.White,
        textfieldBorderColor = textfield.copy(alpha = 0.2f),
        textfieldBorderSelectedColor = primary,
        textfieldBackgroundColor = textfield.copy(alpha = 0.15f),
        textfieldAccessoryColor = Color.White.copy(alpha = 0.3f),

        buttonRadius = 12.dp,
        cardRadius = 12.dp,
        textfieldRadius = 10.dp,
        textfieldStroke = 0.dp,
        textfieldStrokeSelected = 2.dp,
        paymentMethodShape = Appearance.PaymentMethodShape.ROUND,

        headerTitle = "MONEXT DARK",
        headerImage = painterResource(R.drawable.logo_monext),
        headerBackgroundColor = Color.rgba(27, 25, 31),
        onHeaderBackgroundColor = Color.White
    )
}

private fun Color.Companion.rgba(r: Int, g: Int, b: Int, a: Int = 255): Color =
    Color(r.toFloat() / 255f, g.toFloat() / 255f, b.toFloat() / 255f, a.toFloat() / 255f)