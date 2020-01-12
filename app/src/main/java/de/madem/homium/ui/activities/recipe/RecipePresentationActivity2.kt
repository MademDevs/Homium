package de.madem.homium.ui.activities.recipe

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.FakePageFragment
import de.madem.homium.utilities.setPictureFromPath

class RecipePresentationActivity2 : AppCompatActivity(){

    //quelle: https://github.com/saulmm/CoordinatorExamples

    private var recipeid = -1
    private lateinit var recipe: Recipe
    private lateinit var description: List<RecipeDescription>
    private lateinit var ingredients: List<RecipeIngredient>

    private lateinit var image: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_presentationx)

        val tabLayout = findViewById<View>(R.id.materialup_tabs) as TabLayout

        val viewPager = findViewById<View>(R.id.materialup_viewpager) as ViewPager

        val appbarLayout = findViewById<View>(R.id.materialup_appbar) as AppBarLayout

        image = findViewById(R.id.materialup_profile_backdrop)

        viewPager.adapter = TabsAdapter(supportFragmentManager)
        tabLayout.setupWithViewPager(viewPager)

        loadRecipe()
    }

    private fun loadRecipe() {
        recipeid = intent.getIntExtra(resources.getString(R.string.data_transfer_intent_edit_recipe_id), -1)
        if(recipeid > 0) {
            //nesting coroutines to avaid not initialized properties -> also possible with await?

            val op2 = CoroutineBackgroundTask<List<RecipeDescription>>()
                .executeInBackground { AppDatabase.getInstance().recipeDao().getDescriptionByRecipeId(recipeid) }
                .onDone { description = it; initGuiElements() }

            val op1 = CoroutineBackgroundTask<List<RecipeIngredient>>()
                .executeInBackground { AppDatabase.getInstance().recipeDao().getIngredientByRecipeId(recipeid) }
                .onDone { ingredients = it; op2.start() }

            CoroutineBackgroundTask<Recipe>()
                .executeInBackground { AppDatabase.getInstance().recipeDao().getRecipeById(recipeid) }
                .onDone { recipe = it; op1.start() }
                .start()
        }
    }

    private fun initGuiElements() {
        image.setPictureFromPath(recipe.image)
    }

    class TabsAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm!!) {
        override fun getCount(): Int {
            return Companion.TAB_COUNT
        }

        override fun getItem(i: Int): Fragment {
            return FakePageFragment.newInstance()
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return "Tab $position"
        }

        companion object {
            private const val TAB_COUNT = 2
        }
    }


}