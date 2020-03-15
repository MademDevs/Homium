package de.madem.homium.ui.activities.onboarding


import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import de.madem.homium.R
import de.madem.homium.application.HomiumSettings
import de.madem.homium.constants.*
import de.madem.homium.managers.DatabaseInitializer
import de.madem.homium.ui.activities.main.MainActivity
import de.madem.homium.ui.fragments.onboarding.*
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.getSetting
import de.madem.homium.utilities.extensions.putSetting
import de.madem.homium.utilities.extensions.switchToActivity

class OnboardingActivity : AppIntro() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //not initialized database = app runs for the first time
        val dataBaseInitialized : Boolean = getSetting<Boolean>(
            SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_DATABASE_INITIALIZED,Boolean::class) ?: false
        val onBoardingCompleted : Boolean = getSetting<Boolean>(
            SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_ONBOARDING_COMPLETED,Boolean::class) ?: false

        if(!dataBaseInitialized){
            //init database
            DatabaseInitializer(applicationContext) {
                println("INFO FOR DEVELOPPERS: DATABASE INITIALIZED!")
                putSetting(SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_DATABASE_INITIALIZED,true)
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
        putSetting(SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_ONBOARDING_COMPLETED,true)
        goToMain()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        putSetting(SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_ONBOARDING_COMPLETED,true)
        goToMain()
    }

    //init app settings
    private fun initAppSettings(){
        //TODO: maybe later taking application context for this but i am not sure, because last time there were some stackoverflowerrors xD
        CoroutineBackgroundTask<Unit>()
            .executeInBackground {
            this@OnboardingActivity.putSetting(
                SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_VIBRATION_ENABLED,true)
                HomiumSettings.vibrationEnabled = true
            this@OnboardingActivity.putSetting(
                SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_SORT,
                SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_NORMAL)
                HomiumSettings.shoppingSort = SHARED_PREFERENCE_SETTING_VALUE_SHOPPING_SORT_NORMAL
            this@OnboardingActivity.putSetting(
                SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_DELETE_QUESTION_SPEECH_ASSISTENT_ALLOWED,true)
                HomiumSettings.speechAssistantDeleteQuestion = true
            this@OnboardingActivity.putSetting(
                SHAREDPREFERENCE_SETTINGS_PREFERENCEKEY_SHOPPING_TO_INVENTORY,R.id.radio_check_question)
                HomiumSettings.shoppingToInventory = R.id.radio_check_question
        }.onDone { println("SETTINGS INITIALIZED") }.start()
    }
}