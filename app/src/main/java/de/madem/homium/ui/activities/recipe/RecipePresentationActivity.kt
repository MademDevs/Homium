package de.madem.homium.ui.activities.recipe

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import de.madem.homium.R
import de.madem.homium.constants.REQUEST_CODE_EDIT_RECIPE_FROM_PRESENTATION
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databinding.ActivityRecipePresentationBinding
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.setPictureFromPath
import de.madem.homium.utilities.switchToActivityForResult

class RecipePresentationActivity : AppCompatActivity(){

    //quelle: https://github.com/saulmm/CoordinatorExamples

    private var recipeid = -1
    private lateinit var recipe: Recipe
    private lateinit var description: List<RecipeDescription>
    private lateinit var ingredients: List<RecipeIngredient>

    private lateinit var binding: ActivityRecipePresentationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_recipe_presentation
        )

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.presentation_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if(id == R.id.presentation_toolbar_edit) {
            switchToActivityForResult(REQUEST_CODE_EDIT_RECIPE_FROM_PRESENTATION, RecipeEditActivity::class) {
                it.putExtra("recipe", recipeid)
            }
            return true
        }
        if(id == R.id.presentation_toolbar_cook) {
            println("Cooking function called")
            return true
        }
        if(id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initGuiElements() = with(binding) {
        ivBanner.setPictureFromPath(recipe.image)
        viewpager.adapter = TabsAdapter(supportFragmentManager, description, ingredients)
        layoutTab.setupWithViewPager(viewpager)
        tvHeaderFirstline.text = recipe.name
    }

    class TabsAdapter(
        fm: FragmentManager?,
        private val description: List<RecipeDescription>,
        private val ingredients: List<RecipeIngredient>
    ) : FragmentStatePagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getCount(): Int {
            return 1 + description.size
        }

        override fun getItem(position: Int): Fragment {
            return if(position == 0) {
                var text = ""
                for(el in ingredients) {
                    text += "${el.count} ${el.unit} ${el.name} \n"
                }
                RecipePresentationStepFragment(text)
            } else {
                RecipePresentationStepFragment(description[position-1].description)
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return when (position) {
                0 -> "Zutaten"
                else -> "Schritt $position"
            }
        }

    }


}