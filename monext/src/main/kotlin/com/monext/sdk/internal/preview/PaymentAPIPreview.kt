package com.monext.sdk.internal.preview

import com.monext.sdk.internal.threeds.response.DirectoryServerSdkKeyResponse
import com.monext.sdk.internal.api.AvailableCardNetworksRequest
import com.monext.sdk.internal.api.AvailableCardNetworksResponse
import com.monext.sdk.internal.api.PaymentAPI
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.api.model.request.PaymentRequest
import com.monext.sdk.internal.api.model.request.SecuredPaymentRequest
import com.monext.sdk.internal.api.model.request.WalletPaymentRequest
import com.monext.sdk.internal.data.sessionstate.PaymentMethodCardCode
import com.monext.sdk.internal.api.model.response.SessionState

internal object PaymentAPIPreviewSuccess: PaymentAPI {
    override suspend fun stateCurrent(sessionToken: String): SessionState = PreviewSamples.sessionStatePaymentMethodsList
    override suspend fun payment(sessionToken: String, params: PaymentRequest): SessionState = PreviewSamples.sessionStateSuccess
    override suspend fun securedPayment(sessionToken: String, params: SecuredPaymentRequest): SessionState = PreviewSamples.sessionStateSuccess
    override suspend fun walletPayment(sessionToken: String, params: WalletPaymentRequest): SessionState = PreviewSamples.sessionStateSuccess
    override suspend fun availableCardNetworks(sessionToken: String, params: AvailableCardNetworksRequest): AvailableCardNetworksResponse =
        AvailableCardNetworksResponse(
            alternativeNetwork = PaymentMethodCardCode.VISA,
            alternativeNetworkCode = "2",
            defaultNetwork = PaymentMethodCardCode.CB,
            defaultNetworkCode = "1",
            selectedContractNumber = "FAKE_CONTRACT"
        )

    override suspend fun fetchDirectoryServerSdkKeys(sessionToken: String): DirectoryServerSdkKeyResponse {
        TODO("Not yet implemented")
    }

    override fun updateContext(context: InternalSDKContext) {}
}