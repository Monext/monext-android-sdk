package com.monext.sdk.internal.threeds

import com.monext.sdk.SdkTestHelper
import com.monext.sdk.SdkTestHelper.Companion.createInternalSDKContext
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.service.CustomLogger
import com.monext.sdk.internal.threeds.model.ChallengeUseCaseCallback
import com.netcetera.threeds.sdk.api.transaction.challenge.ErrorMessage
import com.netcetera.threeds.sdk.api.transaction.challenge.events.CompletionEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.ProtocolErrorEvent
import com.netcetera.threeds.sdk.api.transaction.challenge.events.RuntimeErrorEvent
import io.mockk.clearAllMocks
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CustomChallengeStatusReceiverTest {

    private lateinit var internalSDKContext: InternalSDKContext
    private lateinit var customLogger: CustomLogger
    private lateinit var challengeUseCaseCallback: ChallengeUseCaseCallback
    internal val sdkChallengeData = SdkTestHelper.createSdkChallengeData()
    internal val authenticationResponse = SdkTestHelper.createAuthenticationResponse()

    private lateinit var underTest: CustomChallengeStatusReceiver

    @BeforeEach
    fun setUp() {
        // Clear any existing singleton instance
        clearAllMocks()

        challengeUseCaseCallback = mockk<ChallengeUseCaseCallback>(relaxed = true)
        customLogger = mockk<CustomLogger>(relaxed = true)
        internalSDKContext = createInternalSDKContext()
        internalSDKContext.logger = customLogger;

        underTest = spyk(
            CustomChallengeStatusReceiver(
                internalSDKContext = internalSDKContext,
                sdkChallengeData = sdkChallengeData,
                useCaseCallback = challengeUseCaseCallback
            )
        )
    }

    @Test
    fun cancelled() {
        underTest.cancelled()

        // Verif
        verify { internalSDKContext.logger.d("CustomChallengeStatusReceiver", "Challenge cancelled !") }
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
        verify { internalSDKContext.logger.e("CustomChallengeStatusReceiver", "Challenge failed from ProtocolErrorEvent => errorCode: errorCodeMsg - errorDetails: errorDetailMsg - errorDescription: errorDescriptionMsg - errorComponent: errorComponentMsg - errorMessageType:errorMessageTypeMsg - messageVersion: messageVersionNumberMsg",
            null) }
        verify { challengeUseCaseCallback.onChallengeCompletion(authenticationResponse) }
    }

    @Test
    fun runtimeError() {
        val errorEvent = RuntimeErrorEvent("xxx-yyyy", "errorMessage aaaa")

        // Test
        underTest.runtimeError(errorEvent)

        // Verif
        verify { internalSDKContext.logger.e("CustomChallengeStatusReceiver", "Challenge failed from RuntimeErrorEvent => errorCode: xxx-yyyy - errorMessage:errorMessage aaaa",
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
        verify { internalSDKContext.logger.d("CustomChallengeStatusReceiver", "Challenge completed ! => CompletionEvent{sdkTransactionID='111222333'\n" +
                ", transactionStatus='U'}") }
        verify { challengeUseCaseCallback.onChallengeCompletion(authenticationResponse) }
    }

    @Test
    fun timedout() {
        underTest.timedout()

        // Verif
        verify { internalSDKContext.logger.w("CustomChallengeStatusReceiver", "Challenge timedout !") }
        verify { challengeUseCaseCallback.onChallengeCompletion(authenticationResponse) }
    }

}