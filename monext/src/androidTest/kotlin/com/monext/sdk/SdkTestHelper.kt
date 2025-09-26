package com.monext.sdk

import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.api.model.response.SessionStateType
import com.monext.sdk.internal.data.sessionstate.AdditionalData
import com.monext.sdk.internal.data.sessionstate.FormOption
import com.monext.sdk.internal.data.sessionstate.PaymentForm
import com.monext.sdk.internal.data.sessionstate.PaymentMethodData
import com.monext.sdk.internal.preview.PreviewSamples.Companion.posAddress
import com.monext.sdk.internal.preview.PreviewSamples.Companion.sessionInfo
import com.monext.sdk.internal.threeds.model.PaymentSdkChallenge
import com.monext.sdk.internal.threeds.model.SdkChallengeData

/**
 * Helper pour la construction des données de test
 */
class SdkTestHelper {
    companion object {

        internal fun createSdkChallengeData(): SdkChallengeData = SdkChallengeData(
            cardType = 1,
            threeDSServerTransID = "threeDSServerTransID",
            threeDSVersion = "threeDSVersion",
            authenticationType =     "authenticationType",
            transStatus = "transStatus",
            sdkTransID = "sdkTransID",
            dsTransID = "dsTransID",
            acsTransID =     "acsTransID",
            acsRenderingType = "acsRenderingType",
            acsReferenceNumber =     "acsReferenceNumber",
            acsSignedContent =     "acsSignedContent",
            acsOperatorID = "acsOperatorID",
            acsChallengeMandated =     "acsChallengeMandated"
        )

        internal fun createSessionState(state: SessionStateType) = SessionState(
            token = "fake_token",
            type = state,
            creationDate = "Tue Mar 25 12:33:22 CET 2025",
            cancelUrl = "https://yourdomain.com:8080/route/1.0/returns?paylinetoken=fake_token",
            pointOfSale = "POS_Fake",
            language = "en",
            returnUrl = "https://yourdomain.com:8080/route/1.0/returns?paylinetoken=fake_token",
            automaticRedirectAtSessionsEnd = false,
            info = sessionInfo,
            pointOfSaleAddress = posAddress,
            isSandbox = true,
            paymentMethodsList = null,
            paymentSuccess = null,
            paymentFailure = null,
            paymentSdkChallenge = PaymentSdkChallenge(createSdkChallengeData())
        )

        internal fun createPaymentMethodData(cardCode: String, hasForm: Boolean = false, form: PaymentForm? = null): PaymentMethodData =
            PaymentMethodData(
                cardCode = cardCode,
                contractNumber = cardCode,
                disabled = false,
                hasForm = hasForm,
                form = form,
                hasLogo = false,
                logo = null,
                isIsolated = false,
                options = listOf(FormOption.SAVE_PAYMENT_DATA),
                paymentMethodAction = null,
                additionalData = AdditionalData(
                    merchantCapabilities = null,
                    networks = null,
                    applePayMerchantId = null,
                    applePayMerchantName = null,
                    savePaymentDataChecked = null,
                    email = null,
                    date = null,
                    holder = null,
                    pan = null
                ),
                requestContext = null,
                shouldBeInTopPosition = null,
                state = null
            )
    }
}