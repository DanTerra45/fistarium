@file:Suppress("UnstableApiUsage")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.serialization)
}

val isBuildingBundle = project.gradle.startParameter.taskNames.any {
    it.contains("bundle") || it.contains("buildAllRelease")
}

android {
    namespace = "wiki.tk.fistarium"
    compileSdk = 36
    buildToolsVersion = "36.1.0"
    ndkVersion = "29.0.14206865"
    defaultConfig {
        applicationId = "wiki.tk.fistarium"
        minSdk = 28
        targetSdk = 36
        versionCode = 2
        versionName = "0.0.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: error("KEYSTORE_PATH environment variable not set"))
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: error("KEYSTORE_PASSWORD environment variable not set")
            keyAlias = System.getenv("KEYSTORE_KEY_ALIAS") ?: error("KEYSTORE_KEY_ALIAS environment variable not set")
            keyPassword = System.getenv("KEYSTORE_KEY_PASSWORD") ?: error("KEYSTORE_KEY_PASSWORD environment variable not set")
            storeType = "PKCS12"
        }
    }
    buildTypes {
        debug {
            isDebuggable = true
            isJniDebuggable = true
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isJniDebuggable = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
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
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE*"
            excludes += "META-INF/NOTICE*"
        }
    }
    splits {
        abi {
            isEnable = !isBuildingBundle
            reset()
            include("arm64-v8a", "x86_64")
            isUniversalApk = true
        }
    }
}

tasks.register("buildAllRelease") {
    group = "Build"
    description = "Generates APKs (splits and universal) and the App Bundle (AAB) for release."
    dependsOn("assembleRelease")
    dependsOn("bundleRelease")
    doLast {
        println("APKs: build/outputs/apk/release/")
        println("AABs: build/outputs/bundle/release/")
    }
}

dependencies {
    // PRESENTATION LAYER (UI & State)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Compose Core
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Images
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.svg)
    
    // Paging UI
    implementation(libs.androidx.paging.compose)

    // DATA LAYER (Networking & Storage)
    // Networking Bundle
    implementation(libs.bundles.networking)

    // Room Database (Local Cache)
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    
    // Paging Data Support
    implementation(libs.androidx.paging.runtime)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Firebase Cloud
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.config)
    implementation(libs.firebase.analytics)
    implementation(libs.play.services.base)

    // DI & UTILS (Infrastructure)
    // Dependency Injection
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.navigation)

    // Utilities
    implementation(libs.timber)

    // TESTING
    testImplementation(libs.junit)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.room.testing)
    
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}