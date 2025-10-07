package com.monext.sdk.internal.threeds.model

import com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeParameters
import kotlinx.serialization.Serializable

/**
 * Classe qui représente les données de context 3DS renvoyées par Payline.
 */
@Serializable
internal data class SdkChallengeData(val cardType: Int,
                                     val threeDSServerTransID: String,
                                     val threeDSVersion: String,
                                     val authenticationType: String,
                                     val transStatus: String? = "",
                                     val sdkTransID: String,
                                     val dsTransID: String,
                                     val acsTransID: String? = "",
                                     val acsRenderingType: String,
                                     val acsReferenceNumber: String,
                                     val acsSignedContent: String,
                                     val acsOperatorID: String,
                                     val acsChallengeMandated: String) {

    /**
     * Crée un objet ChallengeParameters à partir des données SDK Challenge
     */
    fun toSdkChallengeParameters() : ChallengeParameters {
        val challengeParameters = ChallengeParameters()
        challengeParameters.set3DSServerTransactionID(threeDSServerTransID)
        challengeParameters.acsTransactionID = acsTransID
        challengeParameters.acsRefNumber = acsReferenceNumber
        challengeParameters.acsSignedContent = acsSignedContent
        return challengeParameters
    }

    /**
     * Crée un objet AuthenticationResponse à partir des données SDK Challenge
     */
    fun toAuthenticationResponse(transactionStatus: String? = null) : AuthenticationResponse {
        return  AuthenticationResponse(
            acsReferenceNumber = acsReferenceNumber,
            acsTransID = acsTransID,
            threeDSVersion = threeDSVersion,
            threeDSServerTransID = threeDSServerTransID,
            transStatus = transactionStatus
        )
    }
}