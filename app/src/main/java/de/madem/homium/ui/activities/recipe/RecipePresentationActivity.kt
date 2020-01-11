package de.madem.homium.ui.activities.recipe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.madem.homium.R
import de.madem.homium.constants.REQUEST_CODE_EDIT_RECIPE_FROM_PRESENTATION
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.utilities.*
import kotlinx.coroutines.awaitAll
import java.lang.ref.WeakReference


class RecipePresentationActivity : AppCompatActivity() {

    private var recipeid = -1
    private lateinit var recipe: Recipe
    private var cookingAssistant : CookingAssistant? = null
    private lateinit var description: List<RecipeDescription>
    private lateinit var ingredients: List<RecipeIngredient>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_presentation)
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

        //init Cooking assistant
        cookingAssistant = CookingAssistant(WeakReference<Context>(this))
    }

    private fun initGuiElements() {
            val image = findViewById<ImageView>(R.id.recipe_pres_image)
            image.setPictureFromPath(recipe.image)
            val collToolbar = findViewById<CollapsingToolbarLayout>(R.id.recipe_pres_coll_toolbar)
            collToolbar.title = recipe.name
            val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.recipe_pres_toolbar)
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
            toolbar.setNavigationOnClickListener { finish() }
            val viewPager = findViewById<ViewPager2>(R.id.recipe_pres_viewpager)
            viewPager.adapter = PresentationAdapter(recipe, description, ingredients)
            val tabLayout = findViewById<TabLayout>(R.id.recipe_pres_tablayout)
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                //name tabs
                if (position == 0) {
                    tab.text = "Zutaten"
                } else {
                    tab.text = "Schritt ${position}"
                }
            }.attach()
            setSupportActionBar(toolbar)
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
            cookRecipe()
            return true
        }
        if(id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        println("recipepres $recipeid")
        if(requestCode == REQUEST_CODE_EDIT_RECIPE_FROM_PRESENTATION) {
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
        super.onActivityResult(requestCode, resultCode, data)
    }

    //function for trigger recipe cooking
    private fun cookRecipe(){
        println("Cooking function called")
        if(cookingAssistant == null){
            showToastShort(R.string.errormsg_cooking_impossible)
        }
        else{
            //only doing this because smart cast is not availible here xD
            cookingAssistant.notNull {
                println("readyToCook")
                it.cookRecipe(recipe)
            }
        }
    }

}




class PresentationAdapter(val recipe: Recipe, val description: List<RecipeDescription>, val ingredients: List<RecipeIngredient>): RecyclerView.Adapter<PresentationAdapter.PresentationViewHolder>() {

    override fun getItemCount(): Int = description.count()+1

    override fun onBindViewHolder(holder: PresentationViewHolder, position: Int) {
        if(position == 0) {
            var text = ""
            for(el in ingredients) {
                text += "${el.count} ${el.unit} ${el.name} \n"
            }
            holder.txt.text = text
        } else {
            holder.txt.text = description[position-1].description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = PresentationViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recipe_presentation_viewpager, parent, false))

    class PresentationViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        val txt = view.findViewById<TextView>(R.id.recipe_pres_txtView)
    }

}

