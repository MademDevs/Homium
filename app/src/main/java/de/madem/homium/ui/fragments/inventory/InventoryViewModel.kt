package de.madem.homium.ui.fragments.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InventoryViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Das Inventar ist bald verfügbar ;)"
    }
    val text: LiveData<String> = _text
}