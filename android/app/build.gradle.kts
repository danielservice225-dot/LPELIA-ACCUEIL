plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ci.lpelia.accueil"
    compileSdk = 35
    defaultConfig {
        applicationId = "ci.lpelia.accueil"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }
}
