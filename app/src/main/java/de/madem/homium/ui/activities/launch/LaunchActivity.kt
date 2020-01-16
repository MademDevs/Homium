package de.madem.homium.ui.activities.launch

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import de.madem.homium.R
import de.madem.homium.ui.activities.main.MainActivity
import de.madem.homium.ui.activities.onboarding.OnboardingActivity
import de.madem.homium.utilities.getSetting
import gr.net.maroulis.library.EasySplashScreen

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val onBoardingCompleted : Boolean = getSetting<Boolean>(resources.getString(R.string.sharedpreference_settings_preferencekey_onboardingCompleted),Boolean::class) ?: false
        val target = if(onBoardingCompleted) MainActivity::class.java else OnboardingActivity::class.java


        //using library to build splash screen
        val splashScreen = EasySplashScreen(this@LaunchActivity)
            .withFullScreen()
            .withTargetActivity(target)
            .withSplashTimeOut(2000)
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
