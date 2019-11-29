package de.madem.homium.ui.activities.onboarding


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.github.paolorotolo.appintro.model.SliderPagerBuilder
import de.madem.homium.R
import de.madem.homium.managers.DatabaseInitializer
import de.madem.homium.ui.activities.main.MainActivity
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.getSetting
import de.madem.homium.utilities.putSetting
import de.madem.homium.utilities.switchToActivity

class OnboardingActivity : AppIntro() {

    //private lateinit var manager : PreferencesManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //manager = PreferencesManager(this)
        //not initialized database = app runs for the first time
        val dataBaseInitialized : Boolean = getSetting<Boolean>(resources.getString(R.string.sharedpreference_settings_preferencekey_databaseInitialized),Boolean::class) ?: false

        if(!dataBaseInitialized){
            //init database
            DatabaseInitializer(applicationContext) {
                println("INFO FOR DEVELOPPERS: DATABASE INITIALIZED!")
                putSetting(resources.getString(R.string.sharedpreference_settings_preferencekey_databaseInitialized),true)
            }

            initAppSettings()

            showIntroSlides()

        }
        else{
            goToMain()
        }

    }

    private fun showIntroSlides() {
        //manager.setFirstRun()

        val pageOne = SliderPagerBuilder()
            .title("Willkommen zu Homium")
            .description(getString(R.string.onboarding_welcome))
            .imageDrawable(R.drawable.onboarding_logo)
            .bgColor(ContextCompat.getColor(this,R.color.colorPrimary))//Color.parseColor("#176A93")
            .build()

        val pageTwo = SliderPagerBuilder()
            .title("So verwendest du die Einkaufsliste")
            .description(getString(R.string.onboarding_shopping))
            .imageDrawable(R.drawable.onboarding_shoppingcart)
            .bgColor(ContextCompat.getColor(this,R.color.colorPrimary))
            .build()

        val pageThree = SliderPagerBuilder()
            .title("Coming soon ;)")
            .description(getString(R.string.onboarding_moreFeatures))
            .imageDrawable(R.drawable.onboarding_comingsoon)
            .bgColor(ContextCompat.getColor(this,R.color.colorPrimary))
            .build()

//Können uns hier noch die Übergangsanimation aussuchen ;)
        setFadeAnimation()
        //setZoomAnimation()
        //setFlowAnimation()
        //setSlideOverAnimation()
        //setDepthAnimation()

        addSlide(AppIntro2Fragment.newInstance(pageOne))
        addSlide(AppIntro2Fragment.newInstance(pageTwo))
        addSlide(AppIntro2Fragment.newInstance(pageThree))
    }

    private fun goToMain() {
        switchToActivity(MainActivity::class)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        goToMain()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        goToMain()
    }

    //init app settings
    private fun initAppSettings(){
        //TODO: maybe later taking application context for this but i am not sure, because last time there were some stackoverflowerrors xD
        CoroutineBackgroundTask<Unit>().executeInBackground {
            this@OnboardingActivity.putSetting(resources.getString(R.string.sharedpreference_settings_preferencekey_vibrationEnabled),true)
        }.onDone { println("SETTING INITIALIZED") }.start()
    }
}