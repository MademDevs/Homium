package de.madem.homium.ui.activities.recipe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.github.paolorotolo.appintro.AppIntro2Fragment
import com.github.paolorotolo.appintro.model.SliderPagerBuilder
import com.google.android.material.appbar.CollapsingToolbarLayout
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.RecipeDao
import de.madem.homium.models.Recipe
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.setPictureFromPath
import de.madem.homium.utilities.switchToActivity


class RecipePresentation: AppCompatActivity() {

    private lateinit var dao: RecipeDao
    private lateinit var imgView: ImageView
    private lateinit var txtView: TextView
    private lateinit var recipe: Recipe
    private lateinit var coltoolbar: CollapsingToolbarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var startBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_presentation)
        txtView = findViewById(R.id.presentation_txtView)
        imgView = findViewById(R.id.presentation_imgView)
        coltoolbar = findViewById(R.id.collapsingToolbar)
        startBtn = findViewById(R.id.presentation_start_btn)
        toolbar = findViewById(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        toolbar.setNavigationOnClickListener {
            finish()
        }
        toolbar.inflateMenu(R.menu.presentation_toolbar_menu)
        toolbar.setOnMenuItemClickListener {
            finish()
            false
        }
        dao = AppDatabase.getInstance().recipeDao()
        val recipeId = intent.extras?.getInt("recipe")
        if(recipeId != null) {
            CoroutineBackgroundTask<Recipe>()
                .executeInBackground { dao.getRecipeById(recipeId) }
                .onDone { recipe = it; setRecipeToView(it)}
                .start()
        }
        startBtn.setOnClickListener { startActivity(Intent(this, PresentationMode::class.java).putExtra("recipeId", recipe.uid)) }
    }

    private fun setRecipeToView(recipe: Recipe) {
        coltoolbar.title = recipe.name
        txtView.text = recipe.description
        imgView.setPictureFromPath(recipe.image)
        toolbar.title = recipe.uid.toString()
    }



}