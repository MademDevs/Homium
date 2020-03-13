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
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import de.madem.homium.R
import de.madem.homium.constants.REQUEST_CODE_ADD_INGREDIENT
import de.madem.homium.constants.REQUEST_GET_PHOTO_FROM_SRC
import de.madem.homium.constants.REQUEST_TAKE_PHOTO
import de.madem.homium.databinding.ActivityRecipeEditBinding
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
    private var editTextList = mutableListOf<EditText>()
    private var addDescription = false

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

    override fun onResume() {
        super.onResume()
        println("ON Resume")
        editTextList = mutableListOf()/*
        recipeEditViewModel.descriptions.observe(this, Observer { newDescription ->
            println("DESCRIPTION OBSERVER TRIGGERED $newDescription")
            if(newDescription.isNotEmpty()) {
                if(addDescription) {
                    with(newDescription.last()) {
                        val view = layoutInflater.inflate(R.layout.recipe_edit_description, null)
                        view.findViewById<TextView>(R.id.descr_count).text = "${(newDescription.count())}"
                        val editText = view.findViewById<EditText>(R.id.descr_editTxt)
                        editText.setText(this.description)
                        binding.recipeEditLayoutDescr.addView(view)
                        editTextList.add(editText)
                    }
                    addDescription = false
                } else {
                    binding.recipeEditLayoutDescr.removeAllViews()
                    var cnt = 1
                    for(el in newDescription) {
                        val view = layoutInflater.inflate(R.layout.recipe_edit_description, null)
                        view.findViewById<TextView>(R.id.descr_count).text = cnt++.toString()
                        val editText = view.findViewById<EditText>(R.id.descr_editTxt)
                        editText.setText(el.description)
                        binding.recipeEditLayoutDescr.addView(view)
                        editTextList.add(editText)
                    }
                }
            }
        })*/
    }

    override fun onPause() {
        super.onPause()
        writeDescriptionAndRecipeTitleToViewModel()
    }

    private fun writeDescriptionAndRecipeTitleToViewModel() {
        recipeEditViewModel.editRecipeName(binding.recipeEditTitleEditTxt.text.toString())
        editTextList.forEach {
            recipeEditViewModel.editDescription(editTextList.indexOf(it), it.text.toString())
        }
    }

    private fun initGuiComponents() {
        recipeEditViewModel.recipe.observe(this, Observer { newRecipe ->
            println("image changed")
            binding.recipeEditTitleEditTxt.setText(newRecipe.name)
            binding.recipeEditImgView.setPictureFromPath(newRecipe.image, 400, 400)
        })

        //test
        recipeEditViewModel.descriptions.observe(this, Observer { newDescription ->
            println("DESCRIPTION OBSERVER TRIGGERED $newDescription")
            if(true) {
                if(addDescription) {
                    with(newDescription.last()) {
                        val view = layoutInflater.inflate(R.layout.recipe_edit_description, null)
                        view.findViewById<TextView>(R.id.descr_count).text = "${(newDescription.count())}"
                        val editText = view.findViewById<EditText>(R.id.descr_editTxt)
                        editText.setText(this.description)
                        binding.recipeEditLayoutDescr.addView(view)
                        editTextList.add(editText)
                    }
                    addDescription = false
                } else {


                    binding.recipeEditLayoutDescr.removeAllViews()
                    var cnt = 1
                    for(el in newDescription) {
                        val view = layoutInflater.inflate(R.layout.recipe_edit_description, null)
                        view.findViewById<TextView>(R.id.descr_count).text = cnt++.toString()
                        val editText = view.findViewById<EditText>(R.id.descr_editTxt)
                        println("Description: ${el.description}")
                        editText.setText(el.description)
                        binding.recipeEditLayoutDescr.addView(view)
                        editTextList.add(editText)
                    }
                }
            }
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
        binding.recipeEditImgView.setOnClickListener {
            AlertDialog.Builder(this).setSingleChoiceItems(R.array.recipes_photo_options,0,
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
        binding.recipeEditAddIngredientBtn.setOnClickListener { switchToActivityForResult(REQUEST_CODE_ADD_INGREDIENT,IngredientEditActivity::class) }
        binding.recipeEditAddDescriptionBtn.setOnClickListener {
            addDescription = true
            recipeEditViewModel.addDescription(RecipeDescription("", 0))
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
                                putExtra(resources.getString(R.string.data_transfer_intent_edit_recipe_id),recipeId)
                            }
                        }
                    }.start()
                }
                else{
                    showToastLong(R.string.errormsg_invalid_recipe_title)
                }
                //finishWithBooleanResult("dataChanged", true, Activity.RESULT_OK)
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
    }

    override fun onDestroy() {
        super.onDestroy()
/*
        with(recipeEditViewModel){
            if(shallDiscardPictureChanges){
                discardPictureChanges()
            }
        }

 */
    }

}