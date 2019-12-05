package de.madem.homium.ui.fragments.recipes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.madem.homium.models.Recipe
import de.madem.homium.models.ShoppingItem

class RecipesViewModel : ViewModel() {
    val recipeList = MutableLiveData<List<Recipe>>().apply {
        value = listOf(
            Recipe("rezept1", "beschreibung1", "bild1"),
            Recipe("rezept2", "beschreibung2", "bild2"),
            Recipe("rezept3", "beschreibung3", "bild3"),
            Recipe("rezept4", "beschreibung4", "bild4"),
            Recipe("rezept5", "beschreibung5", "bild5"),
            Recipe("rezept6", "beschreibung6", "bild6"),
            Recipe("rezept7", "beschreibung7", "bild7")
            )
    }
}