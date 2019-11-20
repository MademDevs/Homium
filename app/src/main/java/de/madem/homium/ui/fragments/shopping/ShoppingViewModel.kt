package de.madem.homium.ui.fragments.shopping

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.ShoppingItem
import de.madem.homium.utilities.CoroutineBackgroundTask

class ShoppingViewModel : ViewModel() {

    val shoppingItemList = MutableLiveData<List<ShoppingItem>>()


    //live data
    private val _text = MutableLiveData<String>().apply {
        value = "This is shopping Fragment"
    }
    val text: LiveData<String> = _text


    //functions
    fun reloadShoppingItems(context: Context){
        CoroutineBackgroundTask<List<ShoppingItem>>()
            .executeInBackground { AppDatabase.getInstance(context).itemDao().getAllShopping() }
            .onDone { shoppingItemList.value = it }
            .start()
    }



}