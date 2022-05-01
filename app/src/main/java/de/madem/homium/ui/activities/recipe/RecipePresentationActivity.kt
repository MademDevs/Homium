package de.madem.homium.ui.activities.recipe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import dagger.hilt.android.AndroidEntryPoint
import de.madem.homium.R
import de.madem.homium.constants.INTENT_DATA_TRANSFER_EDIT_COOK_REQUEST
import de.madem.homium.constants.INTENT_DATA_TRANSFER_EDIT_RECIPE_ID
import de.madem.homium.constants.REQUEST_CODE_COOK_RECIPE
import de.madem.homium.constants.REQUEST_CODE_EDIT_RECIPE_FROM_PRESENTATION
import de.madem.homium.databinding.ActivityRecipePresentationBinding
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.utilities.CookingAssistant
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.notNull
import de.madem.homium.utilities.extensions.setPictureFromPath
import de.madem.homium.utilities.extensions.switchToActivityForResult
import javax.inject.Inject

@AndroidEntryPoint
class RecipePresentationActivity : AppCompatActivity() {

    private var recipeid = -1
    private var recipe: Recipe? = null
    private lateinit var description: List<RecipeDescription>
    private lateinit var ingredients: List<RecipeIngredient>

    @Inject
    lateinit var cookingAssistant : CookingAssistant
    private var allowedToAutoStartCooking : Boolean = false

    private lateinit var binding: ActivityRecipePresentationBinding
    private val viewModel : RecipePresentationViewModel by viewModels()

    companion object{
        private const val SAVEINSTANCESTATE_KEY_ALLOWED_TO_AUTOSTART_COOKING = "autostartcookingpermission"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this, R.layout.activity_recipe_presentation
        )
        supportActionBar?.title = resources.getString(R.string.screentitle_recipe_presentation)

        //getting id right
        val intentId = intent.getIntExtra(
            INTENT_DATA_TRANSFER_EDIT_RECIPE_ID, -1)

        //end activity if there is no valid id in intent
        if(intentId < 0){
            finish()
        }
        else{
            val vMiD : Int = viewModel.recipeId
            recipeid = if(vMiD < 0){
                //live data in viewmodel is not initialized
                viewModel.updateRecipeId(intentId)
                intentId
            } else{
                vMiD
            }

        }


        loadRecipe(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(SAVEINSTANCESTATE_KEY_ALLOWED_TO_AUTOSTART_COOKING,allowedToAutoStartCooking)

    }

    private fun loadRecipe(savedInstanceState: Bundle? = null) {

        if (recipeid > 0) {
            //nesting coroutines to avaid not initialized properties -> also possible with await?

            val op2 = CoroutineBackgroundTask<List<RecipeDescription>>()
                .executeInBackground { viewModel.getRecipeDescription() }
                .onDone {
                    description = it;
                    initGuiElements()
                    autoStartCookingIfRequested(savedInstanceState)
                }

            val op1 = CoroutineBackgroundTask<List<RecipeIngredient>>()
                .executeInBackground { viewModel.getIngredients() }
                .onDone { ingredients = it; op2.start() }

            CoroutineBackgroundTask<Recipe>()
                .executeInBackground { viewModel.getRecipe() }
                .onDone {
                    recipe = it
                    op1.start()
                }
                .start()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.presentation_toolbar_menu, menu)
        return true
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.presentation_toolbar_edit -> {
                switchToActivityForResult(
                    REQUEST_CODE_EDIT_RECIPE_FROM_PRESENTATION,
                    RecipeEditActivity::class
                ) {
                    it.putExtra(INTENT_DATA_TRANSFER_EDIT_RECIPE_ID, recipeid)
                }

                true
            }
            R.id.presentation_toolbar_cook -> {
                cookRecipe()
                true
            }
            R.id.recipe_presentation_share -> {
                shareRecipe()
                true
            }
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data.notNull { intent ->
            if(resultCode == Activity.RESULT_OK){
                when(requestCode){
                    REQUEST_CODE_EDIT_RECIPE_FROM_PRESENTATION -> {
                        val dataChanged = intent.getBooleanExtra("dataChanged",false)
                        if(dataChanged){
                            val id = intent.getIntExtra(INTENT_DATA_TRANSFER_EDIT_RECIPE_ID,-1)
                            viewModel.updateRecipeId(id)
                            recipeid = id
                            loadRecipe()
                        }
                    }
                }
            }
        }
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
            intent.getIntExtra(
                INTENT_DATA_TRANSFER_EDIT_COOK_REQUEST,
                -1) == REQUEST_CODE_COOK_RECIPE
        } else{
            savedInstanceState.getBoolean(
                SAVEINSTANCESTATE_KEY_ALLOWED_TO_AUTOSTART_COOKING)
        }


        if(allowedToAutoStartCooking){
            cookRecipe()
        }
    }

    private fun shareRecipe(){
        CoroutineBackgroundTask<String>().executeInBackground {
            val rec = recipe
            return@executeInBackground if(rec == null){
                ""
            }
            else{
                "${rec.name}:\n\n${resources.getString(R.string.recipe_ingredients)}:\n- ${ingredients
                    .joinToString("\n- ") { it.toString() }}\n\n${resources.getString(R.string.recipe_description)}:\n" +
                        description.mapIndexed { index, description -> "${index+1}) ${description.description}"}.joinToString("\n")
            }
        }.onDone {result ->
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT,result)

            if(shareIntent.resolveActivity(packageManager) != null){
                startActivity(shareIntent)
            }
        }.start()
    }


}