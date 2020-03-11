package de.madem.homium.ui.activities.recipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.madem.homium.application.HomiumApplication
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import de.madem.homium.utilities.extensions.notNull
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

class RecipeEditViewModelFactory(private val recipeId: Int?): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeEditViewModel::class.java)) {
            return RecipeEditViewModel(recipeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class RecipeEditViewModel(private val recipeId: Int?): ViewModel() {

    private var _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe>
        get() = _recipe
    private var _ingredients = MutableLiveData<MutableList<RecipeIngredient>>()
    val ingredients: LiveData<MutableList<RecipeIngredient>>
        get() = _ingredients
    private var _descriptions = MutableLiveData<MutableList<RecipeDescription>>()
    val descriptions: LiveData<MutableList<RecipeDescription>>
        get() = _descriptions
    private val database = AppDatabase.getInstance()

    //handling image files
    private val oldImageFiles = mutableSetOf<File>()
    private var firstImagePath = ""
    //var shallDiscardPictureChanges = true

    init {
        if(recipeId == null) {
            _recipe.value = Recipe("", "", 0)
            _ingredients.value = mutableListOf()
            _descriptions.value = mutableListOf()
        } else {
            getRecipeFromDatabaseAndSetValues()
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("viewmodel destroyed")
    }

    private fun getRecipeFromDatabaseAndSetValues() {
        CoroutineBackgroundTask<Recipe>()
            .executeInBackground { database.recipeDao().getRecipeById(recipeId!!) }
            .onDone { _recipe.value = it; firstImagePath = it.image }
            .start()
        CoroutineBackgroundTask<List<RecipeIngredient>>()
            .executeInBackground { database.recipeDao().getIngredientByRecipeId(recipeId!!) }
            .onDone {
                _ingredients.value = it.toMutableList()
            }
            .start()
        CoroutineBackgroundTask<List<RecipeDescription>>()
            .executeInBackground { database.recipeDao().getDescriptionByRecipeId(recipeId!!) }
            .onDone {
                _descriptions.value = it.toMutableList()
            }
            .start()
    }

    fun editRecipeName(name: String) {
        val recipe = _recipe.value
        recipe?.name = name
        _recipe.value = recipe
    }

    fun editImagePath(path: String) {
        val recipe : Recipe = _recipe.value ?: return

        //marking old file if possible
        val oldPath = recipe.image ?: ""
        if(oldPath.isNotEmpty() && oldPath.isNotBlank()){
            val oldFile = File(oldPath)
            if(oldFile.exists()){
                oldImageFiles.add(oldFile)
            }
        }

        recipe.image = path
        _recipe.value = recipe
    }

    fun addIngredient(item: RecipeIngredient) {
        val list = _ingredients.value
        list?.add(item)
        _ingredients.value = list
    }

    fun addDescription(item: RecipeDescription) {
        val list = _descriptions.value
        list?.add(item)
        _descriptions.value = list
    }

    fun editDescription(index: Int, name: String) {
        _descriptions.value?.get(index)?.description = name
    }

    //THIS FUNCTION SHOULD always be called from a Background Thread
    suspend fun addDataToDatabase() = coroutineScope {
        launch {
            oldImageFiles.forEach{
                deleteImageFile(it)
            }
        }

        if (recipeId == null) {
            var newRecipeId = database.recipeDao().insertRecipe(_recipe.value!!)
            changeIngredientsAndDescriptionsRecipeId(newRecipeId.toInt())
            _ingredients.value?.forEach { database.recipeDao().insertIngredient(it) }
            _descriptions.value?.forEach {
                if(it.description.isNotEmpty() && it.description.isNotBlank()) {
                    database.recipeDao().insertDescription(it)
                }
            }
        } else {
            changeIngredientsAndDescriptionsRecipeId(recipeId)
            database.recipeDao().deleteIngredientByRecipeId(recipeId)
            database.recipeDao().deleteDescriptionByRecipeId(recipeId)
            database.recipeDao().updateRecipe(_recipe.value!!)
            _ingredients.value?.forEach {
                database.recipeDao().insertIngredient(it)
            }
            _descriptions.value?.forEach {
                if(it.description.isNotEmpty() && it.description.isNotBlank()) {
                    database.recipeDao().insertDescription(it)
                }
            }
        }

    }

    fun discardPictureChanges(){
        _recipe.value.notNull { recipe ->
            if(firstImagePath != recipe.image){
                //if picture is another than original then add current to old ones and remove original from old ones
                val path = recipe.image
                if(path.isNotEmpty() && path.isNotBlank()){
                    val oldFile = File(path)
                    if(oldFile.exists()){
                        oldImageFiles.add(oldFile)
                    }
                }

                val originalFile = File(firstImagePath)
                oldImageFiles.remove(originalFile)

                //deleting all other files
                CoroutineBackgroundTask<Unit>().executeInBackground {
                    oldImageFiles.forEach{
                        deleteImageFile(it)
                    }
                }.start()
            }
        }


    }

    private fun changeIngredientsAndDescriptionsRecipeId(id: Int) {
        _ingredients.value?.forEach { it.recipeId = id }
        _descriptions.value?.forEach { it.recipeID = id }
    }

    private fun deleteImageFile(file: File){
        file.delete()

        if(file.exists()){
            file.canonicalFile.delete()

            if(file.exists()){
                HomiumApplication.appContext?.deleteFile(file.name)
            }
        }
    }

}