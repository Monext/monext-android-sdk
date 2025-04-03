package com.monext.sdk.internal.presentation.paymentmethods

import android.content.Context
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import com.monext.sdk.internal.data.PaymentMethod
import com.monext.sdk.internal.data.sessionstate.PaymentMethodData
import com.monext.sdk.internal.api.model.response.SessionState
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.Locale
import kotlin.math.max
import kotlin.math.pow

internal data class GooglePayRequestData(

    val sessionState: SessionState,

    /**
     * Changing this to ENVIRONMENT_PRODUCTION will make the API return chargeable card information.
     * Please refer to the documentation to read about the required steps needed to enable
     * ENVIRONMENT_PRODUCTION.
     *
     * @value #PAYMENTS_ENVIRONMENT
     */
    val environment: Int = WalletConstants.ENVIRONMENT_TEST
) {

    private val paymentMethods: List<PaymentMethod> =
        sessionState.paymentMethodsList?.paymentMethods ?: emptyList()

    val googlePay: PaymentMethodData? =
        paymentMethods.firstOrNull { it is PaymentMethod.GooglePay }?.data

    private val merchantIdentifier = googlePay?.requestContext?.requestData?.googlePayMerchantId ?: ""
    val merchantName = googlePay?.requestContext?.requestData?.googlePayMerchantName ?: ""
    val supportedMethods = googlePay?.requestContext?.requestData?.googlePayAllowedAuthMethod() ?: emptyList()
    val supportedNetworks: List<String> = googlePay?.requestContext?.requestData?.googlePayAllowedNetworks() ?: emptyList()

    private val sessionInfo = sessionState.info
    val countryCode = sessionInfo?.merchantCountry ?: ""
    val currencyCode = sessionInfo?.currencyCode ?: ""
    val orderReference = sessionInfo?.orderRef ?: ""

    val orderAmount: String
        get() {
            val amountSmallestUnit = sessionState.info?.amountSmallestUnit ?: 0
            val currencyDigits = sessionState.info?.currencyDigits ?: 0
            val divisor = max(10f.pow(currencyDigits), 1f)
            val adjustedAmount = amountSmallestUnit / divisor
            val formattedAmount = String.format(Locale.US, "%.2f", adjustedAmount)
            return formattedAmount
        }

    private val paymentGateway: String = "monext"

    val paymentGatewayParams: Map<String, String> = mapOf(
        "gateway" to paymentGateway,
        "gatewayMerchantId" to merchantIdentifier
    )
}

/**
 * Contains helper methods for dealing with the Payments API.
 *
 * Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
internal data class GooglePayUtil(val inputs: GooglePayRequestData) {

    /**
     * Create a Google Pay API base request object with properties used in all requests.
     *
     * @return Google Pay API base request object.
     * @throws JSONException
     */
    private val baseRequest = JSONObject()
        .put("apiVersion", 2)
        .put("apiVersionMinor", 0)

    private val gatewayTokenizationSpecification: JSONObject =
        JSONObject()
            .put("type", "PAYMENT_GATEWAY")
            .put("parameters", JSONObject(inputs.paymentGatewayParams))

    private val allowedCardNetworks = JSONArray(inputs.supportedNetworks)

    private val allowedCardAuthMethods = JSONArray(inputs.supportedMethods)

    /**
     * Describe your app's support for the CARD payment method.
     *
     *
     * The provided properties are applicable to both an IsReadyToPayRequest and a
     * PaymentDataRequest.
     *
     * @return A CARD PaymentMethod object describing accepted cards.
     * @throws JSONException
     * See [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
     */
    // Optionally, you can add billing address/phone number associated with a CARD payment method.
    private fun baseCardPaymentMethod(): JSONObject =
        JSONObject()
            .put("type", "CARD")
            .put("parameters", JSONObject()
                .put("allowedAuthMethods", allowedCardAuthMethods)
                .put("allowedCardNetworks", allowedCardNetworks)
//                .put("billingAddressRequired", true)
//                .put("billingAddressParameters", JSONObject()
//                    .put("format", "FULL")
//                )
            )

    /**
     * Describe the expected returned payment data for the CARD payment method
     *
     * @return A CARD PaymentMethod describing accepted cards and optional fields.
     * @throws JSONException
     * See [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
     */
    private val cardPaymentMethod: JSONObject = baseCardPaymentMethod()
        .put("tokenizationSpecification", gatewayTokenizationSpecification)

    val allowedPaymentMethods: JSONArray = JSONArray().put(cardPaymentMethod)

    /**
     * An object describing accepted forms of payment by your app, used to determine a viewer's
     * readiness to pay.
     *
     * @return API version and payment methods supported by the app.
     * See [IsReadyToPayRequest](https://developers.google.com/pay/api/android/reference/object.IsReadyToPayRequest)
     */
    fun isReadyToPayRequest(): JSONObject? =
        try {
            baseRequest
                .put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
        } catch (e: JSONException) {
            null
        }

    /**
     * Information about the merchant requesting payment information
     *
     * @return Information about the merchant.
     * @throws JSONException
     * See [MerchantInfo](https://developers.google.com/pay/api/android/reference/object.MerchantInfo)
     */
    private fun merchantInfo(merchantName: String): JSONObject =
        JSONObject().put("merchantName", merchantName)

    /**
     * Creates an instance of [PaymentsClient] for use in an [Context] using the
     * environment and theme set in [Constants].
     *
     * @param context from the caller activity.
     */
    fun createPaymentsClient(context: Context, walletEnvironment: Int): PaymentsClient {
        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(walletEnvironment)
            .build()

        return Wallet.getPaymentsClient(context, walletOptions)
    }

    /**
     * Provide Google Pay API with a payment amount, currency, and amount status.
     *
     * @return information about the requested payment.
     * @throws JSONException
     * See [TransactionInfo](https://developers.google.com/pay/api/android/reference/object.TransactionInfo)
     */
    private fun getTransactionInfo(price: String): JSONObject =
        JSONObject()
            .put("totalPrice", price)
            .put("totalPriceStatus", "FINAL")
            .put("countryCode", inputs.countryCode)
            .put("currencyCode", inputs.currencyCode)

    /**
     * An object describing information requested in a Google Pay payment sheet
     *
     * @return Payment data expected by your app.
     * See [PaymentDataRequest](https://developers.google.com/pay/api/android/reference/object.PaymentDataRequest)
     */
    fun getPaymentDataRequest(priceLabel: String): JSONObject =
        baseRequest
            .put("allowedPaymentMethods", allowedPaymentMethods)
            .put("transactionInfo", getTransactionInfo(priceLabel))
            .put("merchantInfo", merchantInfo(inputs.merchantName))
//            .put("shippingAddressRequired", true)
//            .put("shippingAddressParameters", JSONObject()
//                .put("phoneNumberRequired", false)
//                .put("allowedCountryCodes", JSONArray(listOf("FR", "US")))
//            )
}