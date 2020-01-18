package de.madem.homium.ui.activities.recipe

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecipePresentationViewModel : ViewModel() {

    private var _recipeId : Int = -1

    val recipeId : Int
    get() = _recipeId

    fun updateRecipeId(id : Int){
        _recipeId = id
    }
}