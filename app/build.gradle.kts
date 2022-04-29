plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
    //room apply for kotlin
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
}


android {
    compileSdk = 31
    defaultConfig {
        applicationId = "de.madem.homium"
        minSdk = 21
        targetSdk= 31
        versionCode = 5
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
    buildFeatures {
        dataBinding = true
    }
}

dependencies {
    AppDependencyInstaller(this).apply {
        installKotlinLibraries()
        installRoomLibraries()
        installAndroidMaterial()
        installAndroidxCommonLibraries()
        installAndroidxNavigationLibraries()
        installTestLibraries()
        installOnboardingLibraries()
        installSplashscreenLibraries()
        installRecyclerViewSwipeDecorator()
        installDaggerHilt()
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
