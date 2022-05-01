package de.madem.homium.ui.activities.recipe

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Recipe
import de.madem.homium.models.RecipeDescription
import de.madem.homium.models.RecipeIngredient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class RecipePresentationViewModel @Inject constructor(
    database: AppDatabase
): ViewModel() {

    private val recipeDao = database.recipeDao()

    private var _recipeId : Int = -1

    val recipeId : Int
    get() = _recipeId

    fun updateRecipeId(id : Int){
        _recipeId = id
    }

    suspend fun getRecipeDescription() : List<RecipeDescription> = coroutineScope{
        return@coroutineScope withContext(Dispatchers.IO) {
            recipeDao.getDescriptionByRecipeId(recipeId)
        }
    }

    suspend fun getIngredients() : List<RecipeIngredient> = coroutineScope{
        return@coroutineScope withContext(Dispatchers.IO) {
            recipeDao.getIngredientByRecipeId(recipeId)
        }
    }

    suspend fun getRecipe() : Recipe = coroutineScope{
        return@coroutineScope withContext(Dispatchers.IO) {
            recipeDao.getRecipeById(recipeId)
        }
    }
}