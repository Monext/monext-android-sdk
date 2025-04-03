package com.monext.sdk.internal.api

import com.monext.sdk.MnxtEnvironment
import com.monext.sdk.internal.preview.PaymentAPIPreviewSuccess
import com.monext.sdk.internal.service.CustomLogger
import com.monext.sdk.internal.service.Logger
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


internal class PaymentAPIFactory {

    companion object {
        fun create(
            environment: MnxtEnvironment,
            language: String,
            logger: Logger = CustomLogger(),
            httpConfig: HttpClientConfig = HttpClientConfig(),
            dispatcher: CoroutineDispatcher = Dispatchers.IO,
            httpClient: HttpClient = ProxyHttpClient(httpConfig, logger, dispatcher),
            isLocalInspectionMode: Boolean = false
        ): PaymentAPI {

            return if(isLocalInspectionMode) {
                // Utilisé dans la preview des composants
                PaymentAPIPreviewSuccess
            } else {
                PaymentAPIImpl(
                    environment = environment,
                    language = language,
                    httpClient = httpClient,
                    logger = logger
                )
            }
        }
    }
}
