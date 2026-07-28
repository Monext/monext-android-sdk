package com.monext.sdk.internal.data.sessionstate

import kotlinx.serialization.Serializable;

@Serializable
internal data class ActiveWaiting(
    val needActiveWaitingAction: Boolean,
    val message: CustomMessage?,
    val cardCode: String,
    val contractNumber: String,
    val walletCardIndex: Int,
    val merchantReturnUrl: String?
)