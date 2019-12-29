package de.madem.homium.ui.fragments.recipes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.ShoppingItem
import de.madem.homium.utilities.CoroutineBackgroundTask
import kotlinx.coroutines.Job
import java.io.File

class RecipesViewModel : ViewModel() {

    private val viewModelJob = Job()
    private val db = AppDatabase.getInstance()

    val recipeList = MutableLiveData<List<Recipe>>().apply {
        value = listOf(
            )
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun reloadRecipes(){
        CoroutineBackgroundTask<List<Recipe>>()
            .executeInBackground { db.recipeDao().getAllRecipe() }
            .onDone { recipeList.value = it }
            .start()
    }

    fun deleteAllRecipes(callback: () -> Unit){
        CoroutineBackgroundTask<Unit>()
            .executeInBackground {
                db.recipeDao().deleteAllRecipe()
                db.recipeDao().deleteAllIngredient()
            }
            .onDone { callback() }
            .start()
        deleteImages()
    }

    fun deleteImages() {
        recipeList.value?.forEach {
            val file = File(it.image)
            file.delete()
        }
    }

}