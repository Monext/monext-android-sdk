package com.monext.sdk

import com.monext.sdk.internal.api.AvailableCardNetworksRequest
import com.monext.sdk.internal.api.AvailableCardNetworksResponse
import com.monext.sdk.internal.api.HandledContract
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.api.model.DeviceInfo
import com.monext.sdk.internal.api.model.request.PaymentParams
import com.monext.sdk.internal.api.model.request.PaymentRequest
import com.monext.sdk.internal.api.model.request.SecuredPaymentParams
import com.monext.sdk.internal.api.model.request.SecuredPaymentRequest
import com.monext.sdk.internal.api.model.request.WalletPaymentRequest
import com.monext.sdk.internal.data.CardNetwork
import com.monext.sdk.internal.data.FormData
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.sessionstate.PaymentMethodCardCode
import com.monext.sdk.internal.data.sessionstate.PaymentMethodData
import com.monext.sdk.internal.presentation.PaymentAttempt
import com.monext.sdk.internal.service.CustomLogger
import com.monext.sdk.internal.threeds.model.AuthenticationResponse
import com.monext.sdk.internal.threeds.model.SdkChallengeData
import com.monext.sdk.internal.threeds.model.SdkContextData
import com.monext.sdk.internal.threeds.response.DirectoryServerSdkKey
import com.monext.sdk.internal.threeds.response.DirectoryServerSdkKeyResponse
import io.mockk.mockk
import kotlinx.serialization.json.Json

/**
 * Helper pour la construction des données de test
 */
class SdkTestHelper {
    companion object {

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
            encodeDefaults = true
        }

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

        internal fun createSdkContextData(): SdkContextData = SdkContextData(
            deviceRenderingOptionsIF= ThreeDSConfiguration.DEFAULT_DEVICE_RENDERING_OPTIONS_IF,
            deviceRenderOptionsUI= ThreeDSConfiguration.DEFAULT_DEVICE_RENDER_OPTIONS_UI,
            maxTimeout= ThreeDSConfiguration.MAX_TIMEOUT,
            referenceNumber= "refNumber_xx",
            ephemPubKey= "ephemPubKey_yyy",
            appID= "sdkAppID_pp",
            transID= "sdkTransactionID_kk",
            encData= "deviceData_qq"
        )

        internal fun createAuthenticationResponse(transStatus: String ? = null): AuthenticationResponse {
            return createSdkChallengeData().toAuthenticationResponse(transStatus)
        }

        internal fun createWalletPaymentRequest(): WalletPaymentRequest {
            return WalletPaymentRequest(
                cardCode = PaymentMethodCardCode.PAYPAL,
                index = 1,
                isEmbeddedRedirectionAllowed = true,
                merchantReturnUrl = "http://merchant.com/return/url",
                securedPaymentParams = createSecuredParams()
            )
        }

        internal fun createPaymentRequestCB(): PaymentRequest = PaymentRequest(
            cardCode = "CB",
            contractNumber = "CB_01",
            isEmbeddedRedirectionAllowed = false,
            merchantReturnUrl = "http://merchant.dev.com/return/url",
            paymentParams = PaymentParams(
                network = "2",
                expirationDate = "1228",
                savePaymentData = false,
                holderName = "Jean-Claude"
            )
        )

        internal fun createPaymentRequestGooglePay(): PaymentRequest = PaymentRequest(
            cardCode = "GOOGLE_PAY",
            contractNumber = "GOOGLE_01",
            isEmbeddedRedirectionAllowed = false,
            merchantReturnUrl = "http://merchant.dev.com/return/url",
            paymentParams = PaymentParams(
                googlePayData = "{\"apiVersion\":2,\"apiVersionMinor\":0,\"email\":\"genty.thomas@gmail.com\",\"paymentMethodData\":{\"description\":\"Test Card: Visa •••• 1111\",\"info\":{\"assuranceDetails\":{\"accountVerified\":true,\"cardHolderAuthenticated\":false},\"billingAddress\":{\"countryCode\":\"US\",\"name\":\"Card Holder Name\",\"phoneNumber\":\"6505555555\",\"postalCode\":\"94043\"},\"cardDetails\":\"1111\",\"cardFundingSource\":\"CREDIT\",\"cardNetwork\":\"VISA\"},\"tokenizationData\":{\"token\":\"{\\\"signature\\\":\\\"MEYCIQDehpOq5Ug6urtWzQD76hLa1HF4MIqGQ0ZZDPOEGmasPwIhAKfhJmItFQGJR9EZ8J6RFbANUwvSQIlK1iM38517tCOm\\\",\\\"intermediateSigningKey\\\":{\\\"signedKey\\\":\\\"{\\\\\\\"keyValue\\\\\\\":\\\\\\\"MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE6IMK7zDhF+Ug7X7mipkDnWyghrTVvvZfnjg5XS7G77Ya/4iF3rXJO+BJx4mY8EJmDQrugKy1k/y0Tf29CnQnRw\\\\\\\\u003d\\\\\\\\u003d\\\\\\\",\\\\\\\"keyExpiration\\\\\\\":\\\\\\\"1759620591007\\\\\\\"}\\\",\\\"signatures\\\":[\\\"MEUCIQCn4LpiUh7gTR8o+sAJFs5bGUCWQstlgMiAUKJoONBJnAIgKo3Kn0Qf5HtNza/wBRWaIOYjNWXOIksBbOEd3NkayYU\\\\u003d\\\"]},\\\"protocolVersion\\\":\\\"ECv2\\\",\\\"signedMessage\\\":\\\"{\\\\\\\"encryptedMessage\\\\\\\":\\\\\\\"vkT+3fh76rKRsLJ+w0qczpvaaR1jWnHeoUy4N3SCbGR+wJkJQQ+iZxSEWRi1nhKJfavgaldymKLQvjGxpw45T8Vy3ZVHdaY+12r2IbaOR7xIxVr5gfJCzXM0Rx9viAUGa2ElwF+khiDCF1ef9w8z+bdt+8NJdOwyERHnt6svHSWGytldYvY8NW4hP7WMng0zKbkkFyP5qOcJ2GmQ5TNzOTcaQBh0Jdyhwhxa3Y4CWr9gY7VBhj94xqibtKVVJ7FvWscppuCwNOrF7nTk1oqKnC48WXzb26HByNksSl9PYwaiwurXJEFRJi46f7fscaOWVc98xpRcNzM4hWfMo2kqK9xN0Oz0goDM7h0sSaDMh8w6AmxLY6kS3yJxZNHhK5MZ8kK6dS3190P4G0XNYaoEgYq5KxF+tcTujX+PLL3SSiPQLd+5iq99FAZHsQt22yto4CPYOKxAvo+g+Q+a\\\\\\\",\\\\\\\"ephemeralPublicKey\\\\\\\":\\\\\\\"BARfsLy23mq5xHv7eh6rYpJcHuAyg2vIxbrELvPN0bAFcOJ0hjC/eK0e0QJ5AAhK97j+gojqhDDZRB17xfJ2/0c\\\\\\\\u003d\\\\\\\",\\\\\\\"tag\\\\\\\":\\\\\\\"66dhF0LqeoeX6FTFX2dVFyQGc0TNWiMeTmH94T7YaWo\\\\\\\\u003d\\\\\\\"}\\\"}\",\"type\":\"PAYMENT_GATEWAY\"},\"type\":\"CARD\"},\"shippingAddress\":{\"address1\":\"1600 Amphitheatre Parkway1\",\"address2\":\"\",\"address3\":\"\",\"administrativeArea\":\"CA\",\"countryCode\":\"US\",\"locality\":\"Mountain View\",\"name\":\"US User\",\"phoneNumber\":\"+1 650-555-5555\",\"postalCode\":\"94043\",\"sortingCode\":\"\"}}"
            )
        )

        internal fun createSecuredPaymentRequestCB(): SecuredPaymentRequest = SecuredPaymentRequest(
            cardCode = "CB",
            contractNumber = "CB_01",
            deviceInfo = createDeviceInfo(),
            isEmbeddedRedirectionAllowed = false,
            merchantReturnUrl = "http://merchant.dev.com/return/url",
            paymentParams = PaymentParams(
                network = "2",
                expirationDate = "1228",
                savePaymentData = false,
                holderName = "Jean-Claude",
                sdkContextData = "{\"deviceRenderingOptionsIF\":\"01\",\"deviceRenderOptionsUI\":\"03\",\"maxTimeout\":60,\"referenceNumber\":\"3DS_LOA_SDK_NEAG_020301_00792\",\"ephemPubKey\":\"P-256;EC;3OXzA9Qt5sV8Ejqd_XXXXXXXXXXZW7Z0AnL4;VC94_YYYYYYYYYYYYYYYYY2r3g\",\"appID\":\"510000-0f48-4d7b-b00a-1b20000001\",\"transID\":\"e450000c0-2300-4000-a46f-3783b5141085\",\"encData\":\"xxxxxxxxxxxxxxxxxxxxxx.yyyyyyyyyyyyyyyyyyyy-oooooooooooooooooooooo-j9314Zwn_9LBTRvdSeI65JwHn_SB-KFTpnbZ8lFJMbfjcTgRTfoDpuU2ALLPIfE81yyUqulfYeUJ0tIVpQk6VMtwL5QfAIU2w4jZ30IlQIChCCW6OXipslYCihAAN-3g9HjY48dBQeNp-IHnMFVnnG-AxI65hgCptmJsoGAR7QrRgQ.zS1yZ6cHMPcdjvu2iw6ayA.nEN6fkOdbnCRZZSZ7UY39Qx829dGegNitt5QqSoYZ6-ZKiCCLHp2b_daHV3waQWZ4FaGMT9QLdQaX-tizhBi6tzb4yFIhWtNUDuz6_dQ-SNl-I4OIzAJURbXCkeY4gkH_rlyZdWCBykKwZ8PLBamgIKkXc9QEyvVhmxUrSdRUJ2DTJjQ2NEo_Xo90uJh62mbdvnYh8sD4-HoHPYDxoH-L-8bVdERM9ppSktuPneOvHwR5GR9Yk7RXv91emLV64QB-uWFJ-Jibg2pWjSZxTS5g-DkEzsjF3EQxIfewxdFmsmjV30G5Kzom0J3-iAfgPaRW8Ir9PCVYUSyn9p2GwN9EeU6W0Rvge4Fnz0-soDQ3DO8KU6ABueouZlE_CPHG7zJTXzOXuPXs5QFWImyXNdB_azgOFEWQLQ1071dWLmKkKCo3kAuzXTzeNqJJgHypprDmOUtpyDIaf-41_r6UVYmRXVIyQfYbW0mABFIzMD6NMLrlu4EB25g913wT_806xZK-q43N8XqBWwaRLvWhFstqEEELKImGzAB0YSP3v3z6lo1qh4q1OY53tcTpynq48MzIhXHRL4DmvxKvNLHOzjU7765GyjFv8Hm2rumoAV1Te50BZB5XKKivSBY52OBuqBWVg1KqLHzUlEx32jGqLZTyxP92AHOGBWHRlWf8EKC121311111111111111EwO5GtaT-34B9tOOHXgVruS9pl0PH8MztIwlfM3llBZo4xTGLPYNycVQ2c0qU1iAGO5YwPnhkABBTornkNQZJQK1M83cQ0HOtL-RRRew00000000000000000000eQO6Wxzkos0PRGOrJ1WYqmTkR4GFyg0g0QT8G94jSXYytPo9mzKfSYYPWPNjbD0Q64MJ8_wi9QzPzCeBR8bPj1724ppaGLz29Bjkcs2mP4w-7ZMT1G6685NTmt6HL176WQUjzlwpBOw03FnUUmG3mDRtzHF2Hccl_YWuBYWExQrUfDL-Ldjzg_D9b8qKeJ2ftVTwjZ9ih9vdYCi7L2cwc5ksJHo03g6Le6egzQD2GqUq4lhIoKfEVHZ4ri8MtDQXYn6XyUG8kw7u-JceGR0AthxUp2lQx6hdNfwNm0FJTqIPsKNTED_6FJ1CiHyaR8ZX-LC\"}"
            ),
            securedPaymentParams = createSecuredParams()
        )

        internal fun createAvailableCardNetworksRequest(): AvailableCardNetworksRequest = AvailableCardNetworksRequest(
            cardNumber = "4970109000000007",
            handledContracts = mutableListOf(HandledContract(cardCode = "CB", contractNumber = "CB_01"))
        )
        internal fun createAvailableCardNetworksResponse(): AvailableCardNetworksResponse = AvailableCardNetworksResponse(
            alternativeNetwork = PaymentMethodCardCode.VISA,
            alternativeNetworkCode = "2",
            defaultNetwork = PaymentMethodCardCode.CB,
            defaultNetworkCode = "1",
            selectedContractNumber = "FAKE_CONTRACT"
        )

        internal fun createSecuredParams(): SecuredPaymentParams = SecuredPaymentParams(
            pan = "4970109000000007",
            cvv = "123"
        )

        internal fun createDeviceInfo(): DeviceInfo = DeviceInfo(
            colorDepth = 32,
            containerHeight = 498.467,
            containerWidth = 750,
            javaEnabled = false,
            screenHeight = 2424,
            screenWidth = 400,
            timeZoneOffset = 0
        )

        internal fun createInternalSDKContext(): InternalSDKContext {
            val customLogger = mockk<CustomLogger>( relaxed = true)
            val internalSDKContext = InternalSDKContext(
                sdkContext = MnxtSDKContext(environment = MnxtEnvironment.Sandbox)
            )
            internalSDKContext.logger = customLogger

            return internalSDKContext;
        }

        internal fun createPaymentAttemptCB(): PaymentAttempt {
            return PaymentAttempt(
                selectedPaymentMethod = createPaymentMethodCB(),
                paymentFormData = FormData.Card(
                    paymentMethod = createPaymentMethodCB(),
                    cardNum = "4970100000000000",
                    expDate = "1230",
                    cvvNum = "123",
                    holder = "Jean michel",
                    cardNetwork = CardNetwork(PaymentMethodCardCode.CB, code = "2"),
                    saveCard = true

                ),
                selectedWallet = null,
                walletFormData = null
            )
        }

        internal fun createPaymentMethodCB(): PaymentMethod.Cards = PaymentMethod.Cards(
            paymentMethods = mutableListOf(
                PaymentMethod.CB(createPaymentMethodDataCB())
            ),
            data = createPaymentMethodDataCB()
        )

        internal fun createPaymentMethodDataCB(): PaymentMethodData = PaymentMethodData(
            cardCode = PaymentMethodCardCode.CB,
            contractNumber = "CB_01",
            disabled = false,
            hasForm = false,
            hasLogo = false,
            isIsolated = false,
            options = null,
            paymentMethodAction = null,
            additionalData = null,
            requestContext = null,
            shouldBeInTopPosition = false,
            state = null,
        )

        internal fun createDirectoryServerSdkKey(): DirectoryServerSdkKey = DirectoryServerSdkKey(scheme = "CB",
            rid = "A000000042",
            publicKey = "aaaaaa",
            rootPublicKey = "bbbbbb")

        internal fun createDirectoryServerSdkKeyResponse(): DirectoryServerSdkKeyResponse =
            DirectoryServerSdkKeyResponse(arrayOf(createDirectoryServerSdkKey()))

    }
}