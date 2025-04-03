package com.monext.sdk.internal.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import com.monext.sdk.Appearance
import com.monext.sdk.LocalAppearance
import com.monext.sdk.LocalEnvironment
import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.MnxtSDKContext
import com.monext.sdk.internal.api.PaymentAPIFactory
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.threeds.ThreeDSManager
import com.monext.sdk.internal.data.LocalSessionStateRepo
import com.monext.sdk.internal.data.SessionStateRepository
import com.monext.sdk.internal.ext.rgba

@Composable
internal fun darkAppearance(): Appearance {

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
        headerBackgroundColor = Color.rgba(27, 25, 31),
        onHeaderBackgroundColor = Color.White
    )
}

@Composable
internal fun PreviewWrapper(modifier: Modifier = Modifier.background(Color.rgba(230, 250, 230)), content: @Composable ColumnScope.() -> Unit) {

    val sdkContext = MnxtSDKContext(
        environment = MnxtEnvironment.Sandbox,
        appearance = darkAppearance(),

        )

    val paymentApi = PaymentAPIFactory.create(
        sdkContext.environment,
        sdkContext.config.language,
        isLocalInspectionMode = LocalInspectionMode.current
    )
    val internalSDKContext = InternalSDKContext(sdkContext = sdkContext)
    val repository = SessionStateRepository(
        paymentApi,
        internalSDKContext,
        ThreeDSManager(paymentApi = paymentApi, internalSDKContext = internalSDKContext, context = LocalContext.current)
    )

    MaterialTheme {
        CompositionLocalProvider(
            LocalAppearance provides sdkContext.appearance,
            LocalEnvironment provides sdkContext.environment,
            LocalSessionStateRepo provides repository,
            LocalTextStyle provides sdkContext.appearance.baseTextStyle
        ) {
            Scaffold { innerPadding ->
                Column(modifier.padding(innerPadding).fillMaxWidth()) {
                    Spacer(Modifier.Companion.weight(1f))
                    content()
                    Spacer(Modifier.Companion.weight(1f))
                }
            }
        }
    }
}