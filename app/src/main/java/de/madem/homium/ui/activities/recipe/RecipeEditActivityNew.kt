package de.madem.homium.ui.activities.recipe

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.madem.homium.R
import de.madem.homium.constants.REQUEST_CODE_ADD_INGREDIENT
import de.madem.homium.constants.REQUEST_CODE_EDIT_RECIPE_FROM_PRESENTATION
import de.madem.homium.constants.REQUEST_TAKE_PHOTO
import de.madem.homium.databinding.ActivityRecipeEditBinding
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.ui.activities.ingredient.IngredientEditActivity
import de.madem.homium.utilities.finishWithBooleanResult
import de.madem.homium.utilities.notNull
import de.madem.homium.utilities.setPictureFromPath
import de.madem.homium.utilities.switchToActivityForResult
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RecipeEditActivityNew: AppCompatActivity() {

    private lateinit var recipeEditViewModel: RecipeEditViewModel
    private lateinit var recipeEditViewModelFactory: RecipeEditViewModelFactory
    private lateinit var binding: ActivityRecipeEditBinding
    private var recipeId: Int? = null
    private var picturePath = ""

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
            binding.recipeEditLayoutIngr.removeAllViews()
            for(el in newIngredient) {
                val view = layoutInflater.inflate(R.layout.recipe_edit_ingredient, null)
                view.findViewById<TextView>(R.id.ingredient_amount_txt).text = "${el.count}"
                view.findViewById<TextView>(R.id.ingredient_unit_txt).setText(el.unit)
                view.findViewById<TextView>(R.id.ingredient_name_txt).setText(el.name)
                binding.recipeEditLayoutIngr.addView(view)
            }
        })
        recipeEditViewModel.descriptions.observe(this, Observer { newDescription ->
            binding.recipeEditLayoutDescr.removeAllViews()
            for(el in newDescription) {
                val view = layoutInflater.inflate(R.layout.recipe_edit_description, null)
                view.findViewById<TextView>(R.id.descr_count).text = "${(newDescription.indexOf(el)+1)}"
                with(view.findViewById<EditText>(R.id.descr_editTxt)) {
                    setText(el.description)
                    //TODO: TextChangeListener not working properly yet -> Fires on every change in edittext instead of when done with editing
                    //TODO: OrientationChange still messed up
                    addTextChangedListener( object: TextWatcher {
                        override fun afterTextChanged(p0: Editable?) { recipeEditViewModel.editDescription(newDescription.indexOf(el), RecipeDescription(p0.toString(), 0)) }
                        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {  }
                        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {  }
                    })
                }
                binding.recipeEditLayoutDescr.addView(view)
            }
        })
        //TODO: Implement working listener also for RecipeName
        binding.recipeEditTitleEditTxt.addTextChangedListener( object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {  }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {  }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {  }
        })
        binding.recipeEditImgView.setOnClickListener { dispatchTakePictureIntent() }
        binding.recipeEditAddIngredientBtn.setOnClickListener { switchToActivityForResult(REQUEST_CODE_ADD_INGREDIENT,IngredientEditActivity::class) }
        binding.recipeEditAddDescriptionBtn.setOnClickListener { recipeEditViewModel.addDescription(RecipeDescription("", 0)) }
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                val photoFile: File? = try { createImageFile() }
                catch (ex: IOException) { null }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", it)
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            .apply { picturePath = absolutePath }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_TAKE_PHOTO -> recipeEditViewModel.setImagePath(picturePath)
                REQUEST_CODE_ADD_INGREDIENT -> {
                    data.notNull { dataIntent ->
                        val ingrName = dataIntent.getStringExtra(resources.getString(R.string.data_transfer_intent_edit_ingredient_name)) ?: ""
                        val ingrCount = dataIntent.getIntExtra(resources.getString(R.string.data_transfer_intent_edit_ingredient_count), 0)
                        val ingrUnit = dataIntent.getStringExtra(resources.getString(R.string.data_transfer_intent_edit_ingredient_unit)) ?: ""
                        if(ingrName != "" && ingrCount != 0 && ingrUnit != "") {
                            val ingredient = RecipeIngredient(ingrName, ingrCount, ingrUnit, 0)
                            recipeEditViewModel.addIngredient(ingredient)
                        } else {
                            println("dataIntent empty")
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(menu != null) { menuInflater.inflate(R.menu.recipe_edit_actionbar_menu,menu) }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.recipe_edit_actionbar_confirm -> {
                recipeEditViewModel.addDataToDatabase()
                finishWithBooleanResult("dataChanged", true, REQUEST_CODE_EDIT_RECIPE_FROM_PRESENTATION)
            }
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}