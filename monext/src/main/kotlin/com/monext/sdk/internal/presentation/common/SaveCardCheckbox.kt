package com.monext.sdk.internal.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.monext.sdk.LocalAppearance
import com.monext.sdk.internal.ext.foreground
import com.monext.sdk.internal.ext.s14

@Composable
internal fun SaveCardCheckbox(label: String, saveCard: Boolean, onSaveCardChecked: (Boolean) -> Unit) {

    val theme = LocalAppearance.current

    Row(
        Modifier.Companion.clickable {
            onSaveCardChecked(!saveCard)
        },
        verticalAlignment = Alignment.Companion.CenterVertically
    ) {

        Text(
            label,
            Modifier.Companion.weight(1f),
            style = theme.baseTextStyle.s14()
                .foreground(theme.onBackgroundColor)
        )

        Checkbox(
            checked = saveCard,
            modifier = Modifier.testTag("saveCardCheckbox"),
            onCheckedChange = onSaveCardChecked,
            colors = CheckboxDefaults.colors().copy(
                checkedCheckmarkColor = theme.onSecondaryColor,
//                    uncheckedCheckmarkColor = TODO(),
                checkedBoxColor = theme.secondaryColor,
//                    uncheckedBoxColor = theme.backgroundColor,
//                    disabledCheckedBoxColor = TODO(),
//                    disabledUncheckedBoxColor = TODO(),
//                    disabledIndeterminateBoxColor = TODO(),
                checkedBorderColor = theme.secondaryColor,
                uncheckedBorderColor = theme.secondaryColor,
//                    disabledBorderColor = TODO(),
//                    disabledUncheckedBorderColor = TODO(),
//                    disabledIndeterminateBorderColor = TODO()
            )
        )
    }
}