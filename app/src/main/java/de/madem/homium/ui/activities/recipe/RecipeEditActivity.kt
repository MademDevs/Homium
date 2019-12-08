package de.madem.homium.ui.activities.recipe

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.room.Database
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.utilities.CoroutineBackgroundTask
import de.madem.homium.utilities.finishWithBooleanResult
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RecipeEditActivity : AppCompatActivity() {

    private lateinit var imgView: ImageView
    private lateinit var title: EditText
    private lateinit var description: EditText

    private val db = AppDatabase.getInstance()

    val REQUEST_TAKE_PHOTO = 1
    var currentPhotoPath: String = ""
    private var recipeid: Int = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_edit)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)


        initGuiComponents()


        recipeid = intent.getIntExtra("recipe", -1)
        println("Recipe-ID: $recipeid")
        if(recipeid >= 0) {
            setRecipeToElements(recipeid)
            supportActionBar?.title = resources.getString(R.string.recipeEdit_title_edit)
        } else {
            supportActionBar?.title = resources.getString(R.string.recipeEdit_title_add)
        }

    }

    fun setRecipeToElements(id: Int) {
        CoroutineBackgroundTask<Recipe>()
            .executeInBackground { db.recipeDao().getRecipeById(id) }
            .onDone {
                //setting name
               title.text = Editable.Factory.getInstance().newEditable(it.name)
                description.text = Editable.Factory.getInstance().newEditable(it.description)
                setPicFromDatabse(it.image)

            }
            .start()
    }

    fun setPicFromDatabse(databasePhotoPath: String) {
        // Get the dimensions of the View
        if(databasePhotoPath.isNotEmpty()) {
            BitmapFactory.decodeFile(databasePhotoPath)?.also { bitmap ->
                imgView.setImageBitmap(bitmap)
            }
        } else {
            imgView.setImageResource(R.drawable.empty_picture)
        }
    }

    fun addOrUpdateToDatabaseIfPossible() {
        val name: String = title.text.toString()
        val details: String = description.text.toString()

        if(name.isNotBlank() && details.isNotBlank()){
            //all input components are valid -> creating object and put it into database via coroutine
            val recipe = Recipe(name, details, currentPhotoPath)
            CoroutineBackgroundTask<Unit>().executeInBackground {
                db.recipeDao().insertRecipe(recipe)
            }.onDone {
                finishWithBooleanResult("dataChanged",true, Activity.RESULT_OK)
            }.start()
        }
        else{
            Toast.makeText(this, resources.getString(R.string.errormsg_invalid_shoppingitem_parameters),
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.recipe_edit_actionbar_confirm ->  addOrUpdateToDatabaseIfPossible()
            android.R.id.home -> finishWithBooleanResult("dataChanged",false, Activity.RESULT_OK)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(menu != null){
            menuInflater.inflate(R.menu.recipe_edit_actionbar_menu,menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun initGuiComponents() {
        imgView = findViewById(R.id.recipe_edit_imgView)
        title = findViewById(R.id.recipe_edit_title_editTxt)
        description = findViewById(R.id.recipe_edit_description_editTxt)
        imgView.setOnClickListener { dispatchTakePictureIntent() }
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



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            /*
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imgView.setImageBitmap(imageBitmap)
             */
            setPic()
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
            imgView.setImageResource(R.drawable.empty_picture)
        }
    }

}