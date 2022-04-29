// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.1.3")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${AppDependencyVersions.kotlin}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${AppDependencyVersions.hilt}")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        maven(url = "https://jitpack.io")
        jcenter()
    }
}

//task clean(type: Delete) {
//    delete rootProject.buildDir
//}