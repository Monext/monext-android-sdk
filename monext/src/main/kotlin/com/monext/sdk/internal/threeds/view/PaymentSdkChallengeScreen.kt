package com.monext.sdk.internal.threeds.view

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.monext.sdk.LocalAppearance
import com.monext.sdk.PaymentOverlayToggle
import com.monext.sdk.internal.data.LocalSessionStateRepo
import com.monext.sdk.internal.threeds.model.AuthenticationResponse
import com.monext.sdk.internal.threeds.model.ChallengeUseCaseCallback
import com.monext.sdk.internal.threeds.model.SdkChallengeData
import kotlinx.coroutines.launch

/**
 * Composant qui permet d'afficher la page de challenge du SDK 3DS
 * State : SDK_CHALLENGE
 */
@Composable
internal fun  PaymentSdkChallengeScreen(sdkChallengeData: SdkChallengeData, showOverlay: (PaymentOverlayToggle) -> Unit) {

    val scope = rememberCoroutineScope()
    val theme = LocalAppearance.current
    val sessionStateRepository = LocalSessionStateRepo.current
    val activity = LocalActivity.current

    LaunchedEffect(Unit) {
        sessionStateRepository.makeThreeDsChallengeFlow(
            activity = activity!!,
            sdkChallengeData = sdkChallengeData,
            theme = theme,
            object: ChallengeUseCaseCallback {
                override fun onChallengeCompletion(authenticationResponse: AuthenticationResponse) {
                    scope.launch {
                        showOverlay(PaymentOverlayToggle.on())
                        sessionStateRepository.makeSdkPayment(authenticationResponse)
                        showOverlay(PaymentOverlayToggle.off())
                    }
                }
            }
        )
    }

    Box(Modifier
        .fillMaxSize()
        .background(theme.backgroundColor)) {
    }

}