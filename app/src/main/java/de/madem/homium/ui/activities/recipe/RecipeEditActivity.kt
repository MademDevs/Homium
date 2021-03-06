package de.madem.homium.ui.activities.recipe

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import de.madem.homium.R
import de.madem.homium.constants.*
import de.madem.homium.databinding.ActivityRecipeEditBinding
import de.madem.homium.managers.adapters.IngredientsAdapter
import de.madem.homium.managers.adapters.RecipeDescriptionAdapter
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.ui.activities.ingredient.IngredientEditActivity
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.*
import kotlinx.coroutines.async
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class RecipeEditActivity: AppCompatActivity() {

    private lateinit var recipeEditViewModel: RecipeEditViewModel
    private lateinit var recipeEditViewModelFactory: RecipeEditViewModelFactory
    private lateinit var binding: ActivityRecipeEditBinding
    private var recipeId: Int? = null
    private var picturePath = ""

    private var descriptionAdapter : RecipeDescriptionAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_recipe_edit)
        binding.lifecycleOwner = this
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = resources.getString(R.string.recipeEdit_title_add)
        if(intent.hasExtra(INTENT_DATA_TRANSFER_EDIT_RECIPE_ID)) {
            recipeId = intent.getIntExtra(INTENT_DATA_TRANSFER_EDIT_RECIPE_ID, -1)
            supportActionBar?.title = resources.getString(R.string.recipeEdit_title_edit)
        }
        recipeEditViewModelFactory = RecipeEditViewModelFactory(recipeId)
        recipeEditViewModel = ViewModelProviders.of(this, recipeEditViewModelFactory).get(RecipeEditViewModel::class.java)
        initGuiComponents()
    }

    override fun onPause() {
        super.onPause()
        writeDescriptionAndRecipeTitleToViewModel()
    }

    private fun writeDescriptionAndRecipeTitleToViewModel() {
        recipeEditViewModel.editRecipeName(binding.recipeEditTitleEditTxt.text.toString())

        with(descriptionAdapter?.descriptionTexts()){
            this.notNull {
                for(idx in it.indices){
                    recipeEditViewModel.editDescription(idx,it[idx])
                }
            }
        }

    }

    private fun initGuiComponents() {
        recipeEditViewModel.recipe.observe(this, Observer { newRecipe ->
            binding.recipeEditTitleEditTxt.setText(newRecipe.name)
            binding.recipeEditImgView.setPictureFromPath(newRecipe.image, 400, 400)
        })

        //ingredients
        val ingrAdapter = IngredientsAdapter(this,recipeEditViewModel
            .ingredients.value ?: mutableListOf<RecipeIngredient>()).apply {
            deleteButtonClickListener = {position ->
                recipeEditViewModel.ingredients.value?.removeAt(position)
            }
        }
        binding.recyclerviewEditIngredients.adapter = ingrAdapter
        binding.recyclerviewEditIngredients.layoutManager = LinearLayoutManager(this)
        recipeEditViewModel.ingredients.observe(this, Observer {newIngredients ->
            ingrAdapter.setData(newIngredients)
        })


        binding.recipeEditAddIngredientBtn.setOnClickListener { switchToActivityForResult(REQUEST_CODE_ADD_INGREDIENT,IngredientEditActivity::class) }

        //descriptions
        descriptionAdapter = RecipeDescriptionAdapter(this,
            recipeEditViewModel.descriptions.value ?: listOf())

        recipeEditViewModel.descriptions.observe(this, Observer {newDescriptions ->
            descriptionAdapter?.setData(newDescriptions)
        })

        binding.recyclerviewEditDescriptions.adapter = descriptionAdapter
        binding.recyclerviewEditDescriptions.layoutManager = LinearLayoutManager(this)
        binding.recipeEditAddDescriptionBtn.setOnClickListener {
            //addDescription = true
            recipeEditViewModel.addDescription(RecipeDescription("", 0))
        }

        //imageview
        binding.recipeEditImgView.setOnClickListener {
            AlertDialog.Builder(this).setItems(R.array.recipes_photo_options,
                DialogInterface.OnClickListener { dialog, position ->
                    when(position){
                        0 -> {
                            dispatchTakePictureIntent()
                            dialog.dismiss()
                        }
                        1 -> {
                            dispatchImageSrcLoadingIntent()
                            dialog.dismiss()
                        }
                        2 -> {
                            recipeEditViewModel.editImagePath("")
                            dialog.dismiss()
                        }
                    }
                }).setTitle(R.string.dialog_choosePicture).show()

        }

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

    private fun dispatchImageSrcLoadingIntent(){
        val photoPickIntent = Intent(Intent.ACTION_PICK)
        photoPickIntent.type = "image/*"
        startActivityForResult(photoPickIntent, REQUEST_GET_PHOTO_FROM_SRC)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_TAKE_PHOTO -> recipeEditViewModel.editImagePath(picturePath)
                REQUEST_GET_PHOTO_FROM_SRC ->{

                    if(data != null){
                        loadImageFromSrc(data.data)
                    }
                    else{
                        showPhotoLoadingError()
                    }

                }
                REQUEST_CODE_ADD_INGREDIENT -> {
                    data.notNull { dataIntent ->
                        val ingrName = dataIntent.getStringExtra(
                            INTENT_DATA_TRANSFER_EDIT_INGREDIENT_NAME) ?: ""
                        val ingrCount = dataIntent.getIntExtra(
                            INTENT_DATA_TRANSFER_EDIT_INGREDIENT_COUNT, 0)
                        val ingrUnit = dataIntent.getStringExtra(
                            INTENT_DATA_TRANSFER_EDIT_INGREDIENT_UNIT) ?: ""
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

    private fun loadImageFromSrc(uri : Uri?){
        CoroutineBackgroundTask<String?>().executeInBackground {
            try {
                if(uri != null){
                    val resultFileDeferred = async<File> {
                        createImageFile()
                    }

                    val imageStream = contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(imageStream)

                    val resultFile = resultFileDeferred.await()

                    val success = bitmap.compress(Bitmap.CompressFormat.JPEG,85,resultFile.outputStream())

                    if(success) resultFile.absolutePath else null

                }
                else{
                    showPhotoLoadingError();
                    null
                }
            }
            catch (ex : Exception){
                ex.printStackTrace()
                showPhotoLoadingError()
                null
            }
        }.onDone {
            it.notNull {path ->
                recipeEditViewModel.editImagePath(path)
            }
        }.start()
    }

    private fun showPhotoLoadingError() = Toast.makeText(this,R.string.photo_loading_error,Toast.LENGTH_SHORT).show()

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(menu != null) { menuInflater.inflate(R.menu.recipe_edit_actionbar_menu,menu) }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.recipe_edit_actionbar_confirm -> {
                val nameText = binding.recipeEditTitleEditTxt.text.toString();
                if(nameText.isNotEmpty() && nameText.isNotBlank()){
                    writeDescriptionAndRecipeTitleToViewModel()
                    CoroutineBackgroundTask<Unit>().executeInBackground {
                        recipeEditViewModel.addDataToDatabase()
                    }.onDone {
                        println("inserted reciped with ingredients and descriptions")
                        //recipeEditViewModel.shallDiscardPictureChanges = false
                        finishWithResultData(Activity.RESULT_OK){intent ->
                            with(intent){
                                putExtra("dataChanged", true)
                                putExtra(INTENT_DATA_TRANSFER_EDIT_RECIPE_ID,recipeId)
                            }
                        }
                    }.start()
                }
                else{
                    showToastLong(R.string.errormsg_invalid_recipe_title)
                }
            }
            android.R.id.home -> {
                recipeEditViewModel.discardPictureChanges()
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        recipeEditViewModel.discardPictureChanges()
        finish()
    }

}