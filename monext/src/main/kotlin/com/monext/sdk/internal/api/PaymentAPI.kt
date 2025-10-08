package com.monext.sdk.internal.api

import com.monext.sdk.BuildConfig.VERSION_NAME
import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.api.model.request.PaymentRequest
import com.monext.sdk.internal.api.model.request.SecuredPaymentRequest
import com.monext.sdk.internal.api.model.request.WalletPaymentRequest
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.data.CardNetwork
import com.monext.sdk.internal.data.sessionstate.PaymentMethodCardCode
import com.monext.sdk.internal.exception.NetworkError
import com.monext.sdk.internal.service.Logger
import com.monext.sdk.internal.threeds.model.AuthenticationResponse
import com.monext.sdk.internal.threeds.response.DirectoryServerSdkKeyResponse
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

internal interface PaymentAPI {

    @Throws(NetworkError::class)
    suspend fun stateCurrent(sessionToken: String): SessionState

    @Throws(NetworkError::class)
    suspend fun payment(sessionToken: String, params: PaymentRequest): SessionState

    @Throws(NetworkError::class)
    suspend fun securedPayment(sessionToken: String, params: SecuredPaymentRequest): SessionState

    @Throws(NetworkError::class)
    suspend fun walletPayment(sessionToken: String, params: WalletPaymentRequest): SessionState

    @Throws(NetworkError::class)
    suspend fun availableCardNetworks(sessionToken: String, params: AvailableCardNetworksRequest): AvailableCardNetworksResponse

    @Throws(NetworkError::class)
    suspend fun fetchDirectoryServerSdkKeys(sessionToken: String): DirectoryServerSdkKeyResponse

    @Throws(NetworkError::class)
    suspend fun sdkPaymentRequest(sessionToken: String, params: AuthenticationResponse): SessionState

    fun updateContext(context: InternalSDKContext)
}

internal class PaymentAPIImpl(
    private var environment: MnxtEnvironment,
    private var language: String,
    private var logger: Logger,
    private val httpClient: HttpClient,
): PaymentAPI {

    companion object {

        private const val TAG = "PaymentAPIImpl"
        private const val DEFAULT_MASKED_PAN = "XXXX XXXX XXXX XXXX"

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
            encodeDefaults = true
        }
    }

    override fun updateContext(context: InternalSDKContext) {
        environment = context.environment
        language = context.config.language
        logger = context.logger
    }

    /**
     * GET /token/{token}/state/current
     */
    @Throws(NetworkError::class)
    override suspend fun stateCurrent(sessionToken: String): SessionState {
        val baseUrl = buildBaseUrl(environment)
        val url = appendPath(baseUrl, sessionToken, "state", "current")
        val httpRequest = buildHttpRequest(url, HttpMethod.GET)
        return makeRequest(httpRequest)
    }

    /**
     * POST /token/{token}/paymentRequest
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(NetworkError::class)
    override suspend fun payment(sessionToken: String, params: PaymentRequest): SessionState {
        logParameters(params)
        val baseUrl = buildBaseUrl(environment)
        val url = appendPath(baseUrl, sessionToken, "paymentRequest")
        val httpRequest = buildHttpRequest(url, method = HttpMethod.POST, body = json.encodeToString(params))
        return makeRequest(httpRequest)
    }

    /**
     * POST /token/{token}/securedPaymentRequest
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(NetworkError::class)
    override suspend fun securedPayment(sessionToken: String, params: SecuredPaymentRequest): SessionState {
        logParameters(params)
        val baseUrl = buildBaseUrl(environment)
        val url = appendPath(baseUrl, sessionToken, "securedPaymentRequest")
        val httpRequest = buildHttpRequest(url, method = HttpMethod.POST, body = json.encodeToString(params))
        return makeRequest(httpRequest)
    }

    /**
     * POST /token/{token}/walletPaymentRequest
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(NetworkError::class)
    override suspend fun walletPayment(sessionToken: String, params: WalletPaymentRequest): SessionState {
        logParameters(params)
        val baseUrl = buildBaseUrl(environment)
        val url = appendPath(baseUrl, sessionToken, "walletPaymentRequest")
        val httpRequest = buildHttpRequest(url, method = HttpMethod.POST, body = json.encodeToString(params))
        return makeRequest(httpRequest)
    }

    /**
     * POST /token/{token}/availablecardnetworks
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(NetworkError::class)
    override suspend fun availableCardNetworks(sessionToken: String, params: AvailableCardNetworksRequest): AvailableCardNetworksResponse {
        logParameters(params)
        val baseUrl = buildBaseUrl(environment)
        val url = appendPath(baseUrl, sessionToken, "availablecardnetworks")
        val httpRequest = buildHttpRequest(url, method = HttpMethod.POST, body = json.encodeToString(params))
        return makeRequest(httpRequest)
    }

    /**
     * POST /token/{token}/directoryServerSdkKeys
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(NetworkError::class)
    override suspend fun fetchDirectoryServerSdkKeys(sessionToken: String): DirectoryServerSdkKeyResponse {
        val baseUrl = buildBaseUrl(environment)
        val url = appendPath(baseUrl, sessionToken, "directoryServerSdkKeys")
        val httpRequest = buildHttpRequest(url, method = HttpMethod.GET)
        return makeRequest(httpRequest)
    }

    /**
     * POST /token/{token}/SdkPaymentRequest
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(NetworkError::class)
    override suspend fun sdkPaymentRequest(sessionToken: String, params: AuthenticationResponse): SessionState {
        val baseUrl = buildBaseUrl(environment)
        val url = appendPath(baseUrl, sessionToken, "SdkPaymentRequest")
        val httpRequest = buildHttpRequest(url, method = HttpMethod.POST, body = json.encodeToString(params))
        return makeRequest(httpRequest)
    }

    // region Internal

    /**
     * Construit les headers HTTP standard
     */
    private fun buildHttpRequest(url: URL, method: String, body: String? = null): HttpRequest {
        return HttpRequest(
            url = url.toString(),
            method = method,
            headers = buildHeaders(),
            body = body
        )
    }

    /**
     * Construit les headers HTTP standard
     */
    private fun buildHeaders(): Map<String, String> {
        return mapOf(
            "Content-Type" to "application/json",
            "Accept" to "application/json",
            "Accept-Language" to language,
            "Origin" to environment.host,
            "X-Widget-SDK" to "Android $VERSION_NAME"
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    private suspend inline fun <reified R> makeRequest(request: HttpRequest): R {
        val httpResponse = httpClient.execute(request)

        return handleResponse(httpResponse)
    }

    /**
     * Gère la réponse HTTP et les erreurs
     */
    @Throws(NetworkError::class)
    private inline fun <reified R> handleResponse(response: HttpResponse): R {
        if (response.statusCode !in 200..299) {
            logger.e(TAG, "HTTP Error: ${response.statusCode}")

            when (response.statusCode) {
                HttpURLConnection.HTTP_BAD_REQUEST -> throw NetworkError.BadRequest()
                HttpURLConnection.HTTP_UNAUTHORIZED -> throw NetworkError.Unauthorized()
                HttpURLConnection.HTTP_PAYMENT_REQUIRED -> throw NetworkError.PaymentRequired()
                HttpURLConnection.HTTP_FORBIDDEN -> throw NetworkError.Forbidden()
                HttpURLConnection.HTTP_NOT_FOUND -> throw NetworkError.NotFound()
                HttpURLConnection.HTTP_ENTITY_TOO_LARGE -> throw NetworkError.RequestEntityTooLarge()
                422 -> throw NetworkError.UnprocessableEntity()
                else -> throw NetworkError.Http(response.statusCode)
            }
        }

        return try {
            json.decodeFromString<R>(response.body)
        } catch (e: Exception) {
            logger.e(TAG, "JSON parsing error", e)
            throw NetworkError.ParseError(e)
        }
    }

    private fun buildBaseUrl(environment: MnxtEnvironment): String {
        val defaultScheme = "https"

        var cleanPath = ""
        if (environment.path.isNotEmpty()) {
            cleanPath = if (environment.path.startsWith("/")) environment.path else "/$environment.path"
        }

        val fullServicePath = "${cleanPath}/services/token"
        return URI(defaultScheme, environment.host, fullServicePath, null).toString()
    }

    private fun appendPath(baseUrl: String, vararg pathSegments: String): URL {
        val baseUri = URI(baseUrl)
        val pathBuilder = StringBuilder(baseUri.path ?: "")

        pathSegments.forEach { segment ->
            if (!pathBuilder.endsWith("/")) {
                pathBuilder.append("/")
            }
            pathBuilder.append(segment.trim('/'))
        }

        return URI(baseUri.scheme, baseUri.authority, pathBuilder.toString(), null).toURL()
    }

    private fun logParameters(params: SecuredPaymentRequest) {
        val securedPaymentParams = params.securedPaymentParams.copy(pan = DEFAULT_MASKED_PAN)
        val copy = params.copy(securedPaymentParams = securedPaymentParams)
        logParameters(copy as Any)
    }

    private fun logParameters(params: WalletPaymentRequest) {
        val securedPaymentParams = params.securedPaymentParams.copy(pan = DEFAULT_MASKED_PAN)
        val copy = params.copy(securedPaymentParams = securedPaymentParams)
        logParameters(copy as Any)
    }

    private fun logParameters(params: AvailableCardNetworksRequest) {
        val copy = params.copy(cardNumber = DEFAULT_MASKED_PAN)
        logParameters(copy as Any)
    }

    private fun logParameters(params: Any) {
        logger.d(TAG, params.toString())
    }

    // endregion
}

@Serializable
internal data class AvailableCardNetworksResponse(
    val alternativeNetwork: PaymentMethodCardCode?,
    val alternativeNetworkCode: String?,
    val defaultNetwork: PaymentMethodCardCode?,
    val defaultNetworkCode: String?,
    val selectedContractNumber: String?
) {

    val defaultCardNetwork: CardNetwork?
        get() {
            defaultNetwork ?: return null
            defaultNetworkCode ?: return null
            return CardNetwork(defaultNetwork, defaultNetworkCode)
        }

    val altCardNetwork: CardNetwork?
        get() {
            alternativeNetwork ?: return null
            alternativeNetworkCode ?: return null
            return CardNetwork(alternativeNetwork, alternativeNetworkCode)
        }
}
