package com.monext.sdk.internal.api

import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.internal.api.model.DeviceInfo
import com.monext.sdk.internal.api.model.request.PaymentParams
import com.monext.sdk.internal.api.model.request.SecuredPaymentParams
import com.monext.sdk.internal.api.model.request.SecuredPaymentRequest
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.api.model.response.SessionStateType
import com.monext.sdk.internal.service.CustomLogger
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.Ignore
import kotlin.test.assertNotNull

// Response String JSON du WIDGET
private const val RESPONSE_CONTEXT_SUCCESS =
    "{\"token\":\"1t5Nkg8QucOkzGp219111756801475229\",\"type\":\"PAYMENT_SUCCESS\",\"creationDate\":\"Tue Sep 02 10:25:31 CEST 2025\",\"cancelUrl\":\"https://google.com?paylinetoken=1t5Nkg8QucOkzGp219111756801475229\",\"pointOfSale\":\"POS_TGE_MERCHANT_DEV2\",\"language\":\"fr\",\"returnUrl\":\"https://monext.fr?paylinetoken=1t5Nkg8QucOkzGp219111756801475229\",\"automaticRedirectAtSessionsEnd\":false,\"info\":{\"PaylineBuyerTitle\":\"M\",\"PaylineBuyerIp\":\"10.0.0.2\",\"PaylineBuyerLastName\":\"Dupont\",\"PaylineBuyerBillingAddress.street2\":\"BAstreet2\",\"PaylineOrderDate\":\"26/05/2008 10:00\",\"PaylineBuyerBillingAddress.street1\":\"BArue de marseille\",\"PaylineOrderRef\":\"TGE_cb_20250902102434\",\"PaylineFormattedOrderAmount\":\"100,00 EUR\",\"PaylineBuyerBillingAddress.name\":\"MichelB\",\"PaylineBuyerBillingAddress.country\":\"FR\",\"PaylineBuyerMobilePhone\":\"0600000000\",\"PaylineOrderDeliveryMode\":\"4\",\"PaylineMerchantCountry\":\"FR\",\"PaylineBuyerShippingAddress.cityName\":\"Aix\",\"PaylineBuyerBillingAddress.lastName\":\"MICHELLB\",\"PaylineBuyerBillingAddress.phone\":\"0600000000\",\"PaylineBuyerBillingAddress.zipCode\":\"13013\",\"PaylineBuyerShippingAddress.phone\":\"0600000000\",\"PaylineFormattedAmount\":\"100,00 EUR\",\"PaylineOrderAmountSmallestUnit\":10000,\"PaylineBuyerShippingAddress.street2\":\"SAstreet2\",\"PaylineBuyerEmail\":\"jean-claude.dupont@monext.net\",\"PaylineBuyerFirstName\":\"Jean-claude\",\"PaylineBuyerShippingAddress.country\":\"FR\",\"PaylineBuyerBillingAddress.firstName\":\"EricB\",\"PaylineBuyerShippingAddress.name\":\"Dupont\",\"PaylineBuyerBillingAddress.cityName\":\"Marseille\",\"PaylineAmountSmallestUnit\":10000,\"PaylineCurrencyDigits\":2,\"PaylineOrderDetails\":[{\"ref\":\"1\",\"price\":998,\"quantity\":1,\"comment\":\"commentaire1\",\"category\":\"1\",\"brand\":\"66999\",\"subcategory1\":\"\",\"subcategory2\":\"\",\"additionalData\":\"\"}],\"PaylineBuyerShippingAddress.street1\":\"SArue de marseille\",\"PaylineBuyerBillingAddress.countryLabel\":\"FRANCE\",\"PaylineOrderDeliveryTime\":\"1\",\"PaylineCurrencyCode\":\"EUR\",\"PaylineBuyerShippingAddress.zipCode\":\"13390\"},\"pointOfSaleAddress\":{\"address1\":\"123 rue des petits poids\",\"address2\":\"\",\"zipCode\":\"13090\",\"city\":\"Aix en Provence\"},\"isSandbox\":true,\"scripts\":[],\"paymentSuccess\":{\"ticket\":[{\"i\":false,\"k\":\"Date et heure\",\"v\":\"LE  02/09/2025 A  10:25 CEST\",\"t\":0},{\"i\":false,\"k\":\"Boutique\",\"v\":\"POS_TGE_MERCHANT_DEV2\",\"t\":0},{\"i\":true,\"k\":\"Adresse url\",\"v\":\"WWW.NOWHERE.FR\",\"t\":0},{\"i\":true,\"k\":\"Siret\",\"v\":\"12345678912345\",\"t\":0},{\"i\":false,\"k\":\"Numéro de carte\",\"v\":\" 4970 10XX XXXX XX36\",\"t\":0},{\"i\":false,\"k\":\"Terminal / Accepteur\",\"v\":\"002 27 172 649 083 798\",\"t\":0},{\"i\":true,\"k\":\"Numéro de transaction\",\"v\":\"16245082530476\",\"t\":0},{\"i\":false,\"k\":\"Type de transaction\",\"v\":\"DEBIT VADS @\",\"t\":0},{\"i\":false,\"s\":\"ticketNumAutorization\",\"k\":\"N° autorisation\",\"v\":\"A55A\",\"t\":0},{\"i\":true,\"s\":\"ticketAmount\",\"k\":\"Montant\",\"v\":\"100,00 EUR\",\"t\":0},{\"i\":true,\"k\":\"Référence\",\"v\":\"TGE_cb_20250902102434\",\"t\":0},{\"i\":true,\"s\":\"ticketTestCard\",\"v\":\"CARTE DE TEST\",\"t\":0},{\"i\":false,\"s\":\"ticketKeep\",\"v\":\"TICKET CLIENT A CONSERVER\",\"t\":0}],\"paymentCard\":\"Cartes Bancaires\",\"selectedCardCode\":\"CB\",\"selectedContractNumber\":\"CB01\",\"displayTicket\":true,\"fragmented\":false}}"
private const val RESPONSE_DIRECTORY_SERVER_SDK_KEYS =
    "{\"directoryServerSdkKeyList\":[{\"scheme\":\"CB\",\"rid\":\"A000000042\",\"publicKey\":\"publicKeyxxxxxxxCByyyy//=\",\"rootPublicKey\":\"rootPublicKeyxxxxxxxCByyyy//=\"},{\"scheme\":\"VISA\",\"rid\":\"A000000003\",\"publicKey\":\"publicKeyxxxxxxxVISAyyyy//=\",\"rootPublicKey\":\"rootPublicKeyxxxxxxxVISAyyyy//=\"},{\"scheme\":\"MASTERCARD\",\"rid\":\"A000000004\",\"publicKey\":\"publicKeyxxxxxxxMASTERCARDyyyy//=\",\"rootPublicKey\":\"rootPublicKeyxxxxxxxMASTERCARDyyyy//=\"},{\"scheme\":\"AMEX\",\"rid\":\"A000000025\",\"publicKey\":\"publicKeyxxxxxxxAMEXyyyy//=\",\"rootPublicKey\":\"rootPublicKeyxxxxxxxAMEXyyyy//=\"},{\"scheme\":\"DINERS\",\"rid\":\"A000000152\",\"publicKey\":\"publicKeyxxxxxxxDINERSyyyy//=\",\"rootPublicKey\":\"rootPublicKeyxxxxxxxDINERSyyyy//=\"}]}"

class PaymentAPIImplTest {

    private lateinit var paymentApi: PaymentAPI
    private lateinit var mockHttpClient: HttpClient
    private lateinit var mockkLogger: CustomLogger
    private val testDispatcher = StandardTestDispatcher()
    private val testEnvironment = MnxtEnvironment.Custom("test.example.com/api/v1")
    private val sessionToken = "test-session-token-123"

    // allow to capture parameter with non nullable type `Double`
    val captureHttpRequest = slot<HttpRequest>()
    val captureLog = slot<String>()

    @BeforeEach
    fun setUp() {
        // Clear any existing singleton instance
        clearAllMocks()

        mockHttpClient = mockk<HttpClient>()
        mockkLogger = mockk<CustomLogger>()

        paymentApi = spyk(PaymentAPIFactory.create(
            environment = testEnvironment,
            language = "en",
            logger = mockkLogger,
            httpClient = mockHttpClient
        ))
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun stateCurrentShouldReturnSuccessWithCustomUrl() = runTest(testDispatcher) {
        // Given
        val expectedSessionState = SessionStateType.PAYMENT_SUCCESS

        mockHttpClientResponse(responseBody = RESPONSE_CONTEXT_SUCCESS)

        // When
        val result = paymentApi.stateCurrent(sessionToken)

        // Then
        checkResponseSuccess(expectedSessionState, result)

        // On check aussi la request envoyée
        checkHttpRequest(
            url = "https://test.example.com/api/v1/services/token/test-session-token-123/state/current",
            method = "GET"
        )
    }

    @Test
    fun stateCurrentShouldReturnSuccessWithSandboxUrl() = runTest(testDispatcher) {
        // Given
        paymentApi = spyk(PaymentAPIFactory.create(
            environment = MnxtEnvironment.Sandbox,
            language = "en",
            logger = mockkLogger,
            httpClient = mockHttpClient
        ))
        val expectedSessionState = SessionStateType.PAYMENT_SUCCESS

        mockHttpClientResponse(responseBody = RESPONSE_CONTEXT_SUCCESS)

        // When
        val result = paymentApi.stateCurrent(sessionToken)

        // Then
        checkResponseSuccess(expectedSessionState, result)

        // On check aussi la request envoyée
        checkHttpRequest(
            url = "https://homologation-payment.payline.com/services/token/test-session-token-123/state/current",
            method = "GET",
            origin = "homologation-payment.payline.com"
        )
    }

    @Test
    fun stateCurrentShouldReturnSuccessWithProductionUrl() = runTest(testDispatcher) {
        // Given
        paymentApi = spyk(PaymentAPIFactory.create(
            environment = MnxtEnvironment.Production,
            language = "en",
            logger = mockkLogger,
            httpClient = mockHttpClient
        ))
        val expectedSessionState = SessionStateType.PAYMENT_SUCCESS

        mockHttpClientResponse(responseBody = RESPONSE_CONTEXT_SUCCESS)

        // When
        val result = paymentApi.stateCurrent(sessionToken)

        // Then
        checkResponseSuccess(expectedSessionState, result)

        // On check aussi la request envoyée
        checkHttpRequest(
            url = "https://payment.payline.com/services/token/test-session-token-123/state/current",
            method = "GET",
            origin = "payment.payline.com"
        )
    }

    @Test
    fun fetchDirectoryServerSdkKeys() = runTest(testDispatcher) {
        // Given
        mockHttpClientResponse(responseBody =  RESPONSE_DIRECTORY_SERVER_SDK_KEYS)

        // When
        val result = paymentApi.fetchDirectoryServerSdkKeys(sessionToken)

        // Then
        // Add assertion based on your actual SessionState structure
        assertEquals(5, result.directoryServerSdkKeyList.size)
        assertEquals("CB", result.directoryServerSdkKeyList[0].scheme)
        assertEquals("A000000042", result.directoryServerSdkKeyList[0].rid)
        assertEquals("publicKeyxxxxxxxCByyyy//=", result.directoryServerSdkKeyList[0].publicKey)
        assertEquals("rootPublicKeyxxxxxxxCByyyy//=", result.directoryServerSdkKeyList[0].rootPublicKey)

        assertEquals("VISA", result.directoryServerSdkKeyList[1].scheme)
        assertEquals("A000000003", result.directoryServerSdkKeyList[1].rid)
        assertEquals("publicKeyxxxxxxxVISAyyyy//=", result.directoryServerSdkKeyList[1].publicKey)
        assertEquals("rootPublicKeyxxxxxxxVISAyyyy//=", result.directoryServerSdkKeyList[1].rootPublicKey)

        assertEquals("MASTERCARD", result.directoryServerSdkKeyList[2].scheme)
        assertEquals("A000000004", result.directoryServerSdkKeyList[2].rid)
        assertEquals("publicKeyxxxxxxxMASTERCARDyyyy//=", result.directoryServerSdkKeyList[2].publicKey)
        assertEquals("rootPublicKeyxxxxxxxMASTERCARDyyyy//=", result.directoryServerSdkKeyList[2].rootPublicKey)

        assertEquals("AMEX", result.directoryServerSdkKeyList[3].scheme)
        assertEquals("A000000025", result.directoryServerSdkKeyList[3].rid)
        assertEquals("publicKeyxxxxxxxAMEXyyyy//=", result.directoryServerSdkKeyList[3].publicKey)
        assertEquals("rootPublicKeyxxxxxxxAMEXyyyy//=", result.directoryServerSdkKeyList[3].rootPublicKey)

        assertEquals("DINERS", result.directoryServerSdkKeyList[4].scheme)
        assertEquals("A000000152", result.directoryServerSdkKeyList[4].rid)
        assertEquals("publicKeyxxxxxxxDINERSyyyy//=", result.directoryServerSdkKeyList[4].publicKey)
        assertEquals("rootPublicKeyxxxxxxxDINERSyyyy//=", result.directoryServerSdkKeyList[4].rootPublicKey)

        // On check aussi la request envoyée
        checkHttpRequest(
            url = "https://test.example.com/api/v1/services/token/test-session-token-123/directoryServerSdkKeys",
            method = "GET"
        )
    }

    @Test
    fun securedPayment() = runTest(testDispatcher) {
        // Given
        val securedPaymentRequest = createSecuredPaymentRequestCB()
        val expectedSessionState = SessionStateType.PAYMENT_SUCCESS
        val expectedBody: String = "{\"cardCode\":\"CB\",\"contractNumber\":\"CB_01\",\"deviceInfo\":{\"colorDepth\":32,\"containerHeight\":498.467,\"containerWidth\":750,\"javaEnabled\":false,\"screenHeight\":2424,\"screenWidth\":400,\"timeZoneOffset\":0},\"isEmbeddedRedirectionAllowed\":false,\"merchantReturnUrl\":\"http://merchant.dev.com/return/url\",\"paymentParams\":{\"NETWORK\":\"2\",\"EXPI_DATE\":\"1228\",\"SAVE_PAYMENT_DATA\":false,\"HOLDER\":\"Jean-Claude\",\"SDK_CONTEXT_DATA\":\"{\\\"deviceRenderingOptionsIF\\\":\\\"01\\\",\\\"deviceRenderOptionsUI\\\":\\\"03\\\",\\\"maxTimeout\\\":60,\\\"referenceNumber\\\":\\\"3DS_LOA_SDK_NEAG_020301_00792\\\",\\\"ephemPubKey\\\":\\\"P-256;EC;3OXzA9Qt5sV8Ejqd_XXXXXXXXXXZW7Z0AnL4;VC94_YYYYYYYYYYYYYYYYY2r3g\\\",\\\"appID\\\":\\\"510000-0f48-4d7b-b00a-1b20000001\\\",\\\"transID\\\":\\\"e450000c0-2300-4000-a46f-3783b5141085\\\",\\\"encData\\\":\\\"xxxxxxxxxxxxxxxxxxxxxx.yyyyyyyyyyyyyyyyyyyy-oooooooooooooooooooooo-j9314Zwn_9LBTRvdSeI65JwHn_SB-KFTpnbZ8lFJMbfjcTgRTfoDpuU2ALLPIfE81yyUqulfYeUJ0tIVpQk6VMtwL5QfAIU2w4jZ30IlQIChCCW6OXipslYCihAAN-3g9HjY48dBQeNp-IHnMFVnnG-AxI65hgCptmJsoGAR7QrRgQ.zS1yZ6cHMPcdjvu2iw6ayA.nEN6fkOdbnCRZZSZ7UY39Qx829dGegNitt5QqSoYZ6-ZKiCCLHp2b_daHV3waQWZ4FaGMT9QLdQaX-tizhBi6tzb4yFIhWtNUDuz6_dQ-SNl-I4OIzAJURbXCkeY4gkH_rlyZdWCBykKwZ8PLBamgIKkXc9QEyvVhmxUrSdRUJ2DTJjQ2NEo_Xo90uJh62mbdvnYh8sD4-HoHPYDxoH-L-8bVdERM9ppSktuPneOvHwR5GR9Yk7RXv91emLV64QB-uWFJ-Jibg2pWjSZxTS5g-DkEzsjF3EQxIfewxdFmsmjV30G5Kzom0J3-iAfgPaRW8Ir9PCVYUSyn9p2GwN9EeU6W0Rvge4Fnz0-soDQ3DO8KU6ABueouZlE_CPHG7zJTXzOXuPXs5QFWImyXNdB_azgOFEWQLQ1071dWLmKkKCo3kAuzXTzeNqJJgHypprDmOUtpyDIaf-41_r6UVYmRXVIyQfYbW0mABFIzMD6NMLrlu4EB25g913wT_806xZK-q43N8XqBWwaRLvWhFstqEEELKImGzAB0YSP3v3z6lo1qh4q1OY53tcTpynq48MzIhXHRL4DmvxKvNLHOzjU7765GyjFv8Hm2rumoAV1Te50BZB5XKKivSBY52OBuqBWVg1KqLHzUlEx32jGqLZTyxP92AHOGBWHRlWf8EKC121311111111111111EwO5GtaT-34B9tOOHXgVruS9pl0PH8MztIwlfM3llBZo4xTGLPYNycVQ2c0qU1iAGO5YwPnhkABBTornkNQZJQK1M83cQ0HOtL-RRRew00000000000000000000eQO6Wxzkos0PRGOrJ1WYqmTkR4GFyg0g0QT8G94jSXYytPo9mzKfSYYPWPNjbD0Q64MJ8_wi9QzPzCeBR8bPj1724ppaGLz29Bjkcs2mP4w-7ZMT1G6685NTmt6HL176WQUjzlwpBOw03FnUUmG3mDRtzHF2Hccl_YWuBYWExQrUfDL-Ldjzg_D9b8qKeJ2ftVTwjZ9ih9vdYCi7L2cwc5ksJHo03g6Le6egzQD2GqUq4lhIoKfEVHZ4ri8MtDQXYn6XyUG8kw7u-JceGR0AthxUp2lQx6hdNfwNm0FJTqIPsKNTED_6FJ1CiHyaR8ZX-LC\\\"}\"},\"securedPaymentParams\":{\"PAN\":\"4970109000000007\",\"CVV\":\"123\"}}"

        mockHttpClientResponse(responseBody = RESPONSE_CONTEXT_SUCCESS)

        // When
        val result = paymentApi.securedPayment(sessionToken = sessionToken, params = securedPaymentRequest)

        // Then
        checkResponseSuccess(expectedSessionState, result)

        // On check aussi la request envoyée
        checkHttpRequest(
            url = "https://test.example.com/api/v1/services/token/test-session-token-123/securedPaymentRequest",
            method = "POST",
            body = expectedBody
        )
    }

    private fun createSecuredPaymentRequestCB(): SecuredPaymentRequest = SecuredPaymentRequest(
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
        securedPaymentParams = SecuredPaymentParams(
            pan = "4970109000000007",
            cvv = "123"
        )
    )

    private fun createDeviceInfo(): DeviceInfo = DeviceInfo(
        colorDepth = 32,
        containerHeight = 498.467,
        containerWidth = 750,
        javaEnabled = false,
        screenHeight = 2424,
        screenWidth = 400,
        timeZoneOffset = 0
    )

    @Test
    @Ignore
    fun payment() {
        // TODO
    }

    @Test
    @Ignore
    fun updateContext() {
        // TODO
    }

    @Test
    @Ignore
    fun walletPayment() {
        // TODO
    }

    @Test
    @Ignore
    fun availableCardNetworks() {
        // TODO
    }


    /**
     * Fonction qui permet de vérifier la validité du Mapping des champs pour la réponse SUCCES. Basé sur la variable : RESPONSE_CONTEXT_SUCCESS
     */
    private fun checkResponseSuccess(
        expectedSessionState: SessionStateType,
        result: SessionState
    ) {
        // Add assertion based on your actual SessionState structure
        assertEquals(expectedSessionState, result.type)
        assertEquals("1t5Nkg8QucOkzGp219111756801475229", result.token)
        assertEquals("POS_TGE_MERCHANT_DEV2", result.pointOfSale)
        assertEquals("fr", result.language)
        assertEquals(true, result.isSandbox)
        assertNotNull(result.creationDate)
        assertEquals(
            "https://google.com?paylinetoken=1t5Nkg8QucOkzGp219111756801475229",
            result.cancelUrl
        )
        assertEquals(
            "https://monext.fr?paylinetoken=1t5Nkg8QucOkzGp219111756801475229",
            result.returnUrl
        )
        assertNotNull(result.info)

        assertNotNull(result.paymentSuccess)
        assertEquals("Cartes Bancaires", result.paymentSuccess.paymentCard)
        assertEquals("CB", result.paymentSuccess.selectedCardCode)
        assertEquals("CB01", result.paymentSuccess.selectedContractNumber)
        assertEquals(true, result.paymentSuccess.displayTicket)
        assertEquals(false, result.paymentSuccess.fragmented)
        assertNotNull(result.paymentSuccess.ticket)
    }



    private fun checkHttpRequest(url: String,
                                 method: String? = "POST",
                                 body: String? = null,
                                 origin: String? = "test.example.com") {
        assertEquals(true, captureHttpRequest.isCaptured)
        assertEquals(
            url,
            captureHttpRequest.captured.url
        )
        assertEquals(method, captureHttpRequest.captured.method)
        assertEquals(body, captureHttpRequest.captured.body)
        assertNotNull(captureHttpRequest.captured.headers)
        assertEquals(
            "application/json",
            captureHttpRequest.captured.headers.getValue("Content-Type")
        )
        assertEquals("application/json", captureHttpRequest.captured.headers.getValue("Accept"))
        assertEquals("en", captureHttpRequest.captured.headers.getValue("Accept-Language"))
        assertEquals(origin, captureHttpRequest.captured.headers.getValue("Origin"))
    }

    // Helper methods for mocking HTTP responses
    private fun mockHttpClientResponse(
        responseBody: String,
        httpCode:Int = 200,
        headers: Map<String, List<String>> = emptyMap()) {

        val response = HttpResponse(
            statusCode = httpCode,
            headers = headers,
            body = responseBody,
        )

        // On mock l'appel au client
        coEvery {
            mockHttpClient.execute(request = capture(captureHttpRequest))
        } returns response

        // On capture les éventuels log (mais on bloque le spy)
        every {
            mockkLogger.d(tag = any(), message = capture(captureLog))
        } returns Unit // == Do nothing

    }
}