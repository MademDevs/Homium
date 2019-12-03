package de.madem.homium.ui.fragments.money

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MoneyViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Das Bilanzbuch ist bald verfügbar ;)"
    }
    val text: LiveData<String> = _text
}