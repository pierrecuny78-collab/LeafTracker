plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.leafvibe.leaftracker"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.leafvibe.leaftracker"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
    }
}