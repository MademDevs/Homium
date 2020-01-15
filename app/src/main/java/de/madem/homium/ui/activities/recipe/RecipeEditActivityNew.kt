package de.madem.homium.ui.activities.recipe

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.madem.homium.R
import de.madem.homium.databinding.ActivityRecipeEditBinding
import de.madem.homium.utilities.setPictureFromPath

class RecipeEditActivityNew: AppCompatActivity() {

    private lateinit var recipeEditViewModel: RecipeEditViewModel
    private lateinit var recipeEditViewModelFactory: RecipeEditViewModelFactory
    private lateinit var binding: ActivityRecipeEditBinding
    private var recipeId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_edit)
        binding.lifecycleOwner = this
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.recipeEdit_title_add)
        if(intent.hasExtra(resources.getString(R.string.data_transfer_intent_edit_recipe_id))) {
            recipeId = intent.getIntExtra(resources.getString(R.string.data_transfer_intent_edit_recipe_id), -1)
            supportActionBar?.title = resources.getString(R.string.recipeEdit_title_edit)
        }
        recipeEditViewModelFactory = RecipeEditViewModelFactory(recipeId)
        recipeEditViewModel = ViewModelProviders.of(this, recipeEditViewModelFactory).get(RecipeEditViewModel::class.java)
        initGuiComponents()
    }

    private fun initGuiComponents() {
        recipeEditViewModel.recipe.observe(this, Observer { newRecipe ->
            binding.recipeEditTitleEditTxt.setText(newRecipe.name)
            binding.recipeEditImgView.setPictureFromPath(newRecipe.image, 400, 400)
        })
        recipeEditViewModel.ingredients.observe(this, Observer { newIngredient ->
            for(el in newIngredient) {
                val view = layoutInflater.inflate(R.layout.recipe_edit_ingredient, null)
                view.findViewById<TextView>(R.id.ingredien_amount_txt).setText(el.count)
                view.findViewById<TextView>(R.id.ingredient_unit_txt).setText(el.unit)
                view.findViewById<TextView>(R.id.ingredient_name_txt).setText((el.name))
                binding.recipeEditLayoutIngr.addView(view)
            }
        })
        recipeEditViewModel.descriptions.observe(this, Observer { newDescription ->
            var counter = 1
            for(el in newDescription) {
                val view = layoutInflater.inflate(R.layout.recipe_edit_description, null)
                view.findViewById<TextView>(R.id.descr_count).setText(counter)
                view.findViewById<EditText>(R.id.descr_editTxt).setText(el.description)
                counter++
                binding.recipeEditLayoutDescr.addView(view)
            }
        })
        /*
        binding.recipeEditImgView.setOnClickListener { dispatchPictureIntent() }
        binding.recipeEditAddIngredientBtn.setOnClickListener { addIngredient() }
        binding.recipeEditAddDescriptionBtn.setOnClickListener { addDesciption() }
         */
    }

}