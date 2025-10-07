package com.monext.sdk.internal.threeds.model

import kotlinx.serialization.Serializable

/**
 * Classe qui représente les données de context 3DS renvoyées par Payline.
 */
@Serializable
internal data class PaymentSdkChallenge(val sdkChallengeData: SdkChallengeData)