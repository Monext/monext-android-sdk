
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)

    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.monext.sdkexample"
    compileSdk = 36
    version = getVersionName()

    defaultConfig {
        applicationId = "com.monext.sdkexample"
        minSdk = 26
        targetSdk = 35
        versionCode = getVersionCode()
        versionName = getVersionName()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Définition du nom de l'apk
        setProperty("archivesBaseName", "monext-android-sdk-app-demo")
    }

    // Signature de l'APK
    signingConfigs {

        // Configuration Release
        create("release") {
            // En CI : utiliser le keystore de production (si configuré)
            if (System.getenv("CI") == "true") {
                //  Signature pour les releases (CI/CD uniquement)
                System.getenv("KEYSTORE_PATH")?.let {
                    storeFile = file(System.getenv("KEYSTORE_PATH"))
                    storePassword = System.getenv("KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("KEY_ALIAS")
                    keyPassword = System.getenv("KEY_PASSWORD")
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            // Assigner la signingConfig pour signer l'APK
            signingConfig = signingConfigs.getByName("release")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    applicationVariants.all {
        outputs.all {
            val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            // Récupération de la version SDK du gradle.properties
            val versionName = getVersionName() // Version courante
            val versionCode = getVersionCode() // Code du build

            // Format : monext-android-sdk-app-1.0-123-release.apk
            outputImpl.outputFileName = "monext-android-sdk-app-demo-${versionName}-${versionCode}-${name}.apk"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        jvmToolchain(21)
    }
    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

fun getVersionName(): String = (project.extra["getVersionName"] as () -> String)()
fun getVersionCode(): Int = (project.extra["getVersionCode"] as () -> Int)()

dependencies {

    implementation(project(":monext"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.junit.ktx)


    // Import the Firebase BoM
    implementation(platform(libs.firebase.bom))
    // Add the dependency for the Firebase SDK for Google Analytics
    implementation(libs.firebase.analytics)

    testImplementation(libs.junit.jupiter)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4.android)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}