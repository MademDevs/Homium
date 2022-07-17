package de.madem.homium.di.utils

import dagger.Module
import dagger.assisted.AssistedFactory
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import de.madem.homium.ui.activities.recipe.RecipeEditViewModel

@AssistedFactory
interface RecipeEditViewModelAssistedFactory{
    fun create(recipeId: Int?) : RecipeEditViewModel
}


@Module
@InstallIn(ActivityRetainedComponent::class)
interface AssistedInjectModule
