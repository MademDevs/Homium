package de.madem.homium.ui.activities.recipe

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.github.paolorotolo.appintro.model.SliderPagerBuilder
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.RecipeDao
import de.madem.homium.models.Recipe
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.showToastShort


class PresentationMode: AppIntro() {

    private lateinit var recipe: Recipe
    private lateinit var dao: RecipeDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = AppDatabase.getInstance().recipeDao()
        val recipeId = intent.extras?.getInt("recipeId")
        if(recipeId != null) {
            CoroutineBackgroundTask<Recipe>()
                .executeInBackground { dao.getRecipeById(recipeId) }
                .onDone { recipe = it; showIntroSlides() }
                .start()
        }
    }

    private fun showIntroSlides() {

        println(recipe)

        val pageOne = SliderPagerBuilder()
            .title("Willkommen zu Homium")
            .description(getString(R.string.onboarding_welcome))
            .imageDrawable(R.drawable.onboarding_logo)
            .bgColor(ContextCompat.getColor(this, R.color.colorPrimary))//Color.parseColor("#176A93")
            .build()

        val pageTwo = SliderPagerBuilder()
            .title("So verwendest du die Einkaufsliste")
            .description(getString(R.string.onboarding_shopping))
            .imageDrawable(R.drawable.onboarding_shoppingcart)
            .bgColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .build()

        val pageThree = SliderPagerBuilder()
            .title("Coming soon ;)")
            .description(getString(R.string.onboarding_moreFeatures))
            .imageDrawable(R.drawable.onboarding_comingsoon)
            .bgColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .build()

        setFadeAnimation()

        addSlide(AppIntro2Fragment.newInstance(pageOne))
        addSlide(AppIntro2Fragment.newInstance(pageTwo))
        addSlide(AppIntro2Fragment.newInstance(pageThree))
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }

}