plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "wiki.tk.fistarium"
    compileSdk = 36
    buildToolsVersion = "36.1.0"
    ndkVersion = "28.2.13676358"
    defaultConfig {
        applicationId = "wiki.tk.fistarium"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx)
    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    // Activity
    implementation(libs.androidx.activity.compose)
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    // Compose UI
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    // Compose Material
    implementation(libs.androidx.material3)
    // Unit Testing
    testImplementation(libs.junit)
    // Android Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    // Debug Compose
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Navigation
    implementation(libs.androidx.navigation.compose)
    // Material Icons
    implementation(libs.androidx.material.icons.extended)
    // Image Loading
    implementation(libs.glide)
    implementation(libs.coil.compose)
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson.converter)
    // Dependency Injection
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.navigation)
    // Database
    implementation(libs.bundles.local)
    ksp(libs.room.compiler)
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
    // Google Play Services
    implementation(libs.play.services.base)
    // Additional Testing
    testImplementation(libs.mockk)
    androidTestImplementation(libs.room.testing)
}