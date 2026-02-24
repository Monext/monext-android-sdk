package com.monext.sdk.internal.service

import android.os.Build
import android.util.Log
import com.monext.sdk.BuildConfig
import com.monext.sdk.BuildConfig.VERSION_NAME
import com.monext.sdk.MnxtEnvironment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Implémentation Android du Logger.
 * Logguer qui utilise le logger Android.
 * Sur les logs de niveau ERROR, envoie également un rapport au serveur distant (Widget).
 *
 * @param context  Contexte Android (utilisé pour récupérer les infos de l'application).
 * @param remoteErrorUrl  URL de la servlet de réception des logs d'erreur.
 *                        Si null, le reporting distant est désactivé.
 */
class CustomLogger(
    private val environment: MnxtEnvironment,
    private val isSendRemoteLogs: Boolean = false, // False par défaut, sera modifier lors de récupération de la reponse du Widget.
    private val appVersionName: String = "${VERSION_NAME}",
    private val appVersionCode: String = "${BuildConfig.VERSION_CODE}",
    private val reportingScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    // Implémentation réelle par défaut, mockée en test
    internal val connectionFactory: ConnectionFactory = ConnectionFactory { url ->
        URL(url).openConnection() as HttpURLConnection
    }
) : Logger {

    fun interface ConnectionFactory {
        fun open(url: String): HttpURLConnection
    }

    companion object {
        private const val TAG = "CustomLogger"
        private const val CONNECT_TIMEOUT_MS = 2_000
        private const val READ_TIMEOUT_MS = 2_000

        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
            encodeDefaults = true
        }

    }

    // ------------------------------------------------------------------
    // Interface Logger
    // ------------------------------------------------------------------
    override fun d(tag: String, message: String) {
        Log.d(tag, message)
    }

    override fun i(tag: String, message: String) {
        Log.i(tag, message)
    }

    override fun w(tag: String, message: String, throwable: Throwable?) {
        Log.w(tag, message, throwable)
    }

    override fun e(tag: String, message: String, throwable: Throwable?) {
        Log.e(tag, message, throwable)
        if (isSendRemoteLogs) {
            sendRemoteErrorReport(tag, message, throwable)
        }
    }

    /**
     * Envoie un rapport d'erreur au serveur de manière asynchrone.
     *
     * En cas d'échec de l'envoi, on log uniquement en warn Android
     * (jamais en error, pour éviter une boucle infinie).
     */
    private fun sendRemoteErrorReport(tag: String, message: String, throwable: Throwable?) {
        val url = buildBaseUrl()

        reportingScope.launch {
            runCatching {
                val message = buildMessage(tag, message, throwable,
                    "$appVersionName($appVersionCode)"
                )

                val logPayload = LogPayload(
                    logger = "SDK Android $appVersionName",
                    timestamp = System.currentTimeMillis(),
                    level = "ERROR",
                    url = url,
                    message = json.encodeToString(message),
                    token = ""
                )

                postForm(url, logPayload)
            }.onFailure { networkError ->
                // NE PAS appeler e() ici → boucle infinie assurée
                Log.w(TAG, "[RemoteLogger]- Remote error reporting failed: ${networkError.message}")
            }
        }
    }

    private fun buildBaseUrl(): String {
        val defaultScheme = "https"

        var cleanPath = ""
        if (environment.path.isNotEmpty()) {
            cleanPath = if (environment.path.startsWith("/")) environment.path else "/$environment.path"
        }

        val fullServicePath = "${cleanPath}/log"
        return URI(defaultScheme, environment.host, fullServicePath, null).toString()
    }

    /**
     * Construit le body
     */
    private fun buildMessage(
        tag: String,
        message: String,
        throwable: Throwable?,
        appVersion: String
    ): String {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            .format(Date())

        val stackTrace = throwable?.stackTraceToString()?.jsonEscape()
        val exceptionType = throwable?.javaClass?.name?.jsonEscape()
        val exceptionMessage = throwable?.message?.jsonEscape()

        return buildString {
            append("{")
            appendJsonField("timestamp", timestamp)
            append(",")
            appendJsonField("tag", tag.jsonEscape())
            append(",")
            appendJsonField("message", message.jsonEscape())
            append(",")
            // Infos device
            append("\"device\":{")
            appendJsonField("manufacturer", (Build.MANUFACTURER ?: "unknown").jsonEscape())
            append(",")
            appendJsonField("model", (Build.MODEL ?: "unknown").jsonEscape())
            append(",")
            appendJsonField("androidVersion", (Build.VERSION.RELEASE ?: "unknown").jsonEscape())
            append(",")
            appendJsonField("sdkInt", Build.VERSION.SDK_INT.toString(), quoted = false)
            append(",")
            appendJsonField("appVersion", appVersion)
            append("}")
            // Exception (optionnelle)
            if (throwable != null) {
                append(",")
                append("\"exception\":{")
                appendJsonField("type", exceptionType ?: "")
                append(",")
                appendJsonField("message", exceptionMessage ?: "")
                append(",")
                appendJsonField("stackTrace", stackTrace ?: "")
                append("}")
            }
            append("}")
        }
    }

    // Helper d'encodage URL
    private fun String.urlEncode(): String =
        java.net.URLEncoder.encode(this, "UTF-8")

    /**
     * Effectue le POST HTTP avec HttpURLConnection.
     * Timeout courts : on ne veut pas bloquer le thread IO indéfiniment.
     */
    private fun postForm(urlString: String, body: LogPayload) {
        val connection = connectionFactory.open(urlString).apply {
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("X-Widget-SDK", "Android $VERSION_NAME")
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
        }

        try {
            // On crée un tableau avec le json
            val bodyToString = "[" + json.encodeToString(body) + "]";
            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write("data=" + bodyToString.urlEncode())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                Log.w(TAG, "[RemoteLogger]- Remote error reporting returned HTTP $responseCode")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "[RemoteLogger]- Remote error reporting ", e)
        }  finally {
            connection.disconnect()
        }
    }

    @Serializable
    internal data class LogPayload(
        val logger: String,
        val timestamp: Long,
        val level: String,
        val url: String?,
        val message: String,
        val token: String
    )

    /** Échappe les caractères spéciaux JSON dans une String. */
    private fun String.jsonEscape(): String = this
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")

    private fun StringBuilder.appendJsonField(
        key: String,
        value: String,
        quoted: Boolean = true
    ) {
        append("\"$key\":")
        if (quoted) append("\"$value\"") else append(value)
    }
}