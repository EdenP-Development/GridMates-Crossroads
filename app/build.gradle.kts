plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("plugin.compose")
}
val requestedQaVersion = providers.gradleProperty("crossroadsQaVersionCode").orNull?.toIntOrNull()
if (providers.gradleProperty("crossroadsQaVersionCode").isPresent) {
    require(requestedQaVersion != null && requestedQaVersion in 2..2_100_000_000) { "Invalid QA versionCode" }
}
android {
    namespace = "com.playereden.gridmates.crossroads"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.playereden.gridmates.crossroads"
        minSdk = 26
        targetSdk = 35
        versionCode = requestedQaVersion ?: 1
        versionName = "0.1.0-prototype"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        // Deliberately PUBLIC QA-only identity, pinned to the sibling Word Search test signer.
        // DO NOT use this public key for production/public releases.
        create("qaPublicTestOnly") {
            storeFile = rootProject.file("qa-signing/wordsearch-qa.p12")
            storeType = "pkcs12"
            storePassword = "android"
            keyAlias = "wordsearchQa"
            keyPassword = "android"
        }
    }
    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".qa"
            versionNameSuffix = "-qa"
            signingConfig = signingConfigs.getByName("qaPublicTestOnly")
        }
        getByName("release") {
            isMinifyEnabled = false
            // Production signing is NOT yet configured. Do not distribute release APKs.
        }
    }
    buildFeatures { compose = true; buildConfig = true }
    sourceSets.getByName("main").assets.srcDir(rootProject.file("data"))
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}
dependencies {
    implementation(project(":core"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.animation:animation")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
