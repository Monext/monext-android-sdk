package com.monext.sdk.internal.threeds

import ThreeDSConfiguration
import android.app.Activity
import android.content.Context
import com.monext.sdk.Appearance
import com.monext.sdk.R
import com.monext.sdk.internal.api.PaymentAPI
import com.monext.sdk.internal.api.configuration.InternalSDKContext
import com.monext.sdk.internal.exception.ThreeDsException
import com.monext.sdk.internal.exception.ThreeDsExceptionType
import com.monext.sdk.internal.threeds.model.ChallengeUseCaseCallback
import com.monext.sdk.internal.threeds.model.SdkChallengeData
import com.monext.sdk.internal.threeds.model.SdkContextData
import com.netcetera.threeds.sdk.ThreeDS2ServiceInstance
import com.netcetera.threeds.sdk.api.ThreeDS2Service
import com.netcetera.threeds.sdk.api.configparameters.ConfigParameters
import com.netcetera.threeds.sdk.api.configparameters.builder.ConfigurationBuilder
import com.netcetera.threeds.sdk.api.configparameters.builder.SchemeConfiguration
import com.netcetera.threeds.sdk.api.exceptions.SDKNotInitializedException
import com.netcetera.threeds.sdk.api.transaction.Transaction
import com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeParameters
import com.netcetera.threeds.sdk.api.transaction.challenge.ChallengeStatusReceiver
import com.netcetera.threeds.sdk.api.ui.logic.UiCustomization
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.json.JSONException
import java.util.Base64


internal class ThreeDSManager (val paymentApi: PaymentAPI,
                               var internalSDKContext: InternalSDKContext,
                               var context: Context,
                               val threeDS2ServiceProvided : ThreeDS2Service? = null,  // Injection du Service pour les Tests... Pas trouvé d'autre solution...
                               val threeDSBusiness: ThreeDSBusiness = ThreeDSBusiness()) {

    internal var threeDS2Service : ThreeDS2Service? = null
    internal var currentOnGoingThreeDsTransaction: Transaction? = null
    internal var isInitialized = false

    companion object {
        const val TAG: String = "ThreeDSManager"
        val SUPPORTED_CARD_TYPE: Array<String> = arrayOf("CB", "VISA", "MASTERCARD", "AMEX")
    }

    /**
     * Focntion qui permet d'initialiser la configuration du SDK 3DS
     */
    internal suspend fun startInitialize(sessionToken: String, cardCode: String) {
        try {
            val uiCustomization = ThreeDSUICustomization.createUICustomization(internalSDKContext)

            val configurationBuilder = threeDSBusiness.createConfigParameters()
            completeSchemeConfiguration(configurationBuilder, cardCode, sessionToken)

            val configParameters: ConfigParameters = configurationBuilder.build()
            val locale: String = internalSDKContext.config.language
            val uiCustomizationMap: Map<UiCustomization.UiCustomizationType, UiCustomization> = mapOf(
                UiCustomization.UiCustomizationType.DEFAULT to uiCustomization,
                UiCustomization.UiCustomizationType.DARK to uiCustomization,
                UiCustomization.UiCustomizationType.MONOCHROME to uiCustomization
            )

            threeDS2Service = getThreeDS2ServiceInstance()
            threeDS2Service!!.initialize(
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

    internal fun getThreeDS2ServiceInstance(): ThreeDS2Service {
        // Si le service est fournit pour les Tests, on le prends, sinon on récupère une instace via 'ThreeDS2ServiceInstance'
        return threeDS2ServiceProvided ?: ThreeDS2ServiceInstance.get()
    }

    /**
     * Fonction qui permet de collecter les donneés de context 3DS pour les envoyer côté Serveur
     */
    internal fun generateSDKContextData(cardType: String) : SdkContextData {
        if(!isInitialized) {
            throw ThreeDsException(ThreeDsExceptionType.NOT_INITIALISED, "Unable to generate 3DS Context")
        }

        try {
            val directoryServerID : String = getDSIdForCardType(cardType)
            currentOnGoingThreeDsTransaction = threeDS2Service!!.createTransaction(directoryServerID, ThreeDSConfiguration.MESSAGE_VERSION)

            // Récupération des données du SDK 3DS
            val authenticationRequestParameters = currentOnGoingThreeDsTransaction!!.authenticationRequestParameters
            val ephemPubKey = transformDeviceData(authenticationRequestParameters.sdkEphemeralPublicKey)

            return SdkContextData(
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

    /**
     * Fonction qui permet de déclencher le Challenge Flow
     */
    internal suspend fun doChallengeFlow(activity: Activity,
                                         sdkChallengeData: SdkChallengeData,
                                         theme: Appearance,
                                         useCaseCallback: ChallengeUseCaseCallback) {
        try {
            if(!isInitialized || currentOnGoingThreeDsTransaction == null) {
                throw ThreeDsException(ThreeDsExceptionType.NOT_INITIALISED, "Unable to start 3DS Challenge Flow")
            }

            val challengeParameters : ChallengeParameters = sdkChallengeData.toSdkChallengeParameters()
            val challengeStatusReceiver : ChallengeStatusReceiver =
                CustomChallengeStatusReceiver(
                    logger = internalSDKContext.logger,
                    sdkChallengeData = sdkChallengeData,
                    useCaseCallback = useCaseCallback)

            val timeOut = 10
            val progressView = currentOnGoingThreeDsTransaction!!.getProgressView(activity)
            progressView.showProgress()
            delay(2000L)

            currentOnGoingThreeDsTransaction!!.doChallenge(activity, challengeParameters, challengeStatusReceiver, timeOut);

        } catch (exception : Exception) {
            internalSDKContext.logger.e(TAG,"Unable to start 3DS Challenge Flow with threeDSServerTransID : $sdkChallengeData.threeDSServerTransID", exception)
            // On appelle quand meme le service de paiement pour générer une TRS KO
            useCaseCallback.onChallengeCompletion(sdkChallengeData.toAuthenticationResponse())
        }
    }

    /**
     * Close and cleanup 3DS context
     */
    internal fun closeTransaction() {
        try {
            if (currentOnGoingThreeDsTransaction != null) {
                currentOnGoingThreeDsTransaction!!.close()
                currentOnGoingThreeDsTransaction = null
            }

            if (threeDS2Service != null) {
                threeDS2Service!!.cleanup(context)
            }
        } catch (exception : Exception) {
            // Possible Exception lors du clean si le sdk n'est pas initialisé, mais ce n'est pas grave ici : SDKNotInitializedException
            internalSDKContext.logger.e(TAG,"closeTransaction", exception)
        }
    }

    /**
     * Fonction qui permet de récupérer les schme pour la configuration du SDK 3DS.
     * - Si Sandbox, on fait un appel server pour récupérer les Clé et intancier le bon scheme
     * - Pour la PROD tout est fait dans le SDK...
     */
    @Throws(ThreeDsException::class)
    private suspend fun completeSchemeConfiguration(configurationBuilder: ConfigurationBuilder, cardType: String, sessionToken: String) {
        if (internalSDKContext.environment.isSandbox()) {
            val schemesFromServer = fetchSchemesFromServer(sessionToken)
            val valueToCheck = threeDSBusiness.convertCardTypeValueToSchemeValue(cardType)
            val schemeConfiguration = schemesFromServer.find { it.schemeName.equals(valueToCheck, ignoreCase = true) }
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
     * Fonction qui permet de récupérer la liste des schemes à partir du serveur.
     * En SANDBOX uniquement.
     */
    private suspend fun fetchSchemesFromServer(sessionToken: String) : List<SchemeConfiguration> {
        val response = paymentApi.fetchDirectoryServerSdkKeys(sessionToken)

        val schemes = mutableListOf<SchemeConfiguration>();
        for(key in response.directoryServerSdkKeyList) {

            if (SUPPORTED_CARD_TYPE.any {it.equals(key.scheme, ignoreCase = true)}) {
                val schemeLogo = getSchemeLogo(key.scheme)
                schemes.add(threeDSBusiness.createSchemeConfiguration(key, schemeLogo))
            }
        }

        return schemes
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

    private fun getDSIdForCardType(cardType: String): String {
        val schemeConfiguration = threeDS2Service!!.sdkInfo.schemeConfigurations.find { it.name.equals(threeDSBusiness.convertCardTypeValueToSchemeValue(cardType), ignoreCase = true) }
        if(schemeConfiguration == null) {
            throw ThreeDsException(ThreeDsExceptionType.UNSUPPORTED_NETWORK, "Unable to find scheme for card type: $cardType")
        }

        val dsId = schemeConfiguration.ids.first()
        internalSDKContext.logger.d(TAG, "Find DSId [${dsId}] for cardType: $cardType")

        return dsId
    }

    @JvmRecord
    @Serializable
    data class EphemeralKey(val kty: String, val crv: String, val x: String, val y: String)

    private fun transformDeviceData(deviceData: String): String {
        try {
            val jwk = Json.decodeFromString<EphemeralKey>(deviceData)

            val kty = jwk.kty
            val crv = jwk.crv
            val xCoord = jwk.x
            val yCoord = jwk.y

            if (kty.isEmpty() || crv.isEmpty() || xCoord.isEmpty() || yCoord.isEmpty()) {
                throw ThreeDsException(ThreeDsExceptionType.THREE_DS_KEY_ERROR, "Clés JWK manquantes (kty, crv, x, y)")
            }

            if (kty != "EC" || crv != "P-256") {
                throw ThreeDsException(ThreeDsExceptionType.THREE_DS_KEY_ERROR,"Type de clé non supporté: $kty $crv")
            }

            val xBase64 = xCoord.base64URLToBase64()
            val yBase64 = yCoord.base64URLToBase64()

            val xData = try {
                Base64.getDecoder().decode(xBase64)
            } catch (e: IllegalArgumentException) {
                throw ThreeDsException(ThreeDsExceptionType.THREE_DS_KEY_ERROR,"Impossible de décoder la coordonnée x en base64", e)
            }

            val yData = try {
                Base64.getDecoder().decode(yBase64)
            } catch (e: IllegalArgumentException) {
                throw ThreeDsException(ThreeDsExceptionType.THREE_DS_KEY_ERROR,"Impossible de décoder la coordonnée y en base64", e)
            }

            val xFinal = Base64.getEncoder().encodeToString(xData)
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "")

            val yFinal = Base64.getEncoder().encodeToString(yData)
                .replace("+", "-")
                .replace("/", "_")
                .replace("=", "")

            return "$crv;$kty;$xFinal;$yFinal"

        } catch (exception: JSONException) {
            throw ThreeDsException(ThreeDsExceptionType.THREE_DS_KEY_ERROR,"Format JSON invalide", cause = exception)
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

    internal fun displaySdkInfo() {
        internalSDKContext.logger.d(TAG, "SDK License Expiry Date: ${threeDS2Service!!.sdkInfo.licenseExpiryDate}")
        internalSDKContext.logger.d(TAG, "Supported Protocol Versions: ${threeDS2Service!!.sdkInfo.supportedProtocolVersions.joinToString(", ")}")
        for (schemeInfo in threeDS2Service!!.sdkInfo.schemeConfigurations) {
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

    /**
     * Fonction qui permet d'afficher les Warning du SDK 3DS
     */
    private fun loadWarnings() {
        try {
            val warnings = threeDS2Service!!.warnings
            for (warn in warnings) {
                internalSDKContext.logger.w(TAG, " [3DS-WARN] => [${warn.severity}] - ${warn.message}")
            }
            // Handle warnings
        } catch (e: SDKNotInitializedException) {
            // Pas d'erreur ici ...
        }
    }
}