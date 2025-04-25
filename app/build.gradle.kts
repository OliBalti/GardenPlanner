plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.gardenplanner"
    compileSdk = 34 // Changed from 35 to stable 34

    defaultConfig {
        applicationId = "com.example.gardenplanner"
        minSdk = 24
        targetSdk = 34 // Target SDK aligned with compileSdk
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Core AndroidX libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)

    // Material Design Components
    implementation(libs.material)

    // Jetpack Lifecycle components
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Jetpack Navigation components
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // --- Room Persistence Library --- <-- Added Here
    // --- Room Persistence Library (using Version Catalog) ---
    implementation(libs.androidx.room.runtime)  // Uses alias from libs.versions.toml
    implementation(libs.androidx.room.ktx)      // Uses alias from libs.versions.toml
    kapt(libs.androidx.room.compiler)           // Uses alias from libs.versions.toml
    // --- End Room ---

    // Material Calendar View
    implementation(libs.material.calendarview)

    // Unit Testing
    testImplementation(libs.junit)

    // Android Instrumentation Testing
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}