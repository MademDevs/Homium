package de.madem.homium.ui.activities.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.setPictureFromPath
import kotlinx.coroutines.awaitAll


class RecipePresentation: AppCompatActivity() {

    private var recipeid = -1
    private lateinit var recipe: Recipe
    private lateinit var description: List<RecipeDescription>
    private lateinit var ingredients: List<RecipeIngredient>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_presentation)
        recipeid = intent.getIntExtra("recipe", -1)
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

