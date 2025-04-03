package com.monext.sdk.internal.threeds

import com.netcetera.threeds.sdk.api.ui.logic.ButtonCustomization
import com.netcetera.threeds.sdk.api.ui.logic.LabelCustomization
import com.netcetera.threeds.sdk.api.ui.logic.TextBoxCustomization
import com.netcetera.threeds.sdk.api.ui.logic.ToolbarCustomization
import com.netcetera.threeds.sdk.api.ui.logic.UiCustomization
import com.netcetera.threeds.sdk.api.ui.logic.ViewCustomization
import com.monext.sdk.internal.api.configuration.InternalSDKContext

class ThreeDSUICustomization {
    companion object {
        fun createUICustomization(internalSDKContext: InternalSDKContext) : UiCustomization {

            // TODO : Apply l'UICustomization basée sur 'appearance' quand on sera à la page de challenge
            val appearance = internalSDKContext.appearance
            val uiCustomization = UiCustomization()

            val labelCustomization = LabelCustomization().apply {
                headingTextFontSize = 24
                textFontSize = 16
                // ...
            }
            uiCustomization.labelCustomization = labelCustomization

            val textBoxCustomization = TextBoxCustomization().apply {
//                borderColor = "#e4e4e4"
                borderWidth = 2
                cornerRadius = 20
                // ...
            }
            uiCustomization.textBoxCustomization = textBoxCustomization

            val toolbarCustomization = ToolbarCustomization().apply {
//                backgroundColor = "#ec5851"
//                textColor = "#ffffff"
                buttonText = "Cancel"
                headerText = "Secure Checkout"
                // ...
            }
            uiCustomization.toolbarCustomization = toolbarCustomization

            val submitButtonCustomization = ButtonCustomization().apply {
//                backgroundColor = "#ec5851"
                cornerRadius = 20
                textFontSize = 14
//                textColor = "#ffffff"
            }
            uiCustomization.setButtonCustomization(submitButtonCustomization, UiCustomization.ButtonType.SUBMIT)

            val cancelButtonCustomization = ButtonCustomization().apply {
//                textColor = "#ffffff"
                textFontSize = 14
            }
            uiCustomization.setButtonCustomization(cancelButtonCustomization, UiCustomization.ButtonType.CANCEL)

            val viewCustomization = ViewCustomization().apply {
//                challengeViewBackgroundColor = "#ffffff"
//                progressViewBackgroundColor = "#ffffff"
            }
            uiCustomization.viewCustomization = viewCustomization

            return uiCustomization
        }
    }

}