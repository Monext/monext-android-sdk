package com.monext.sdk.internal.threeds.model

internal fun interface ChallengeUseCaseCallback {

    /**
     * Called on [AuthenticationResponse] change.
     *
     * @param authenticationResponse [AuthenticationResponse]
     */
    fun onChallengeCompletion(authenticationResponse: AuthenticationResponse)
}