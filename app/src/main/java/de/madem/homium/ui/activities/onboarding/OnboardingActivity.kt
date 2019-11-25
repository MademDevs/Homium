package de.madem.homium.ui.activities.onboarding


import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.github.paolorotolo.appintro.model.SliderPagerBuilder
import de.madem.homium.R
import de.madem.homium.ui.activities.main.MainActivity

class OnboardingActivity : AppIntro() {

    private lateinit var manager : PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        manager = PreferencesManager(this)


//Kommentare hier löschen, damit das das Intro nur 1x startet
        //if (manager.isFirstRun()){
        showIntroSlides()
         //} else {
         //   goToMain()
         //}
    }

    private fun showIntroSlides() {
        manager.setFirstRun()

        val pageOne = SliderPagerBuilder()
            .title("Willkommen zu Homeium")
            .description(getString(R.string.onboarding_welcome))
            .imageDrawable(R.drawable.onboarding_logo)
            .bgColor(Color.parseColor("#176A93"))
            .build()

        val pageTwo = SliderPagerBuilder()
            .title("So verwendest du die Einkaufsliste")
            .description(getString(R.string.onboarding_shopping))
            .imageDrawable(R.drawable.onboarding_shoppingcart)
            .bgColor(Color.parseColor("#176A93"))
            .build()

        val pageThree = SliderPagerBuilder()
            .title("Coming soon ;)")
            .description(getString(R.string.onboarding_moreFeatures))
            .imageDrawable(R.drawable.onboarding_comingsoon)
            .bgColor(Color.parseColor("#176A93"))
            .build()

//Können uns hier noch die Übergangsanimation aussuchen ;)
        //setFadeAnimation()
        //setZoomAnimation()
        //setFlowAnimation()
        //setSlideOverAnimation()
        setDepthAnimation()

        addSlide(AppIntro2Fragment.newInstance(pageOne))
        addSlide(AppIntro2Fragment.newInstance(pageTwo))
        addSlide(AppIntro2Fragment.newInstance(pageThree))
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        goToMain()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        goToMain()
    }
}