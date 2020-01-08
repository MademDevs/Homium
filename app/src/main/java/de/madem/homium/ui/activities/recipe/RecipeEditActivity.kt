package de.madem.homium.ui.activities.recipe

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.FileProvider
import androidx.core.view.children
import com.google.android.material.textfield.TextInputEditText
import de.madem.homium.R
import de.madem.homium.constants.REQUEST_CODE_ADD_INGREDIENT
import de.madem.homium.constants.REQUEST_TAKE_PHOTO
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.ui.activities.ingredient.IngredientEditActivity
import de.madem.homium.utilities.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RecipeEditActivity : AppCompatActivity() {

    //fields
    private lateinit var imgView: ImageView
    private lateinit var title: EditText
    private lateinit var addIngredientBtn: ImageButton
    private lateinit var ingrName: String
    private lateinit var ingrUnit: String
    private var ingrCount: Int = 0
    private var ingredients: MutableList<Ingredient> = mutableListOf()
    private var descriptions: MutableList<String> = mutableListOf()
    private lateinit var addDescrBtn: ImageButton
    //private lateinit var description: EditText
    private lateinit var descriptionList: List<RecipeDescription>
    private lateinit var ingredientsList: List<RecipeIngredient>
    private val descriptionEditTexts = mutableListOf<EditText>()
    private lateinit var descriptionLayout : LinearLayout

    private val db = AppDatabase.getInstance()

    var currentPhotoPath: String = ""
    private var recipeid: Int = -1
    private var descrCounter: Int = 0
    private var ingredientCounter: Int = 0

    data class Ingredient(val id: Int, val count: Int, val unit: String, val name: String)

    companion object {
        private const val INGREDIENT_KEY_DUMMY = "INGREDIENT_#"
        private const val DESCRIPTION_KEY_DUMMY = "DESCRIPTION_#"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_edit)

        initGuiComponents()


        if(savedInstanceState != null) {
            ingredientCounter = savedInstanceState.getInt("counter")
            for(i in 1..ingredientCounter) {
                val item: Ingredient? = savedInstanceState.getIngredient(INGREDIENT_KEY_DUMMY.replace("#","$i"))
                if(item != null) {
                    ingredients.add(item)
                }
            }

            descrCounter = savedInstanceState.getInt("descriptions")
            println("Counter: $descrCounter")
            for(i in 1..descrCounter) {
                val item: String? = savedInstanceState.getString(DESCRIPTION_KEY_DUMMY.replace("#","$i"))
                println("beschreibung$i: $item")
                if(item != null) {
                    descriptions.add(item)
                    addDescriptionToLayout(item,i)
                }
            }
            setIngredientsFromBundle(ingredients)
            //addDescriptionToLayout(descriptions)


        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recipeid = intent.getIntExtra("recipe", -1)
        println("Recipe-ID: $recipeid")


        if(recipeid >= 0) {
            setRecipeToElements(recipeid)
            supportActionBar?.title = resources.getString(R.string.recipeEdit_title_edit)
        } else {
            assignPreDefinedNameIfExisting()
            supportActionBar?.title = resources.getString(R.string.recipeEdit_title_add)
        }
    }

    private fun addDescriptionToLayout(text: String, index : Int) {
        //FEHLER IRGENDWO HIER!!!


        val view = layoutInflater.inflate(R.layout.recipe_edit_description, descriptionLayout,false)
        println("addDescriptionToLayout: text: $text, index: $index")


        view.findViewById<TextView>(R.id.descr_count).text = (index.toString())
        view.findViewById<EditText>(R.id.descr_editTxt).setText(text)
        val editTxt = view.findViewById<EditText>(R.id.descr_editTxt)
        val ed2 = EditText(this)
        descriptionLayout.addView(view)
        descriptionEditTexts.add(editTxt)




    }

    private fun setIngredientsFromBundle(list: List<Ingredient>) {
        for(el in list) {
            val ingrLayout = findViewById<LinearLayout>(R.id.recipe_edit_layout_ingr)
            val view = layoutInflater.inflate(R.layout.recipe_edit_ingredient, null)
            view.findViewById<TextView>(R.id.ingredien_amount_txt).text = el.count.toString()
            view.findViewById<TextView>(R.id.ingredient_unit_txt).text = el.unit
            view.findViewById<TextView>(R.id.ingredient_name_txt).text = el.name
            ingrLayout.addView(view)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(ingredientCounter > 0) {
            outState.putInt("counter", ingredientCounter)
            for (el in ingredients) {
                outState.putIngredient(INGREDIENT_KEY_DUMMY.replace("#","${el.id}"), el)
            }
        }
        if(descrCounter > 0) {
            outState.putInt("descriptions", descrCounter)
            println(descrCounter)
            val descrLayout = findViewById<LinearLayout>(R.id.recipe_edit_layout_descr)
            var counter: Int = 0
            for(et in descriptionEditTexts) { //el in descrLayout.children
                counter++
                println("Beschreibung: ${et.text}")
                outState.putString(DESCRIPTION_KEY_DUMMY.replace("#","$counter")
                    , et.text.toString())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(menu != null){
            menuInflater.inflate(R.menu.recipe_edit_actionbar_menu,menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun addDescription() {

        descrCounter++
        val descrLayout = findViewById<LinearLayout>(R.id.recipe_edit_layout_descr)
        val view = layoutInflater.inflate(R.layout.recipe_edit_description, null)
        view.findViewById<TextView>(R.id.descr_count).text = descrCounter.toString()
        val editTxt = view.findViewById<EditText>(R.id.descr_editTxt)
        //description = view.findViewById(R.id.descr_editTxt)
        descrLayout.addView(view)
        descriptionEditTexts.add(editTxt)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                REQUEST_TAKE_PHOTO -> setPic()
                REQUEST_CODE_ADD_INGREDIENT -> {
                    data.notNull { dataIntent ->
                        ingredientCounter++
                        ingrName = dataIntent.getStringExtra(resources.getString(R.string.data_transfer_intent_edit_ingredient_name)) ?: ""
                        ingrCount = dataIntent.getIntExtra(resources.getString(R.string.data_transfer_intent_edit_ingredient_count),0)
                        ingrUnit = dataIntent.getStringExtra(resources.getString(R.string.data_transfer_intent_edit_ingredient_unit)) ?: ""
                        val ingredient = Ingredient(ingredientCounter, ingrCount, ingrUnit, ingrName)
                        ingredients.add(ingredient)
                        //TODO: Add logic for adding an Ingredient
                        val ingrLayout = findViewById<LinearLayout>(R.id.recipe_edit_layout_ingr)
                        val view = layoutInflater.inflate(R.layout.recipe_edit_ingredient, null)
                        view.findViewById<TextView>(R.id.ingredien_amount_txt).text = ingredient.count.toString()
                        view.findViewById<TextView>(R.id.ingredient_unit_txt).text = ingredient.unit
                        view.findViewById<TextView>(R.id.ingredient_name_txt).text = ingredient.name
                        ingrLayout.addView(view)
                    }
                }
            }
        }



        /*
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            /*
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imgView.setImageBitmap(imageBitmap)
             */
            setPic()
        }

         */
    }

    //private functions
    private fun setRecipeToElements(id: Int) {
        CoroutineBackgroundTask<Recipe>()
            .executeInBackground { db.recipeDao().getRecipeById(id) }
            .onDone {
                //setting name
                title.text = Editable.Factory.getInstance().newEditable(it.name)
                imgView.setPictureFromPath(it.image)
                currentPhotoPath = it.image
            }
            .start()
        CoroutineBackgroundTask<List<RecipeIngredient>>()
            .executeInBackground { db.recipeDao().getIngredientByRecipeId(id) }
            .onDone { ingredientsList = it; setIngredients(it) }
            .start()
        CoroutineBackgroundTask<List<RecipeDescription>>()
            .executeInBackground { db.recipeDao().getDescriptionByRecipeId(id) }
            .onDone { descriptionList = it; setDescription(it) }
            .start()
    }

    private fun setIngredients(list: List<RecipeIngredient>) {
        for(el in list) {
            val ingrLayout = findViewById<LinearLayout>(R.id.recipe_edit_layout_ingr)
            val view = layoutInflater.inflate(R.layout.recipe_edit_ingredient, null)
            view.findViewById<TextView>(R.id.ingredien_amount_txt).text = el.count.toString()
            view.findViewById<TextView>(R.id.ingredient_unit_txt).text = el.unit
            view.findViewById<TextView>(R.id.ingredient_name_txt).text = el.name
            ingrLayout.addView(view)
        }
    }

    private fun setDescription(list: List<RecipeDescription>) {
        var counter = 0
        for(el in list) {
            counter++
            val descrLayout = findViewById<LinearLayout>(R.id.recipe_edit_layout_descr)
            val view = layoutInflater.inflate(R.layout.recipe_edit_description, null)
            view.findViewById<TextView>(R.id.descr_count).text = counter.toString()
            view.findViewById<EditText>(R.id.descr_editTxt).text = Editable.Factory.getInstance().newEditable(el.description)
            descrLayout.addView(view)
        }
    }

    private fun addOrUpdateToDatabaseIfPossible() {
        val name: String = title.text.toString()
        val descrLayout = findViewById<LinearLayout>(R.id.recipe_edit_layout_descr)

            if (name.isNotBlank()) {
                //all input components are valid -> creating object and put it into database via coroutine
                val recipe = Recipe(name, currentPhotoPath)
                CoroutineBackgroundTask<Unit>().executeInBackground {
                    //Update method in Dao not working properly, so deleting first, then adding new
                    if(recipeid > 0) {
                        db.recipeDao().deleteRecipe(Recipe(recipe.name, recipe.image, recipeid))
                        db.recipeDao().deleteDescriptionByRecipeId(recipeid)
                        db.recipeDao().deleteIngredientByRecipeId(recipeid)
                        ingredientsList.forEach {
                            db.recipeDao().insertIngredient(RecipeIngredient(it.name, it.count, it.unit, recipeid))
                        }
                    }
                    recipeid = db.recipeDao().insertRecipe(recipe).toInt()
                    println("RecipeID add: $recipeid")
                    //add newly added ingredients
                    ingredients.forEach {
                        db.recipeDao().insertIngredient(
                            RecipeIngredient(
                                it.name,
                                it.count,
                                it.unit,
                                recipeid
                            )
                        )
                    }
                    //add old ingredients (saved in list oncreateview)

                    //description in edittext, so saving in 1 go
                    for (el in descrLayout.children) {
                        db.recipeDao().insertDescription(
                            RecipeDescription(
                                el.findViewById<EditText>(R.id.descr_editTxt).text.toString(),
                                recipeid
                            )
                        )
                    }
                }.onDone {
                    finishWithBooleanResult("dataChanged", true, Activity.RESULT_OK)
                }.start()
            } else {
                Toast.makeText(
                    this, resources.getString(R.string.errormsg_invalid_parameters),
                    Toast.LENGTH_LONG
                ).show()
            }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.recipe_edit_actionbar_confirm -> addOrUpdateToDatabaseIfPossible()
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun initGuiComponents() {
        imgView = findViewById(R.id.recipe_edit_imgView)
        title = findViewById(R.id.recipe_edit_title_editTxt)
        addIngredientBtn = findViewById(R.id.recipe_edit_addIngredient_btn)
        imgView.setOnClickListener { dispatchTakePictureIntent() }
        addIngredientBtn.setOnClickListener { addIngredient() }
        descriptionLayout = findViewById<LinearLayout>(R.id.recipe_edit_layout_descr)
        addDescrBtn = findViewById(R.id.recipe_edit_addDescription_btn)
        addDescrBtn.setOnClickListener { addDescription() }
    }

    private fun addIngredient() {
        //TODO: Start AddIngredients-Activity and insert selected values in textfields
        switchToActivityForResult(REQUEST_CODE_ADD_INGREDIENT,IngredientEditActivity::class)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }


    private fun setPic() {
        // Get the dimensions of the View
        if(currentPhotoPath.isNotEmpty()) {
            val targetW: Int = imgView.width
            val targetH: Int = imgView.height

            val bmOptions = BitmapFactory.Options().apply {
                // Get the dimensions of the bitmap
                inJustDecodeBounds = true

                val photoW: Int = outWidth
                val photoH: Int = outHeight

                // Determine how much to scale down the image
                val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

                // Decode the image file into a Bitmap sized to fill the View
                inJustDecodeBounds = false
                inSampleSize = scaleFactor
                inPurgeable = true
            }
            BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
                imgView.setImageBitmap(bitmap)
            }
        } else {
            imgView.setImageResource(R.mipmap.empty_picture)
        }
    }

    private fun assignPreDefinedNameIfExisting(){
        val name : String? = intent.getStringExtra(resources.getString(R.string.data_transfer_intent_edit_recipe_name))
        name.notNull {
            title.setText(name)
        }
    }

}