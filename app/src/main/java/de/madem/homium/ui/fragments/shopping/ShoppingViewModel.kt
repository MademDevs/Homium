package de.madem.homium.ui.fragments.shopping

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.ShoppingItem
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ShoppingViewModel : ViewModel() {

    //live data
    private val _text = MutableLiveData<String>().apply {
        value = "This is shopping Fragment"
    }
    val text: LiveData<String> = _text

    val shoppingItemList = MutableLiveData<List<ShoppingItem>>()

    //functions
    fun reloadShoppingItems(context: Context){
        GlobalScope.launch(IO) {
            shoppingItemList.value = AppDatabase.getInstance(context).itemDao().getAllShopping()
        }
    }



}