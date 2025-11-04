import org.jetbrains.dokka.DokkaConfiguration.Visibility
import org.jetbrains.dokka.gradle.DokkaTask
import java.util.Base64
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

    version = getVersionName()

    defaultConfig {
        minSdk = 26
        val versionName = getVersionName() // Version courante
        val versionCode = getVersionCode() // Code du build

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Injection sécurisée de la clé API et version
        val apiKey = getApiKey()
        buildConfigField("String", "THREEDS_API_ACCESS_KEY", "\"${apiKey}\"")

        // Métadonnées de build
        buildConfigField("String", "VERSION_NAME", "\"$versionName\"")
        buildConfigField("long", "VERSION_CODE", "$versionCode")
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }

        // Configuration release avec obfuscation
        release {
            // ACTIVATION de la minification et obfuscation
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            enableUnitTestCoverage = false
            enableAndroidTestCoverage = false
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
        htmlReport = true
        htmlOutput = layout.buildDirectory.file("reports/lint/lint-results.html").get().asFile
        xmlReport = true
        xmlOutput = layout.buildDirectory.file("reports/lint/lint-results.xml").get().asFile
    }

    // Options de packaging
    packaging {
        resources {
            excludes +=
                setOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "META-INF/LICENSE.md",
                    "META-INF/LICENSE-notice.md"
                )
            // Important pour Netcetera SDK
            pickFirsts += "META-INF/DEPENDENCIES"
        }
    }

    publishing {
        // Configure ce qu'Android doit exposer comme composants => Prépare le variant 'release' pour la publication
        singleVariant("release")
    }

    testOptions {
        unitTests {
            all { test ->
                // Désactive TOUS les tests unitaires pour le variant Release
                test.enabled = !test.name.contains("Release")
            }

            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true  // Désactive les animations pour accélérer
    }

    libraryVariants.all {
        outputs.all {
            val outputImpl = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val versionName = getVersionName()
            outputImpl.outputFileName = "monext-android-sdk-${versionName}-${name}.aar"
        }
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

// Fonction pour déterminer la version du SDK
fun getVersionName(): String = (project.extra["getVersionName"] as () -> String)()
fun getVersionCode(): Int = (project.extra["getVersionCode"] as () -> Int)()

// Configuration pour la publication Maven
publishing {
    //  Configure comment publier sur Maven Central/GitHub
    publications {
        create<MavenPublication>("release") {
            groupId = "com.monext"
            artifactId = "monext-android-sdk"
            version = getVersionName()

            afterEvaluate {
                from(components["release"])
            }

            // Métadonnées POM pour Maven Central
            pom {
                name.set("Monext Android SDK")
                description.set("SDK Android pour l'intégration de paiements sécurisés")
                url.set("https://github.com/Monext/monext-android-sdk")

                // TODO : A voir
//                licenses {
//                    license {
//                        name.set("Proprietary")
//                        url.set("https://www.monext.fr/legal/sdk-license")
//                    }
//                }

                developers {
                    developer {
                        id.set("monext")
                        name.set("Monext online Team")
                        email.set("acceptation.squad3@monext.net")
                        organization.set("Monext")
                        organizationUrl.set("https://www.monext.com")
                    }
                }

                scm {
                    connection.set("scm:git:github.com/Monext/monext-android-sdk.git")
                    developerConnection.set("scm:git:ssh://github.com/Monext/monext-android-sdk.git")
                    url.set("https://github.com/Monext/monext-android-sdk")
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
            url = uri("https://maven.pkg.github.com/Monext/monext-android-sdk")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

// Configuration de la signature GPG pour Maven Central
signing {
    isRequired = System.getenv("CI") == "true" && getVersionName() != "default"

    val signingKeyId = System.getenv("SIGNING_KEY_ID")
    val signingKey = System.getenv("SIGNING_KEY")
    val signingPassword = System.getenv("SIGNING_PASSWORD")

    if (signingKeyId != null && signingKey != null && signingPassword != null) {
        // Décoder la clé base64 en String
        val decodedKey = String(Base64.getDecoder().decode(signingKey))
        useInMemoryPgpKeys(signingKeyId, decodedKey, signingPassword)
    }

    sign(publishing.publications["release"])
}

// Configuration SonarCloud
sonar {
    properties {
        property("sonar.projectKey", "Monext_monext-android-sdk")
        property("sonar.projectName", "monext-android-sdk")
        property("sonar.organization", "monext")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sources", "src/main/kotlin")
        property("sonar.tests", "src/test/kotlin")
        property("sonar.java.binaries", layout.buildDirectory.dir("tmp/kotlin-classes/debug").get().asFile.absolutePath)
        property("sonar.coverage.jacoco.xmlReportPaths",
            layout.buildDirectory.file("reports/jacoco/jacocoTestReport/jacocoTestReport.xml").get().asFile.absolutePath)
        property("sonar.androidLint.reportPaths",
            layout.buildDirectory.file("reports/lint/lint-results.xml").get().asFile.absolutePath)
        property("sonar.language", "kotlin")
        property("sonar.kotlin.file.suffixes", ".kt,.kts")

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

    named("sonar") {
        dependsOn("jacocoTestReport")
    }
}

tasks.register("printVersion") {
    println(getVersionName())
}

tasks.register("printVersionCode") {
    println(getVersionCode())
}

// Configuration des tests avec coverage
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    description = "Generate Jacoco coverage report for both unit and instrumentation tests"
    group = "verification"

    // Dépend des tests unitaires ET d'instrumentation si disponibles
    /*dependsOn(tasks.matching {
        it.name in listOf("testDebugUnitTest", "createDebugCoverageReport")
    })*/
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "**/Enum$*.class",
        "**/*\$Lambda$*.*",
        "**/*\$inlined$*.*"
    )

    val debugTree = fileTree(mapOf(
        "dir" to layout.buildDirectory.dir("tmp/kotlin-classes/debug").get().asFile,
        "excludes" to fileFilter
    ))

    classDirectories.setFrom(debugTree)
    sourceDirectories.setFrom("${projectDir}/src/main/kotlin")

    executionData.setFrom(
        fileTree(layout.buildDirectory.get().asFile) {
            include("**/*.exec")  // Tests unitaires
            include("**/*.ec")    // Tests d'instrumentation
        }
    )
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.bouncycastle.bcprov)
    api(libs.compose.pay.button)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.slf4j.api)
    implementation(libs.play.services.wallet)
    implementation(libs.androidx.runtime)
    implementation(libs.androidx.appcompat)
    implementation(libs.io.coil.compose)
    implementation(libs.io.coil.network.okhttp)

    // !! Attention la Lib Netcetera 2.5.3.2 est mise en Bundle du SDK pour etre buildée avec (il y a le jar et les dossiers 'jnilibs', 'res')
    // Voir la documentation : https://3dss.netcetera.com/3dssdk/doc/2.25.0/android-integration
    implementation(files("libs/netcetera-3ds-sdk-2.5.3.2-classes.jar"))

    //  Tests
    testImplementation(libs.junit.jupiter)
    testImplementation(kotlin("test"))
    testImplementation(libs.io.mockk)
    testImplementation(libs.jetbrains.kotlinx.test)
    testImplementation(libs.slf4j.api)

    debugImplementation(libs.ui.tooling)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.ui.test.junit4.android)
    androidTestImplementation(libs.io.mockk)
    androidTestImplementation(libs.io.mockk.agent)
    androidTestImplementation(libs.io.mockk.android)
    androidTestImplementation(libs.io.coil.test)
}