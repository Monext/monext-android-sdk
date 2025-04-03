package com.monext.sdk

import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType

/**
 * Used to configure the visual elements for the GooglePay button
 * see the [documentation](https://developers.google.com/pay/api/android/guides/brand-guidelines) for
 * possible values.
 */
data class GooglePayConfiguration(
    val theme: ButtonTheme = ButtonTheme.Dark,
    val type: ButtonType = ButtonType.Plain,
)