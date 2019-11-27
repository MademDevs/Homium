package de.madem.homium.ui.fragments.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Die Einstellungen sind bald verfügbar ;)"
    }
    val text: LiveData<String> = _text
}