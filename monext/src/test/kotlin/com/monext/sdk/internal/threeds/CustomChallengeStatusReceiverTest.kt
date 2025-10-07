package com.monext.sdk.internal.threeds

import com.monext.sdk.SdkTestHelper
import com.monext.sdk.internal.data.SessionStateRepository
import com.monext.sdk.internal.service.CustomLogger
import com.monext.sdk.internal.threeds.model.ChallengeUseCaseCallback
import com.netcetera.threeds.sdk.api.transaction.challenge.ErrorMessage
import com.netcetera.threeds.sdk.api.transaction.challenge.events.CompletionEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.ProtocolErrorEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.RuntimeErrorEvent
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CustomChallengeStatusReceiverTest {

    @RelaxedMockK
    private lateinit var customLogger: CustomLogger
    @RelaxedMockK
    private lateinit var challengeUseCaseCallback: ChallengeUseCaseCallback

    internal val sdkChallengeData = SdkTestHelper.createSdkChallengeData() // Pour le InjectMock
    internal val authenticationResponse = SdkTestHelper.createAuthenticationResponse()

    @SpyK
    @InjectMockKs
    private lateinit var underTest: CustomChallengeStatusReceiver

    @Test
    fun cancelled() {
        underTest.cancelled()

        // Verif
        verify { customLogger.d("CustomChallengeStatusReceiver", "Challenge cancelled !") }
        verify { challengeUseCaseCallback.onChallengeCompletion(authenticationResponse) }
    }

    @Test
    fun protocolError() {
        val protocolErrorEvent = ProtocolErrorEvent("111222333", ErrorMessage("111222333",
            "errorCodeMsg", "errorDescriptionMsg",  "errorDetailMsg",
            "errorComponentMsg",  "errorMessageTypeMsg",  "messageVersionNumberMsg"))

        // Test
        underTest.protocolError(protocolErrorEvent)

        // Verif
        verify { customLogger.e("CustomChallengeStatusReceiver", "Challenge failed from ProtocolErrorEvent => errorCode: errorCodeMsg - errorDetails: errorDetailMsg - errorDescription: errorDescriptionMsg - errorComponent: errorComponentMsg - errorMessageType:errorMessageTypeMsg - messageVersion: messageVersionNumberMsg",
            null) }
        verify { challengeUseCaseCallback.onChallengeCompletion(authenticationResponse) }
    }

    @Test
    fun runtimeError() {
        val errorEvent = RuntimeErrorEvent("xxx-yyyy", "errorMessage aaaa")

        // Test
        underTest.runtimeError(errorEvent)

        // Verif
        verify { customLogger.e("CustomChallengeStatusReceiver", "Challenge failed from RuntimeErrorEvent => errorCode: xxx-yyyy - errorMessage:errorMessage aaaa",
            null) }
        verify { challengeUseCaseCallback.onChallengeCompletion(authenticationResponse) }
    }

    @Test
    fun completed() {
        val authenticationResponse = SdkTestHelper.createAuthenticationResponse("U")
        val completionEvent = CompletionEvent( "111222333", "U")

        // Test
        underTest.completed(completionEvent)

        // Verif
        verify { customLogger.d("CustomChallengeStatusReceiver", "Challenge completed ! => CompletionEvent{sdkTransactionID='111222333'\n" +
                ", transactionStatus='U'}") }
        verify { challengeUseCaseCallback.onChallengeCompletion(authenticationResponse) }
    }

    @Test
    fun timedout() {
        underTest.timedout()

        // Verif
        verify { customLogger.w("CustomChallengeStatusReceiver", "Challenge timedout !") }
        verify { challengeUseCaseCallback.onChallengeCompletion(authenticationResponse) }
    }

}