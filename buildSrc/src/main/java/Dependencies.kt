import AppDependencyVersions.androidXNavigation
import AppDependencyVersions.hilt
import AppDependencyVersions.kotlin
import AppDependencyVersions.room
import org.gradle.kotlin.dsl.DependencyHandlerScope


object AppDependencyVersions {
    const val kotlin = "1.5.31"
    const val androidXNavigation = "2.3.5"
    const val room = "2.3.0"
    const val hilt = "2.38.1"
}

class AppDependencyInstaller(private val scope: DependencyHandlerScope) {

    fun installAndroidMaterial() = with(scope) {
        implementation("com.google.android.material:material:1.5.0-alpha03")
    }

    fun installKotlinLibraries() = with(scope) {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0")
    }

    fun installAndroidxCommonLibraries() = with(scope) {
        implementation("androidx.constraintlayout:constraintlayout:2.1.0")
        implementation("androidx.vectordrawable:vectordrawable:1.1.0")
        implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
        implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")
        implementation("androidx.legacy:legacy-support-v4:1.0.0")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0-beta01")
    }

    fun installAndroidxNavigationLibraries() = with(scope) {
        implementation("androidx.navigation:navigation-fragment:$androidXNavigation")
        implementation("androidx.navigation:navigation-ui:$androidXNavigation")
        implementation("androidx.navigation:navigation-fragment-ktx:$androidXNavigation")
        implementation("androidx.navigation:navigation-ui-ktx:$androidXNavigation")
    }

    fun installTestLibraries() = with(scope) {
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.3")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
        androidTestImplementation("androidx.test:rules:1.4.0")
        androidTestImplementation("androidx.test:runner:1.4.0")
    }

    fun installRoomLibraries() = with(scope) {
        implementation("androidx.room:room-runtime:$room")
        implementation("androidx.room:room-ktx:$room")
        kapt("androidx.room:room-compiler:$room")
    }

    fun installSplashscreenLibraries() = with(scope) {
        implementation("gr.pantrif:easy-android-splash-screen:0.0.1")
        implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
        implementation("androidx.viewpager2:viewpager2:1.0.0")
    }

    fun installOnboardingLibraries() = with(scope) {
        implementation("com.github.AppIntro:AppIntro:5.1.0")
    }

    fun installRecyclerViewSwipeDecorator() = with(scope) {
        implementation("com.github.xabaras:RecyclerViewSwipeDecorator:1.3")
    }

    fun installDaggerHilt() = with(scope) {
        implementation("com.google.dagger:hilt-android:$hilt")
        kapt("com.google.dagger:hilt-compiler:$hilt")
    }



}

