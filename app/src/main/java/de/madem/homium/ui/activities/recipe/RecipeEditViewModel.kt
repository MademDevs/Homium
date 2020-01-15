package de.madem.homium.ui.activities.recipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.utilities.CoroutineBackgroundTask

class RecipeEditViewModelFactory(private val recipeId: Int?): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecipeEditViewModel::class.java)) {
            return RecipeEditViewModel(recipeId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class RecipeEditViewModel(val recipeId: Int?): ViewModel() {

    private val _recipe = MutableLiveData<Recipe>()
    val recipe: LiveData<Recipe>
        get() = _recipe
    private val _ingredients = MutableLiveData<MutableList<RecipeIngredient>>()
    val ingredients: LiveData<MutableList<RecipeIngredient>>
        get() = _ingredients
    private val _descriptions = MutableLiveData<MutableList<RecipeDescription>>()
    val descriptions: LiveData<MutableList<RecipeDescription>>
        get() = _descriptions
    private val database = AppDatabase.getInstance()

    init {
        if(recipeId == null) {
            _recipe.value = Recipe("", "", 0)
            _ingredients.value = mutableListOf()
            _descriptions.value = mutableListOf()
        } else {
            getRecipeFromDatabaseAndSetValues()
        }
    }

    private fun getRecipeFromDatabaseAndSetValues() {
        CoroutineBackgroundTask<Recipe>()
            .executeInBackground { database.recipeDao().getRecipeById(recipeId!!) }
            .onDone { _recipe.value = it }
            .start()
        CoroutineBackgroundTask<List<RecipeIngredient>>()
            .executeInBackground { database.recipeDao().getIngredientByRecipeId(recipeId!!) }
            .onDone { _ingredients.value = it.toMutableList() }
            .start()
        CoroutineBackgroundTask<List<RecipeDescription>>()
            .executeInBackground { database.recipeDao().getDescriptionByRecipeId(recipeId!!) }
            .onDone { _descriptions.value = it.toMutableList() }
            .start()
    }

}