package de.madem.homium.ui.activities.onboarding


import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.github.paolorotolo.appintro.model.SliderPagerBuilder
import de.madem.homium.R
import de.madem.homium.managers.DatabaseInitializer
import de.madem.homium.ui.activities.main.MainActivity
import de.madem.homium.ui.fragments.onboarding.OnboardingSlideComingSoon
import de.madem.homium.ui.fragments.onboarding.OnboardingSlideShopping
import de.madem.homium.ui.fragments.onboarding.OnboardingSlideWelcome
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.getSetting
import de.madem.homium.utilities.putSetting
import de.madem.homium.utilities.switchToActivity

class OnboardingActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //val textSlide11 = findViewById<TextView>(R.id.text_slide1) as TextView
        //textSlide11.movementMethod = ScrollingMovementMethod()

        //not initialized database = app runs for the first time
        val dataBaseInitialized : Boolean = getSetting<Boolean>(resources.getString(R.string.sharedpreference_settings_preferencekey_databaseInitialized),Boolean::class) ?: false
        val onBoardingCompleted : Boolean = getSetting<Boolean>(resources.getString(R.string.sharedpreference_settings_preferencekey_onboardingCompleted),Boolean::class) ?: false

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

            if(onBoardingCompleted){
                goToMain()
            }
            else{
                showIntroSlides()
            }

        }

    }

    private fun showIntroSlides() {

        /*
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

         */

//Können uns hier noch die Übergangsanimation aussuchen ;)
        setFadeAnimation()
        //setZoomAnimation()
        //setFlowAnimation()
        //setSlideOverAnimation()
        //setDepthAnimation()

        addSlide(OnboardingSlideWelcome())
        addSlide(OnboardingSlideShopping())
        addSlide(OnboardingSlideComingSoon())

        //addSlide(AppIntro2Fragment.newInstance(pageOne))
        //addSlide(AppIntro2Fragment.newInstance(pageTwo))
        //addSlide(AppIntro2Fragment.newInstance(pageThree))
    }

    private fun goToMain() {
        switchToActivity(MainActivity::class)
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        putSetting(resources.getString(R.string.sharedpreference_settings_preferencekey_onboardingCompleted),true)
        goToMain()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        putSetting(resources.getString(R.string.sharedpreference_settings_preferencekey_onboardingCompleted),true)
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