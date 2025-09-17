import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.gradle.DokkaTask
import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.sonarqube)

    alias(libs.plugins.dokka)
    // Plugins pour la publication
    id("maven-publish")
    id("signing")
    id("jacoco")
}

android {
    namespace = "com.monext.sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Injection sécurisée de la clé API et version
        val apiKey = getApiKey()
        buildConfigField("String", "THREEDS_API_ACCESS_KEY", "\"${apiKey}\"")

        val sdkVersion = System.getenv("SDK_VERSION") ?: "1.0.0"
        buildConfigField("String", "SDK_VERSION", "\"${sdkVersion}\"")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            enableUnitTestCoverage = true       // Pour les tests unitaires
            enableAndroidTestCoverage = true    // Pour les tests d'instrumentation
        }

        // Configuration release avec obfuscation
        release {
            // ACTIVATION de la minification et obfuscation
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Pas de coverage en release
            enableUnitTestCoverage = false
            enableAndroidTestCoverage = false

            //  Signature pour les releases (CI/CD uniquement)
            if (System.getenv("KEYSTORE_PATH") != null) {
                signingConfig = signingConfigs.create("release") {
                    storeFile = file(System.getenv("KEYSTORE_PATH"))
                    storePassword = System.getenv("KEYSTORE_PASSWORD")
                    keyAlias = System.getenv("KEY_ALIAS")
                    keyPassword = System.getenv("KEY_PASSWORD")
                }
            }
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all {
                // N'exécute les tests unitaires qu'en debug
                it.enabled = it.name.contains("Debug")
            }
        }

        // Configuration pour les AndroidTests
        animationsDisabled = true  // Désactive les animations pour accélérer
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
        // AJOUT : Active BuildConfig
        buildConfig = true
    }
    sourceSets {
        getByName("main") {
            jniLibs {
                srcDirs("src\\main\\jniLibs")
            }
        }
    }

    // Configuration lint pour la CI
    lint {
        abortOnError = false
        checkReleaseBuilds = true
        // Génère un rapport HTML
        htmlReport = true
        htmlOutput = file("$buildDir/reports/lint/lint-results.html")
        // Génère un rapport XML pour SonarCloud
        xmlReport = true
        xmlOutput = file("$buildDir/reports/lint/lint-results.xml")
    }

    // Options de packaging
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Important pour Netcetera SDK
            pickFirsts += "META-INF/DEPENDENCIES"
        }
    }

    publishing {
        // Configure ce qu'Android doit exposer comme composants => Prépare le variant 'release' pour la publication
        singleVariant("release") {
//            withSourcesJar()
//            withJavadocJar()
        }
    }
}

// Désactive explicitement les tests Release
tasks {
    matching { it.name == "testReleaseUnitTest" }.configureEach {
        enabled = false
    }
}

// Fonction pour récupérer la clé API de manière sécurisée
fun getApiKey(): String {
    // 1. Variable d'environnement (CI/CD)
    System.getenv("THREEDS_API_ACCESS_KEY")?.let {
        println("✓ API key loaded from environment variable")
        return it
    }

    // 2. Fichier local.properties (développement local)
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        val properties = Properties()
        localPropertiesFile.inputStream().use {
            properties.load(it)
        }
        properties.getProperty("threeds.api.access.key")?.let {
            println("✓ API key loaded from local.properties")
            return it
        }
    }

    // 3. Valeur par défaut pour le développement
    println("⚠️ No API key found - using development placeholder")
    return "DEVELOPMENT_KEY_REPLACE_IN_PRODUCTION"
}

// Configuration pour la publication Maven
publishing {
    //  Configure comment publier sur Maven Central/GitHub
    publications {
        create<MavenPublication>("release") {
            groupId = "com.monext"
            artifactId = "payment-sdk-android"
            version = System.getenv("SDK_VERSION") ?: "1.0.0"

            afterEvaluate {
                from(components["release"])
            }

            // Métadonnées POM pour Maven Central
            pom {
                name.set("Monext Payment SDK")
                description.set("SDK Android pour l'intégration de paiements sécurisés avec 3D-Secure")
                url.set("https://github.com/monext/payment-sdk-android")

                licenses {
                    license {
                        name.set("Proprietary")
                        url.set("https://www.monext.fr/legal/sdk-license")
                    }
                }

                developers {
                    developer {
                        id.set("monext")
                        name.set("Monext Team")
                        email.set("sdk-support@monext.fr")
                        organization.set("Monext")
                        organizationUrl.set("https://www.monext.fr")
                    }
                }

                scm {
                    connection.set("scm:git:github.com/monext/payment-sdk-android.git")
                    developerConnection.set("scm:git:ssh://github.com/monext/payment-sdk-android.git")
                    url.set("https://github.com/monext/payment-sdk-android")
                }
            }
        }
    }

    repositories {
        // Maven Central (Sonatype)
        maven {
            name = "MavenCentral"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }

        // GitHub Packages
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/monext/payment-sdk-android")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }

        // Repository privé Monext (optionnel)
        // TODO : Voir plus tard pour utiliser un repo pour stocker les versions de l'APP pour les test QA
//        maven {
//            name = "MonextPrivate"
//            url = uri("https://nexus.monext.fr/repository/maven-releases/")
//            credentials {
//                username = System.getenv("NEXUS_USERNAME")
//                password = System.getenv("NEXUS_PASSWORD")
//            }
//        }
    }
}

// Configuration de la signature GPG pour Maven Central
signing {
    isRequired = System.getenv("CI") == "true" && System.getenv("SDK_VERSION") != null

    useInMemoryPgpKeys(
        System.getenv("SIGNING_KEY_ID"),
        System.getenv("SIGNING_KEY"),
        System.getenv("SIGNING_PASSWORD")
    )
    sign(publishing.publications["release"])
}

// Configuration SonarCloud
sonar {
    properties {
        property("sonar.projectKey", "Monext_monext-android-sdk")
        property("sonar.organization", "monext")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sources", "src/main/java,src/main/kotlin")
        property("sonar.tests", "src/test/java,src/androidTest/java")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml")
        property("sonar.androidLint.reportPaths", "build/reports/lint/lint-results.xml")

        // Exclusions pour ne pas analyser les fichiers générés
        property("sonar.exclusions",
            "**/BuildConfig.java," +
                    "**/*Test*.java," +
                    "**/*Test*.kt," +
                    "**/R.java," +
                    "**/build/**," +
                    "**/libs/**"
        )
    }
}

// Tâches personnalisées
tasks {
    // Génère un rapport de test unifié
    register("testReport") {
        dependsOn("test", "connectedAndroidTest")
        group = "verification"
        description = "Generates a unified test report"
    }

    // Vérifie que la clé API est configurée avant le build
    register("checkApiKey") {
        doLast {
            val apiKey = getApiKey()
            if (apiKey == "DEVELOPMENT_KEY_REPLACE_IN_PRODUCTION") {
                logger.warn("⚠️  WARNING: Using development API key. Configure a real key for production builds.")
            }
        }
    }

    // Le build dépend de la vérification de la clé
    named("preBuild") {
        dependsOn("checkApiKey")
    }

    // Configuration Dokka pour la documentation
    withType<DokkaTask>().configureEach {
        dokkaSourceSets.configureEach {
            documentedVisibilities.set(setOf(Visibility.PUBLIC))
        }
    }
}

// Configuration des tests avec coverage
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }

    // Active le coverage avec Jacoco
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = listOf("jdk.internal.*")
    }
}

// Jacoco pour le coverage
jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("test")

    reports {
        xml.required.set(true)  // Pour SonarCloud
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R\$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*"
    )

    classDirectories.setFrom(
        fileTree(mapOf(
            "dir" to "$buildDir/intermediates/javac/debug",
            "excludes" to fileFilter
        ))
    )

    sourceDirectories.setFrom("$projectDir/src/main/java", "$projectDir/src/main/kotlin")
    executionData.setFrom(fileTree(mapOf(
        "dir" to buildDir,
        "includes" to listOf("**/*.exec", "**/*.ec")
    )))
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.bouncycastle.bcprov)
    implementation(libs.compose.pay.button)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.slf4j.api)
    implementation(libs.play.services.wallet)

    // !! Attention la Lib Netcetera 2.5.3.0 est mise en Bundle du SDK pour etre buildée avec (il y a le jar et les dossiers 'jnilibs', 'res')
    // Voir la documentation : https://3dss.netcetera.com/3dssdk/doc/2.25.0/android-integration
    implementation(files("libs/netcetera-3ds-sdk-2.5.3.0-classes.jar"))

    //  Tests
    testImplementation(libs.junit.jupiter)
    testImplementation(kotlin("test"))
    testImplementation(libs.io.mockk)
    testImplementation(libs.jetbrains.kotlinx.test)
    testImplementation(libs.slf4j.api)


    debugImplementation(libs.ui.tooling)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}