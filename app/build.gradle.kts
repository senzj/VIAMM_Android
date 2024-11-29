//import org.jetbrains.kotlin.ir.backend.js.compile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.viamm"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.viamm"
        minSdk = 26
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    viewBinding{
        enable = true
    }

    buildFeatures{
        viewBinding = true
        //noinspection DataBindingWithoutKapt
        dataBinding = true
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
        mlModelBinding = true
        compose = true
    }
}

dependencies {

//    Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.support.annotations)
    implementation(libs.tensorflow.lite.support)
    implementation(libs.tensorflow.lite.metadata)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

// Custom dependencies plug in
// Retrofit for network requests
    implementation(libs.retrofit2)
    implementation(libs.converter.gson)
    implementation(libs.logging.interceptor)


    // LiveData and ViewModel for lifecycle-aware components
    implementation(libs.androidx.lifecycle.livedata.ktx.v282)
    implementation(libs.androidx.lifecycle.viewmodel.ktx.v282)

    // Navigation component for fragment and activity navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // Material Design components
    implementation(libs.material.v140)
    implementation(libs.material.v120)

    //courotine
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
}