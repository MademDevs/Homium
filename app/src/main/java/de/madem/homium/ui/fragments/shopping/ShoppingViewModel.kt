package de.madem.homium.ui.fragments.shopping

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.ShoppingItem
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class ShoppingViewModel @Inject constructor(): ViewModel() {

    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private val ioScope = CoroutineScope(Dispatchers.IO + viewModelJob)

    private val shoppingItemDao = AppDatabase.getInstance().itemDao()

    //list with all shopping items
    val shoppingItemList = MutableLiveData<List<ShoppingItem>>().apply {
        value = listOf()
    }

    //functions

    //should run in ioScope of fragment, because if user switches fragment, coroutine should be canceled
    fun reloadShoppingItems(reversed: Boolean = false) = ioScope.launch {
        val shoppingList = if (reversed) {
            shoppingItemDao.getAllShoppingReversedOrder()
        } else {
            shoppingItemDao.getAllShopping()
        }

        shoppingItemList.postValue(shoppingList)
        println("VIEWMODEL: Shoppinglist updated")
    }

    fun updateShoppingItem(shoppingItem: ShoppingItem) = GlobalScope.launch{
        shoppingItemDao.setShoppingItemChecked(shoppingItem.uid, shoppingItem.checked)
    }

    //should run in GlobalScope because it coroutine should not be canceled, if fragment ends
    fun deleteAllCheckedItems(callback: () -> Unit) = GlobalScope.launch(Dispatchers.IO) {
        //delete all items
        shoppingItemDao.deleteAllCheckedShoppingItems()

        //run callback in ui scope
        uiScope.launch { callback() }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}