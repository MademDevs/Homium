package de.madem.homium.ui.fragments.recipes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeIngredient
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RecipesViewModel @Inject constructor(
    database : AppDatabase
): ViewModel() {

    private val viewModelJob = Job()
    private val dao = database.recipeDao()

    val recipeList = MutableLiveData<List<Recipe>>().apply {
        value = listOf()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun reloadRecipes(){
        CoroutineBackgroundTask<List<Recipe>>()
            .executeInBackground { dao.getAllRecipe() }
            .onDone { recipeList.value = it }
            .start()
    }

    fun getIngredientsByRecipeId(id: Int) : List<RecipeIngredient> = dao.getIngredientByRecipeId(id)

    fun deleteRecipe(recipe: Recipe){
        viewModelScope.launch {
            dao.deleteRecipe(recipe)
        }
    }

    fun deleteImages() {
        recipeList.value?.forEach {
            val file = File(it.image)
            file.delete()
        }
    }

}