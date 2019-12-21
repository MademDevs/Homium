package de.madem.homium.ui.fragments.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.madem.homium.R
import de.madem.homium.application.HomiumApplication

class SettingsViewModel : ViewModel() {

    //fields
    private val accessContext = HomiumApplication.appContext!!


    //Live Data
    private val _vibrationAllowed = MutableLiveData<Boolean>().apply {
        value = true
    }
    val vibrationAllowed: LiveData<Boolean> = _vibrationAllowed

    private val _selectedSortID = MutableLiveData<Int>().apply {
        value = R.id.radio_sort_normal
    }
    val selectedSortID: LiveData<Int> = _selectedSortID

    private val _deleteQuestionAllowed = MutableLiveData<Boolean>().apply {
        value = true
    }
    val deleteQuestionAllowed: LiveData<Boolean> = _deleteQuestionAllowed






}