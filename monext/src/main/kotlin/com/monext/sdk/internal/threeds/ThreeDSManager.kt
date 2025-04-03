package com.monext.sdk.internal.threeds

import ThreeDSConfiguration
import android.content.Context
import android.util.Base64
import com.netcetera.threeds.sdk.ThreeDS2ServiceInstance
import com.netcetera.threeds.sdk.api.ThreeDS2Service
import com.netcetera.threeds.sdk.api.configparameters.ConfigParameters
import com.netcetera.threeds.sdk.api.configparameters.builder.ConfigurationBuilder
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration
import com.netcetera.threeds.sdk.api.exceptions.SDKNotInitializedException
import com.netcetera.threeds.sdk.api.ui.logic.UiCustomization
import com.monext.sdk.R
import com.monext.sdk.internal.api.PaymentAPI
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.threeds.model.SDKContextData
import com.monext.sdk.internal.exception.ThreeDsException
import com.monext.sdk.internal.exception.ThreeDsExceptionType
import org.json.JSONException
import org.json.JSONObject
import java.util.Collections

internal class ThreeDSManager (val paymentApi: PaymentAPI,
                               var internalSDKContext: InternalSDKContext,
                               var context: Context){

    lateinit var threeDS2Service : ThreeDS2Service

    var isInitialized = false

    companion object {
        const val TAG: String = "ThreeDSManager"
        val SUPPORTED_CARD_TYPE: Array<String> = arrayOf("CB", "VISA", "MASTERCARD", "AMEX")
    }

    /**
     * Fonction qui permet d'afficher les Warning du SDK 3DS
     */
    private fun loadWarnings() {
        try {
            val warnings = threeDS2Service.warnings
            for (warn in warnings) {
                internalSDKContext.logger.w(TAG, " [3DS-WARN] => [${warn.severity}] - ${warn.message}")
            }
            // Handle warnings
        } catch (e: SDKNotInitializedException) {
            // ...
            // TODO : Gérer les erreurs
        }
    }

    suspend fun initialize(sessionToken: String, cardCode: String) {

        try {
            val uiCustomization =
                ThreeDSUICustomization.createUICustomization(internalSDKContext)

            val configurationBuilder = ThreeDSConfiguration.createConfigParameters()
            completeSchemeConfiguration(configurationBuilder, cardCode, sessionToken)

            val configParameters: ConfigParameters = configurationBuilder.build()
            val locale: String = internalSDKContext.config.language
            val uiCustomizationMap: Map<UiCustomization.UiCustomizationType, UiCustomization> = mapOf(
                UiCustomization.UiCustomizationType.DEFAULT to uiCustomization,
                UiCustomization.UiCustomizationType.DARK to uiCustomization,
                UiCustomization.UiCustomizationType.MONOCHROME to uiCustomization
            )

            threeDS2Service = ThreeDS2ServiceInstance.get()
            threeDS2Service.initialize(
                context,
                configParameters,
                locale,
                uiCustomizationMap,
                object : ThreeDS2Service.InitializationCallback {
                    override fun onCompleted() {
                        isInitialized = true
                        loadWarnings()
                        internalSDKContext.logger.d(TAG, "3DSService initialized !")
                        // On affiche la conf du SDK 3DS en mode sandbox
                        if(internalSDKContext.environment.isSandbox()) {
                            displaySdkInfo()
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        internalSDKContext.logger.e(
                            TAG,
                            "3DSService initialized ERROR",
                            throwable
                        )
                        throw ThreeDsException(
                            ThreeDsExceptionType.INITIALISATION_FAILED,
                            "Unable to initialize 3DS SDK scheme for card code: $cardCode"
                        )
                    }
                })
        } catch (e: Exception) {
            // TODO : Gérer les erreurs
            internalSDKContext.logger.e(
                TAG,
                "Error when initializing : ${e.message}",
                e
            )
        }

    }

    /**
     * Fonction qui permet de récupérer la liste des schemes à partir du serveur.
     * En SANDBOX uniquement.
     */
    private suspend fun fetchSchemesFromServer(sessionToken: String) : List<SchemeConfiguration> {
        val response = paymentApi.fetchDirectoryServerSdkKeys(sessionToken)

        val schemes = mutableListOf<SchemeConfiguration>();
        for(key in response.directoryServerSdkKeyList) {

            if (SUPPORTED_CARD_TYPE.any {it.equals(key.scheme, ignoreCase = true)}) {
                val schemeLogo = getSchemeLogo(key.scheme)
                schemes.add(
                    SchemeConfiguration.newSchemeConfiguration(convertValueIfCB(key.scheme))
                        .ids(Collections.singletonList(key.rid))
                        .encryptionPublicKey(key.publicKey, null)
                        .rootPublicKey(key.rootPublicKey)
                        .logo(schemeLogo)
                        .build()
                )
            }
        }

        return schemes
    }

    /**
     * Fonction qui converti notre carType "CB" en "cartesBancaires" pour la compatibilité du SDK 3DS
     */
    private fun convertValueIfCB(value:String): String {
        return if(value.equals("CB", ignoreCase = true)) "cartesBancaires" else value
    }

    /**
     * Récupère les logos définit dans les resources du projet
     */
    private fun getSchemeLogo(cardType: String) : String {

        return when (cardType) {
            "CB", "cb" -> R.drawable.logo_cb.toString()
            "VISA", "visa"-> R.drawable.logo_visa.toString()
            "MASTERCARD", "mastercard" -> R.drawable.logo_mastercard.toString()
            "AMEX", "amex" -> R.drawable.logo_amex.toString()
            else -> throw ThreeDsException(ThreeDsExceptionType.UNSUPPORTED_NETWORK, "Unsupported card type: $cardType")
        }
    }

    private fun getSchemeForCardCode(cardType: String) : SchemeConfiguration {

        return when (cardType) {
            "CB" -> SchemeConfiguration.cbConfiguration().build()
            "VISA"-> SchemeConfiguration.visaSchemeConfiguration().build()
            "MASTERCARD" -> SchemeConfiguration.mastercardSchemeConfiguration().build()
            "AMEX" -> SchemeConfiguration.amexConfiguration().build()
            else -> throw ThreeDsException(ThreeDsExceptionType.UNSUPPORTED_NETWORK, "Unsupported card code: $cardType")
        } as SchemeConfiguration
    }

    /**
     * Fonction qui permet de récupérer les schme pour la configuration du SDK 3DS.
     * - Si Sandbox, on fait un appel server pour récupérer les Clé et intancier le bon scheme
     * - Pour la PROD tout est fait dans le SDK...
     */
    @Throws(ThreeDsException::class)
    suspend fun completeSchemeConfiguration(configurationBuilder: ConfigurationBuilder, cardType: String, sessionToken: String) {
        if (internalSDKContext.environment.isSandbox()) {
            val schemesFromServer = fetchSchemesFromServer(sessionToken)
            val valueToCheck = convertValueIfCB(cardType)
            val schemeConfiguration = schemesFromServer.find { it.schemeName == valueToCheck }
            if(schemeConfiguration == null) {
                throw ThreeDsException(ThreeDsExceptionType.UNSUPPORTED_NETWORK, "Unable to find scheme for card type: $cardType")
            }
            configurationBuilder.configureScheme(schemeConfiguration)
        } else {
            val schemeForCardCode = getSchemeForCardCode(cardType)
            configurationBuilder.configureScheme(schemeForCardCode)
        }
    }


    /**
     * Fonction qui permet de collecter les donneés de context 3DS
     */
    fun generateSDKContextData(cardType: String) : SDKContextData {
        if(!isInitialized) {
            throw ThreeDsException(ThreeDsExceptionType.NOT_INITIALISED, "Unable to generate 3DS Context")
        }

        try {
            val directoryServerID : String = getDSIdForCardType(cardType)
            val transaction = threeDS2Service.createTransaction(directoryServerID, ThreeDSConfiguration.MESSAGE_VERSION)

            val authenticationRequestParameters = transaction.authenticationRequestParameters
            val ephemPubKey =
                transformDeviceData(authenticationRequestParameters.sdkEphemeralPublicKey)

            return SDKContextData(
                deviceRenderingOptionsIF= ThreeDSConfiguration.DEFAULT_DEVICE_RENDERING_OPTIONS_IF,
                deviceRenderOptionsUI= ThreeDSConfiguration.DEFAULT_DEVICE_RENDER_OPTIONS_UI,
                maxTimeout= ThreeDSConfiguration.MAX_TIMEOUT,
                referenceNumber= authenticationRequestParameters.sdkReferenceNumber,
                ephemPubKey= ephemPubKey,
                appID= authenticationRequestParameters.sdkAppID,
                transID= authenticationRequestParameters.sdkTransactionID,
                encData= authenticationRequestParameters.deviceData
            )

        } catch (exception : Exception) {
            throw ThreeDsException(ThreeDsExceptionType.THREE_DS_KEY_ERROR, "Unable to get contextData card type: $cardType", exception)
        }

    }

    private fun getDSIdForCardType(cardType: String): String {
        val schemeConfiguration = threeDS2Service.sdkInfo.schemeConfigurations.find { it.name == convertValueIfCB(cardType) }
        if(schemeConfiguration == null) {
            throw ThreeDsException(ThreeDsExceptionType.UNSUPPORTED_NETWORK, "Unable to find scheme for card type: $cardType")
        }

        val dsId = schemeConfiguration.ids.first()
        internalSDKContext.logger.d(TAG, "Find DSId [${dsId}] for cardType: $cardType")

        return dsId
    }

    fun transformDeviceData(deviceData: String): String {
        try {
            val jwk = JSONObject(deviceData)

            val kty = jwk.optString("kty", "")
            val crv = jwk.optString("crv", "")
            val xCoord = jwk.optString("x", "")
            val yCoord = jwk.optString("y", "")

            if (kty.isEmpty() || crv.isEmpty() || xCoord.isEmpty() || yCoord.isEmpty()) {
                throw ThreeDsException(ThreeDsExceptionType.THREE_DS_KEY_ERROR, "Clés JWK manquantes (kty, crv, x, y)")
            }

            if (kty != "EC" || crv != "P-256") {
                throw ThreeDsException(ThreeDsExceptionType.THREE_DS_KEY_ERROR,"Type de clé non supporté: $kty $crv")
            }

            val xBase64 = xCoord.base64URLToBase64()
            val yBase64 = yCoord.base64URLToBase64()

            val xData = try {
                Base64.decode(xBase64, Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                throw ThreeDsException(ThreeDsExceptionType.THREE_DS_KEY_ERROR,"Impossible de décoder la coordonnée x en base64", e)
            }

            val yData = try {
                Base64.decode(yBase64, Base64.DEFAULT)
            } catch (e: IllegalArgumentException) {
                throw ThreeDsException(ThreeDsExceptionType.THREE_DS_KEY_ERROR,"Impossible de décoder la coordonnée y en base64", e)
            }

            val xFinal = Base64.encodeToString(xData, Base64.NO_WRAP)
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "")

            val yFinal = Base64.encodeToString(yData, Base64.NO_WRAP)
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "")

            return "$crv;$kty;$xFinal;$yFinal"

        } catch (e: JSONException) {
            throw ThreeDsException(ThreeDsExceptionType.THREE_DS_KEY_ERROR,"Format JSON invalide")
        }
    }

    // Extension pour convertir base64URL vers base64 standard
    private fun String.base64URLToBase64(): String {
        var base64 = this.replace("-", "+").replace("_", "/")

        // Ajouter le padding nécessaire
        when (base64.length % 4) {
            2 -> base64 += "=="
            3 -> base64 += "="
        }

        return base64
    }

    private fun displaySdkInfo() {
        internalSDKContext.logger.d(TAG, "SDK License Expiry Date: ${threeDS2Service.sdkInfo.licenseExpiryDate}")
        internalSDKContext.logger.d(TAG, "Supported Protocol Versions: ${threeDS2Service.sdkInfo.supportedProtocolVersions.joinToString(", ")}")
        for (schemeInfo in threeDS2Service.sdkInfo.schemeConfigurations) {
            internalSDKContext.logger.d(TAG, "-------------------------------------------------------")
            internalSDKContext.logger.d(TAG, "Scheme: ${schemeInfo.name}")
            internalSDKContext.logger.d(TAG, "Scheme IDs: ${schemeInfo.ids}")

            for (rootCertificate in schemeInfo.rootCertificates) {
                internalSDKContext.logger.d(TAG, "Root Certificate/Public Key: ${rootCertificate.name}")
                internalSDKContext.logger.d(TAG, "\t Valid Until: ${rootCertificate.expiryDate}")
                internalSDKContext.logger.d(TAG, "\t ${rootCertificate.certPrefix}")
            }

            internalSDKContext.logger.d(TAG, "Encryption Certificate/Public Key: ${schemeInfo.encryptionCertificate.name}")
            internalSDKContext.logger.d(TAG, "\t Key ID: ${schemeInfo.encryptionCertificateKid}")
            internalSDKContext.logger.d(TAG, "\t Valid Until: ${schemeInfo.encryptionCertificate.expiryDate}")
            internalSDKContext.logger.d(TAG, "\t ${schemeInfo.encryptionCertificate.certPrefix}")
            internalSDKContext.logger.d(TAG, "-------------------------------------------------------")
        }
    }
}