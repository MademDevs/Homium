package de.madem.homium.ui.fragments.recipes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecipesViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Die Rezeptverwaltung ist bald verfügbar ;)"
    }
    val text: LiveData<String> = _text
}