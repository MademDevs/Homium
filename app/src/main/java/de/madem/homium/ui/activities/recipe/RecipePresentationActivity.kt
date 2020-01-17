package de.madem.homium.ui.activities.recipe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import de.madem.homium.R
import de.madem.homium.constants.REQUEST_CODE_COOK_RECIPE
import de.madem.homium.constants.REQUEST_CODE_EDIT_RECIPE_FROM_PRESENTATION
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databinding.ActivityRecipePresentationBinding
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.utilities.CookingAssistant
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.notNull
import de.madem.homium.utilities.extensions.setPictureFromPath
import de.madem.homium.utilities.extensions.switchToActivityForResult
import java.lang.ref.WeakReference

class RecipePresentationActivity : AppCompatActivity() {

    private var recipeid = -1
    private var recipe: Recipe? = null
    private lateinit var description: List<RecipeDescription>
    private lateinit var ingredients: List<RecipeIngredient>
    private lateinit var cookingAssistant : CookingAssistant
    private var allowedToAutoStartCooking : Boolean = false

    private lateinit var binding: ActivityRecipePresentationBinding

    companion object{
        private const val SAVEINSTANCESTATE_KEY_ALLOWED_TO_AUTOSTART_COOKING = "autostartcookingpermission"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_recipe_presentation
        )

        cookingAssistant = CookingAssistant(WeakReference<Context>(this))

        loadRecipe(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SAVEINSTANCESTATE_KEY_ALLOWED_TO_AUTOSTART_COOKING,allowedToAutoStartCooking)

    }

    private fun loadRecipe(savedInstanceState: Bundle?) {
        recipeid = intent.getIntExtra(
            resources.getString(R.string.data_transfer_intent_edit_recipe_id),
            -1
        )
        if (recipeid > 0) {
            //nesting coroutines to avaid not initialized properties -> also possible with await?

            val op2 = CoroutineBackgroundTask<List<RecipeDescription>>()
                .executeInBackground {
                    AppDatabase.getInstance().recipeDao().getDescriptionByRecipeId(recipeid)
                }
                .onDone {
                    description = it;
                    initGuiElements()
                    autoStartCookingIfRequested(savedInstanceState)
                }

            val op1 = CoroutineBackgroundTask<List<RecipeIngredient>>()
                .executeInBackground {
                    AppDatabase.getInstance().recipeDao().getIngredientByRecipeId(recipeid)
                }
                .onDone { ingredients = it; op2.start() }

            CoroutineBackgroundTask<Recipe>()
                .executeInBackground {
                    AppDatabase.getInstance().recipeDao().getRecipeById(recipeid)
                }
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
        if (id == R.id.presentation_toolbar_edit) {
            switchToActivityForResult(
                REQUEST_CODE_EDIT_RECIPE_FROM_PRESENTATION,
                RecipeEditActivity::class
            ) {
                it.putExtra("recipe", recipeid)
            }
            return true
        }
        if (id == R.id.presentation_toolbar_cook) {
            cookRecipe()
            println("Cooking function called")
            return true
        }
        if (id == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initGuiElements() = with(binding) {
        recipe.notNull {
            ivBanner.setPictureFromPath(it.image)
            tvHeaderFirstline.text = it.name
        }
        viewpager.adapter = TabsAdapter(supportFragmentManager, description, ingredients)
        layoutTab.setupWithViewPager(viewpager)

    }

    class TabsAdapter(
        fm: FragmentManager?,
        description: List<RecipeDescription>,
        ingredients: List<RecipeIngredient>
    ) : FragmentStatePagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragments = mutableListOf<Fragment>()

        init {
            var text = ""
            for (el in ingredients) {
                text += "${el.count} ${el.unit} ${el.name} \n"
            }

            fragments.add(
                RecipePresentationStepFragment.with(text)
            )

            fragments.addAll(
                description.map {
                    RecipePresentationStepFragment.with(it.description)
                }
            )
        }

        override fun getCount(): Int = fragments.size

        override fun getItem(position: Int): Fragment = fragments[position]

        override fun getPageTitle(position: Int): CharSequence? =  when (position) {
                0 -> "Zutaten"
                else -> "Schritt $position"
        }

    }

    private fun cookRecipe(){
        recipe.notNull {
            cookingAssistant.cookRecipe(it)
            allowedToAutoStartCooking = false
        }

    }

    private fun autoStartCookingIfRequested(savedInstanceState: Bundle?){

        allowedToAutoStartCooking = if(savedInstanceState == null){
            intent.getIntExtra(resources.getString(R.string.data_transfer_intent_recipe_cook_request),
                -1) == REQUEST_CODE_COOK_RECIPE
        } else{
            savedInstanceState.getBoolean(
                SAVEINSTANCESTATE_KEY_ALLOWED_TO_AUTOSTART_COOKING)
        }


        if(allowedToAutoStartCooking){
            cookRecipe()
        }
    }


}