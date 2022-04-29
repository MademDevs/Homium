package de.madem.homium.ui.activities.recipe

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import javax.inject.Inject

class RecipePresentationStepViewModel @Inject constructor() : ViewModel() {

    var textToDisplay = MutableLiveData<String>("")

}
