import com.monext.sdk.BuildConfig
import com.netcetera.threeds.sdk.api.configparameters.builder.ConfigurationBuilder
import kotlinx.serialization.Serializable

/**
 * Classe permettant de gérer la configuration du SDK 3DS de Netcetera
 */
@Serializable
internal class ThreeDSConfiguration {

    companion object {
        const val API_KEY:String = BuildConfig.THREEDS_API_ACCESS_KEY
        const val MESSAGE_VERSION:String = "2.2.0"
        const val MAX_TIMEOUT:Int = 60
        const val DEFAULT_DEVICE_RENDERING_OPTIONS_IF:String = "01" // TODO: on verra avec la spec EMVCo ce qu'on met par défaut
        const val DEFAULT_DEVICE_RENDER_OPTIONS_UI:String = "03" // TODO: on verra avec la spec EMVCo ce qu'on met par défaut

        fun createConfigParameters(): ConfigurationBuilder {
            val configParameters: ConfigurationBuilder = ConfigurationBuilder()
                .apiKey(API_KEY)
            return configParameters;
        }
    }
}
