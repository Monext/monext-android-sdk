package com.monext.sdk

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.monext.sdk.internal.ext.rgba
import java.time.format.DateTimeFormatter

/**

## Discussion

Used to configure the visual elements of the payment sheet.

All available UI customizations are contained in this class. You are not required to modify any element, the default is a black-and-white light theme.
It is recommended to provide the ``PaymentSheetConfiguration/headerTitle`` or ``PaymentSheetConfiguration/headerImage`` at a minimum in order to identify your brand.

> Note: UI customizations are extensively documented with examples [here](https://www.figma.com).

### Header

- ``headerTitle``
- ``headerImage``
- ``headerBackgroundColor``
- ``onHeaderBackgroundColor``

### Colors

- ``primaryColor``
- ``onPrimaryColor``
- ``secondaryColor``
- ``onSecondaryColor``
- ``backgroundColor``
- ``onBackgroundColor``
- ``surfaceColor``
- ``onSurfaceColor``
- ``confirmationColor``
- ``onConfirmationColor``
- ``errorColor``

### Textfield

- ``textfieldLabelColor``
- ``textfieldTextColor``
- ``textfieldBorderColor``
- ``textfieldBorderSelectedColor``
- ``textfieldBackgroundColor``
- ``textfieldAccessoryColor``

### Dimensions

- ``buttonRadius``
- ``cardRadius``
- ``textfieldRadius``
- ``textfieldStroke``
- ``textfieldStrokeSelected``
- ``paymentMethodShape-swift.property``

### Result

- ``successImage``
- ``failureImage``
 */
class Appearance(

    // MARK: - Public properties -

    // MARK: Colors

    val primaryColor: Color = Defaults.primaryColor,
    val onPrimaryColor: Color = Defaults.onPrimaryColor,
    val secondaryColor: Color = Defaults.secondaryColor,
    val onSecondaryColor: Color = Defaults.onSecondaryColor,
    val backgroundColor: Color = Defaults.backgroundColor,
    val onBackgroundColor: Color = Defaults.onBackgroundColor,
    val surfaceColor: Color = Defaults.surfaceColor,
    val onSurfaceColor: Color = Defaults.onSurfaceColor,
    val confirmationColor: Color = Defaults.confirmationColor,
    val onConfirmationColor: Color = Defaults.onConfirmationColor,
    val errorColor: Color = Defaults.errorColor,
    val pendingColor: Color = Defaults.pendingColor,

    // MARK: Textfield

    textfieldLabelColor: Color? = null,
    textfieldTextColor: Color? = null,
    textfieldBorderColor: Color? = null,
    textfieldBorderSelectedColor: Color? = null,
    textfieldBackgroundColor: Color? = null,
    textfieldAccessoryColor: Color? = null,

    // MARK: Dimensions

    val buttonRadius: Dp = Defaults.buttonRadius,
    val cardRadius: Dp = Defaults.cardRadius,
    val textfieldRadius: Dp = Defaults.textfieldRadius,
    val textfieldStroke: Dp = Defaults.textfieldStroke,
    val textfieldStrokeSelected: Dp = Defaults.textfieldStrokeSelected,
    val paymentMethodShape: PaymentMethodShape = Defaults.paymentMethodShape,

    // MARK: Header

    val headerTitle: String? = null,
    val headerImage: Painter? = null,
    val headerBackgroundColor: Color = Defaults.headerBackgroundColor,
    val onHeaderBackgroundColor: Color = Defaults.onHeaderBackgroundColor,

    // MARK: Result

    /// The image shown to the user on the success screen when the transaction has succeeded
    val successImage: Painter? = null,

    /// The image shown to the user on the failure screen when the transaction has failed
    val failureImage: Painter? = null,

    /// The image shown to the user on the pending screen when the transaction has on hold
    val pendingImage: Painter? = null
) {

    val textfieldLabelColor: Color = textfieldLabelColor ?: onBackgroundColor
    val textfieldTextColor: Color = textfieldTextColor ?: onBackgroundColor
    val textfieldBorderColor: Color = textfieldBorderColor ?: onBackgroundColor.copy(alpha = 0.2f)
    val textfieldBorderSelectedColor: Color = textfieldBorderSelectedColor ?: onBackgroundColor
    val textfieldBackgroundColor: Color = textfieldBackgroundColor ?: Color.Transparent
    val textfieldAccessoryColor: Color = textfieldAccessoryColor ?: onBackgroundColor.copy(alpha = 0.3f)

    // region Derived properties

    internal val primaryAlpha: Color = primaryColor.copy(alpha = 0.05f)
    internal val secondaryAlpha: Color = secondaryColor.copy(alpha = 0.05f)
    internal val confirmationAlpha: Color = confirmationColor.copy(alpha = 0.05f)
    internal val onSurfaceAlpha: Color = onSurfaceColor.copy(alpha = 0.1f)
    internal val onSurfaceCardNumber: Color = onSurfaceColor.copy(alpha = 0.6f)
    internal val paymentMethodRadius: Dp = when (paymentMethodShape) {
        PaymentMethodShape.ROUND -> 10.dp
        PaymentMethodShape.SQUARE -> 0.dp
    }
    internal val textfieldLabelOnSurfaceColor: Color = textfieldLabelColor ?: onSurfaceColor
    internal val textfieldTextOnSurfaceColor: Color = textfieldTextColor ?: onSurfaceColor
    internal val textfieldBorderOnSurfaceColor: Color = textfieldBorderColor ?: onSurfaceColor.copy(alpha = 0.2f)
    internal val textfieldBorderSelectedOnSurfaceColor: Color = textfieldBorderSelectedColor ?: onSurfaceColor
    internal val textfieldBackgroundOnSurfaceColor: Color = textfieldBackgroundColor ?: Color.Transparent
    internal val textfieldAccessoryOnSurfaceColor: Color = textfieldAccessoryColor ?: onSurfaceColor.copy(alpha = 0.3f)
    internal val onHeaderBackgroundAlpha: Color = onHeaderBackgroundColor.copy(alpha = 0.1f)

    // endregion

    // region Fonts

    internal val baseTextStyle = TextStyle(
        fontFamily = FontFamily(
            Font(R.font.avenir_next_demibold, FontWeight.SemiBold),
            Font(R.font.avenir_next_bold, FontWeight.Bold)
        ),
        fontWeight = FontWeight.SemiBold
    )

    // endregion

    // region Defaults

    /**
    Default values for required properties

    > Note: Internal use
     */
    private interface Defaults {

        interface Colors {
            companion object {
                val arsenic = Color.rgba(56, 68, 75)
                val cultured = Color.rgba(244, 245, 247)
                val malachite = Color.rgba(0, 206, 106)
                val alabamaCrimson = Color.rgba(172, 0, 54)
                val radicalRed = Color.rgba(253, 47, 111)
            }
        }

        companion object {
            val primaryColor: Color = Colors.arsenic
            val onPrimaryColor: Color = Color.White
            val secondaryColor: Color = Colors.radicalRed
            val onSecondaryColor: Color = Color.White
            val backgroundColor: Color = Colors.cultured
            val onBackgroundColor: Color = Colors.arsenic
            val surfaceColor: Color = Color.White
            val onSurfaceColor: Color = Colors.arsenic
            val confirmationColor: Color = Colors.malachite
            val onConfirmationColor: Color = Colors.arsenic
            val errorColor: Color = Colors.alabamaCrimson
            val pendingColor: Color = Colors.arsenic

            val buttonRadius: Dp = 24.dp
            val cardRadius: Dp = 16.dp
            val textfieldRadius: Dp = 10.dp
            val textfieldStroke: Dp = 1.dp
            val textfieldStrokeSelected: Dp = 2.dp
            val paymentMethodShape = PaymentMethodShape.ROUND

            val headerBackgroundColor = Colors.cultured
            val onHeaderBackgroundColor = Colors.arsenic
        }
    }

    // endregion

    // region Enums

    /// Controls the shape of the payment method icons found in the SDK.
    enum class PaymentMethodShape {
        ROUND, SQUARE
    }

    // endregion

    // region Internal

    internal companion object {
        internal val cardNetworkFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMyy")
        internal val cardPresentationFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MM / yy")
    }

    // endregion
}

