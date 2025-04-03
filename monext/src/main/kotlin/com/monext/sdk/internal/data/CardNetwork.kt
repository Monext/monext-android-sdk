package com.monext.sdk.internal.data

import android.os.Parcelable
import com.monext.sdk.internal.data.sessionstate.PaymentMethodCardCode
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CardNetwork(
    val network: PaymentMethodCardCode,
    val code : String
): Parcelable