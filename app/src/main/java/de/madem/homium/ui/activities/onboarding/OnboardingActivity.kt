package de.madem.homium.ui.activities.onboarding


import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import de.madem.homium.R
import de.madem.homium.managers.DatabaseInitializer
import de.madem.homium.ui.activities.main.MainActivity
import de.madem.homium.ui.fragments.onboarding.*
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.getSetting
import de.madem.homium.utilities.putSetting
import de.madem.homium.utilities.switchToActivity

class OnboardingActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        setFadeAnimation()
        //setZoomAnimation()
        //setFlowAnimation()
        //setSlideOverAnimation()
        //setDepthAnimation()

        addSlide(OnboardingSlideWelcome())
        addSlide(OnboardingSlideShopping())
        addSlide(OnboardingSlideInventory())
        addSlide(OnboardingSlideRecipe())
        addSlide(OnboardingSlideSpeachAssistant())
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
            this@OnboardingActivity.putSetting(resources.getString(R.string.sharedpreference_settings_preferencekey_sortedShoppingRadioId),R.id.radio_sort_normal)
            this@OnboardingActivity.putSetting(resources.getString(R.string.sharedpreference_settings_preferencekey_deleteQuestionSpeechAssistentAllowed),true)
            this@OnboardingActivity.putSetting(resources.getString(R.string.sharedpreference_settings_preferencekey_shoppingToInventory),R.id.radio_check_question)
        }.onDone { println("SETTINGS INITIALIZED") }.start()
    }
}