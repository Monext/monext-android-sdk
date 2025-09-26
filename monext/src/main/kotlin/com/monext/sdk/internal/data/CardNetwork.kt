package com.monext.sdk.internal.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CardNetwork(
    val network: String,
    val code : String
): Parcelable