
plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
    id("kotlin-android-extensions")
    //room apply for kotlin
    id("kotlin-kapt")
}


android {
    compileSdkVersion(31)
    defaultConfig {
        applicationId = "de.madem.homium"
        minSdkVersion(21)
        targetSdkVersion(31)
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
    implementation("com.google.android.material:material:1.5.0-alpha03")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("androidx.vectordrawable:vectordrawable:1.1.0")
    implementation("androidx.navigation:navigation-fragment:2.3.5")
    implementation("androidx.navigation:navigation-ui:2.3.5")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0-beta01")

    implementation("com.github.xabaras:RecyclerViewSwipeDecorator:1.3")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.test:rules:1.4.0")
    androidTestImplementation("androidx.test:runner:1.4.0")
    //room(database) dependencies
    implementation("androidx.room:room-runtime:2.3.0")
    kapt("androidx.room:room-compiler:2.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    //Onboarding
    implementation("com.github.AppIntro:AppIntro:5.1.0")
    //Splash Screen
    implementation("gr.pantrif:easy-android-splash-screen:0.0.1")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
}

kapt {
    arguments {
        arg("key", "value")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}
