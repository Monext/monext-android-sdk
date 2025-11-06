// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.sonarqube) apply false

    // Add the dependency for the Google services Gradle plugin
    alias(libs.plugins.google.services) apply false

    alias(libs.plugins.jreleaser) apply false
}

allprojects {
    extra["getVersionName"] = fun(): String {
        // Priorité :
        // 1. Variable d'environnement (CI/CD)
        // 2. Propriété gradle.properties ou -P
        // 3. Valeur par défaut
        return System.getenv("VERSION_NAME")
            ?: project.findProperty("version") as? String
            ?: "default"
    }


    // Fonction pour récupérer la version du build
    extra["getVersionCode"] = fun(): Int {
        // GitHub Actions run number ou timestamp pour local
        return System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()
            ?: (System.currentTimeMillis() / 1000).toInt()
    }
}
