package com.monext.sdk.internal.api

import com.monext.sdk.BuildConfig.VERSION_NAME
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.api.model.request.PaymentRequest
import com.monext.sdk.internal.api.model.request.SecuredPaymentRequest
import com.monext.sdk.internal.api.model.request.WalletPaymentRequest
import com.monext.sdk.internal.api.model.response.SessionState
import com.monext.sdk.internal.data.CardNetwork
import com.monext.sdk.internal.exception.NetworkError
import com.monext.sdk.internal.threeds.model.AuthenticationResponse
import com.monext.sdk.internal.threeds.response.DirectoryServerSdkKeyResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

internal interface PaymentAPI {

    @Throws(NetworkError::class)
    suspend fun stateCurrent(sessionToken: String, merchantReturnUrl: String): SessionState

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

    @Throws(NetworkError::class)
    suspend fun isDone(sessionToken: String, cardCode: String, timestamp: Long = System.currentTimeMillis()): Boolean

    fun updateContext(context: InternalSDKContext)
}

internal class PaymentAPIImpl(
    private var internalSDKContext: InternalSDKContext,
    private var language: String,
    private var httpClient: HttpClient,
    private val httpConfig: HttpClientConfig,
    private val dispatcher: CoroutineDispatcher,
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
        internalSDKContext = context
        language = context.config.language
        httpClient = ProxyHttpClient(httpConfig, internalSDKContext, dispatcher);
    }

    /**
     * GET /token/{token}/state/current
     */
    @Throws(NetworkError::class)
    override suspend fun stateCurrent(sessionToken: String, merchantReturnUrl: String): SessionState {
        val baseUrl = buildBaseUrl()
        val url = appendPath(baseUrl, sessionToken, "state", "current")

        val uriWithQuery = URI(
            url.protocol,
            url.authority,
            url.path,
            "merchantReturnUrl=$merchantReturnUrl",
            null
        )
        val finalUrl = uriWithQuery.toURL()

        val httpRequest = buildHttpRequest(finalUrl, HttpMethod.GET)
        val sessionStateResponse = makeRequest<SessionState>(httpRequest)

        // on récupère la configuration pour le remoteLogger
        internalSDKContext.updateSendRemoteLogs(sessionStateResponse.isSendRemoteLogs == true)

        return sessionStateResponse
    }

    /**
     * POST /token/{token}/paymentRequest
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(NetworkError::class)
    override suspend fun payment(sessionToken: String, params: PaymentRequest): SessionState {
        logParameters(params)
        val baseUrl = buildBaseUrl()
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
        val baseUrl = buildBaseUrl()
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
        val baseUrl = buildBaseUrl()
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
        val baseUrl = buildBaseUrl()
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
        val baseUrl = buildBaseUrl()
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
        val baseUrl = buildBaseUrl()
        val url = appendPath(baseUrl, sessionToken, "SdkPaymentRequest")
        val httpRequest = buildHttpRequest(url, method = HttpMethod.POST, body = json.encodeToString(params))
        return makeRequest(httpRequest)
    }

    /**
     * GET /token/{token}/cardCode/(cardCode)/ActiveWaiting/isDone?timestamp=(timestamp)
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(NetworkError::class)
    override suspend fun isDone(sessionToken: String, cardCode: String, timestamp: Long): Boolean {
        val baseUrl = buildBaseUrl()

        // build path-only URL (no query)
        val urlNoQuery = appendPath(baseUrl, sessionToken, "cardCode", cardCode, "activewaiting", "isDone")

        val uriWithQuery = URI(
            urlNoQuery.protocol,
            urlNoQuery.authority,
            urlNoQuery.path,
            "timestamp=$timestamp",
            null
        )
        val finalUrl = uriWithQuery.toURL()

        val httpRequest = buildHttpRequest(finalUrl, method = HttpMethod.GET)
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
            "Origin" to internalSDKContext.environment.host,
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
            internalSDKContext.logger.e(TAG, "HTTP Error: ${response.statusCode}")

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
            internalSDKContext.logger.e(TAG, "JSON parsing error", e)
            throw NetworkError.ParseError(e)
        }
    }

    private fun buildBaseUrl(): String {
        val defaultScheme = "https"
        val environment = internalSDKContext.environment;
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
        internalSDKContext.logger.d(TAG, params.toString())
    }

    // endregion
}

@Serializable
internal data class AvailableCardNetworksResponse(
    val alternativeNetwork: String?,
    val alternativeNetworkCode: String?,
    val defaultNetwork: String?,
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
