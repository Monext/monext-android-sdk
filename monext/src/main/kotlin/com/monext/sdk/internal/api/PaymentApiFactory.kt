package com.monext.sdk.internal.api

import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.preview.PaymentAPIPreviewSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers


internal class PaymentAPIFactory {

    companion object {
        fun create(
            internalSDKContext: InternalSDKContext,
            language: String,
            httpConfig: HttpClientConfig = HttpClientConfig(),
            dispatcher: CoroutineDispatcher = Dispatchers.IO,
            httpClient: HttpClient = ProxyHttpClient(httpConfig, internalSDKContext, dispatcher),
            isLocalInspectionMode: Boolean = false
        ): PaymentAPI {

            return if(isLocalInspectionMode) {
                // Utilisé dans la preview des composants
                PaymentAPIPreviewSuccess
            } else {
                PaymentAPIImpl(
                    internalSDKContext =  internalSDKContext,
                    language = language,
                    httpClient = httpClient,
                    httpConfig = httpConfig,
                    dispatcher = dispatcher
                )
            }
        }
    }
}
