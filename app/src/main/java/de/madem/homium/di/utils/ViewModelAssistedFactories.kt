package de.madem.homium.di.utils

import dagger.assisted.AssistedFactory
import de.madem.homium.ui.activities.recipe.RecipeEditViewModel

@AssistedFactory
interface RecipeEditViewModelAssistedFactory{
    fun create(recipeId: Int?) : RecipeEditViewModel
}

