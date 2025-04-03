package com.monext.sdk.internal.threeds.response

import kotlinx.serialization.Serializable

@Serializable
internal data class DirectoryServerSdkKey(
    val scheme:String,
    val rid:String,
    val publicKey:String,
    val rootPublicKey:String)