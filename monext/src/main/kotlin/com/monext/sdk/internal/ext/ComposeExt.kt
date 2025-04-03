package com.monext.sdk.internal.ext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// region Color

internal fun Color.Companion.rgba(r: Int, g: Int, b: Int, a: Int = 255): Color =
    Color(r.toFloat() / 255f, g.toFloat() / 255f, b.toFloat() / 255f, a.toFloat() / 255f)

// endregion

// region TextStyle

internal fun TextStyle.bold() = copy(fontWeight = FontWeight.Bold)

internal fun TextStyle.s11() = copy(fontSize = 11.sp)
internal fun TextStyle.s12() = copy(fontSize = 12.sp)
internal fun TextStyle.s14() = copy(fontSize = 14.sp)
internal fun TextStyle.s16() = copy(fontSize = 16.sp)
internal fun TextStyle.s18() = copy(fontSize = 18.sp)
internal fun TextStyle.s20() = copy(fontSize = 20.sp)
internal fun TextStyle.s24() = copy(fontSize = 24.sp)

internal fun TextStyle.foreground(color: Color) = copy(color = color)

// endregion