package de.madem.homium.ui.activities.recipe

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.room.Database
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RecipeEditActivity : AppCompatActivity() {

    private lateinit var imgView: ImageView
    private lateinit var title: EditText
    private lateinit var description: EditText

    private val db = AppDatabase.getInstance()

    val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_TAKE_PHOTO = 1
    lateinit var currentPhotoPath: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_edit)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)


        initGuiComponents()
/*
        itemid = intent.getIntExtra("item", -1)
        if(itemid >= 0) {
            btnDelete.isVisible = true
            setShoppingItemToElements(itemid)
            supportActionBar?.title = resources.getString(R.string.screentitle_edit_shoppingitem_edit)
        } else {
            supportActionBar?.title = resources.getString(R.string.screentitle_edit_shopppingitem_add)
            btnDelete.isVisible = false
        }
*/
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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imgView.setImageBitmap(imageBitmap)
        }
    }

}