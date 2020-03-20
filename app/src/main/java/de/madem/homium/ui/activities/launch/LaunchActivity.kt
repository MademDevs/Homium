package de.madem.homium.ui.activities.launch

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import de.madem.homium.R
import de.madem.homium.application.HomiumSettings
import de.madem.homium.constants.SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_APP_THEME
import de.madem.homium.constants.SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_ONBOARDING_COMPLETED
import de.madem.homium.ui.activities.main.MainActivity
import de.madem.homium.ui.activities.onboarding.OnboardingActivity
import de.madem.homium.utilities.extensions.getSetting
import gr.net.maroulis.library.EasySplashScreen

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //launch destination
        val onBoardingCompleted : Boolean = getSetting<Boolean>(
            SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_ONBOARDING_COMPLETED,Boolean::class) ?: false
        val target = if(onBoardingCompleted) MainActivity::class.java else OnboardingActivity::class.java

        //app theme
        val theme : Int  = HomiumSettings.appTheme
        AppCompatDelegate.setDefaultNightMode(theme)

        //using library to build splash screen
        val splashScreen = EasySplashScreen(this@LaunchActivity)
            .withFullScreen()
            .withTargetActivity(target)
            .withSplashTimeOut(500)
            .withBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .withLogo(R.drawable.logo_svg)
            .withAfterLogoText(resources.getString(R.string.app_name))

        with(splashScreen){
            afterLogoTextView.setTextColor(Color.WHITE)
            afterLogoTextView.textSize = 24f
        }

        //creating splash screen and setting it as layout of this activity
        setContentView(splashScreen.create())
    }
}
