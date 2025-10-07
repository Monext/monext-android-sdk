package com.monext.sdk.internal.threeds

import com.monext.sdk.internal.service.Logger
import com.monext.sdk.internal.threeds.model.ChallengeUseCaseCallback
import com.monext.sdk.internal.threeds.model.SdkChallengeData
import com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeStatusReceiver
import com.netcetera.threeds.sdk.api.transaction.challenge.events.CompletionEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.ProtocolErrorEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.RuntimeErrorEvent

/**
 * Classe qui permet d'écouter les évènement reçus par le SDK 3DS.
 * Dans tous les cas, on délègue la gestion du résultat à Payline => on envoi toujours la requete de paiement.
 * C'est Payline, qui en fonction du statut fera une trs OK/KO.
 */
internal class CustomChallengeStatusReceiver(val logger: Logger,
                                             val sdkChallengeData: SdkChallengeData,
                                             val useCaseCallback: ChallengeUseCaseCallback) : ChallengeStatusReceiver {

    override fun completed(p0: CompletionEvent?) {
        logger.d("CustomChallengeStatusReceiver", "Challenge completed ! => ${p0.toString()}")
        useCaseCallback.onChallengeCompletion(sdkChallengeData.toAuthenticationResponse(p0?.transactionStatus))
    }

    override fun cancelled() {
        logger.d("CustomChallengeStatusReceiver", "Challenge cancelled !")
        useCaseCallback.onChallengeCompletion(sdkChallengeData.toAuthenticationResponse())
    }

    override fun timedout() {
        logger.w("CustomChallengeStatusReceiver", "Challenge timedout !")
        useCaseCallback.onChallengeCompletion(sdkChallengeData.toAuthenticationResponse())
    }

    override fun protocolError(p0: ProtocolErrorEvent?) {
        val headerMessage = "Challenge failed from ProtocolErrorEvent => errorCode: ${p0?.errorMessage?.errorCode} - " +
                "errorDetails: ${p0?.errorMessage?.errorDetails} - " +
                "errorDescription: ${p0?.errorMessage?.errorDescription} - " +
                "errorComponent: ${p0?.errorMessage?.errorComponent} - " +
                "errorMessageType:${p0?.errorMessage?.errorMessageType} - " +
                "messageVersion: ${p0?.errorMessage?.messageVersionNumber}"

        logger.e("CustomChallengeStatusReceiver", headerMessage)
        useCaseCallback.onChallengeCompletion(sdkChallengeData.toAuthenticationResponse())
    }

    override fun runtimeError(p0: RuntimeErrorEvent?) {

        val headerMessage = "Challenge failed from RuntimeErrorEvent => errorCode: ${p0?.errorCode} - errorMessage:${p0?.errorMessage}"

        logger.e("CustomChallengeStatusReceiver", headerMessage)
        useCaseCallback.onChallengeCompletion(sdkChallengeData.toAuthenticationResponse())
    }
}