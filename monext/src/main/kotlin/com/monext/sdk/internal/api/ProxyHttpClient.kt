package com.monext.sdk.internal.api

import com.monext.sdk.internal.service.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Interface pour abstraire les appels HTTP
 */
internal fun interface HttpClient {
    suspend fun execute(request: HttpRequest): HttpResponse
}


/**
 * Implémentation du HttpClient avec HttpURLConnection
 */
class ProxyHttpClient (
    private val config: HttpClientConfig,
    private val logger: Logger,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : HttpClient {

    companion object {
        private const val TAG = "ProxyHttpClient"
    }

    override suspend fun execute(request: HttpRequest): HttpResponse {
        return withRetry {
            executeRequest(request)
        }
    }

    private suspend fun executeRequest(request: HttpRequest): HttpResponse {
        return withContext(dispatcher) {
            logger.d(TAG, "Trying to call : ${request.method} ${request.url}")

            val url = getURL(request)
            val connection = (url.openConnection() as HttpsURLConnection).apply {
                requestMethod = request.method
                connectTimeout = config.connectTimeoutMs.toInt()
                readTimeout = config.readTimeoutMs.toInt()

                // Set headers
                request.headers.forEach { (key, value) ->
                    setRequestProperty(key, value)
                }

                // Set body for POST requests
                if (request.body != null && request.method == HttpMethod.POST) {
                    doOutput = true
                    outputStream.use { it.write(request.body.toByteArray()) }
                }
            }

            val statusCode = connection.responseCode
            val headers = connection.headerFields

            logger.d(TAG, "Response code: $statusCode")

            val responseBody = if (statusCode in HttpURLConnection.HTTP_OK..300) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
            }

            logger.d(TAG, "Response body: $responseBody")

            HttpResponse(statusCode, headers, responseBody)
        }
    }

    fun getURL(request: HttpRequest): URL = URL(request.url)

    private suspend fun <T> withRetry(operation: suspend () -> T): T {
        var lastException: Exception? = null

        repeat(config.retryCount) { attempt ->
            try {
                return operation()
            } catch (e: Exception) {
                lastException = e
                logger.e(TAG, "Attempt ${attempt + 1} failed", e)

                if (attempt < config.retryCount - 1) {
                    delay(config.retryDelayMs)
                }
            }
        }

        throw lastException ?: IllegalStateException("All retries failed")
    }
}


/**
 * Représentation d'une requête HTTP
 */
class HttpRequest(
    val url: String,
    val method: String,
    val headers: Map<String, String>,
    val body: String? = null
)

/**
 * Représentation d'une réponse HTTP
 */
class HttpResponse(
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: String
)

/**
 * Configuration HTTP
 */
class HttpClientConfig(
    val connectTimeoutMs: Long = 30_000,
    val readTimeoutMs: Long = 30_000,
    val retryCount: Int = 1,
    val retryDelayMs: Long = 1_000,
)

internal object HttpMethod {
    var GET = "GET"
    var POST = "POST"
}