package de.madem.homium.ui.fragments.shopping

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ShoppingViewModel : ViewModel() {

    //live data
    private val _text = MutableLiveData<String>().apply {
        value = "This is shopping Fragment"
    }
    val text: LiveData<String> = _text

    //functions
    fun fetchAllShoppingItems(){
        //TODO: Implement code to get all shopping items from Database
    }

}