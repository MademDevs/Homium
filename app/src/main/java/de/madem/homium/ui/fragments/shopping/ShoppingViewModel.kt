package de.madem.homium.ui.fragments.shopping

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.ShoppingItem
import de.madem.homium.utilities.CoroutineBackgroundTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class ShoppingViewModel : ViewModel() {

    //TODO @Max
    private val viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Default + viewModelJob)

    //list with all shopping items
    val shoppingItemList = MutableLiveData<List<ShoppingItem>>().apply {
        value = listOf()
    }

    //functions
    fun reloadShoppingItems(){
        CoroutineBackgroundTask<List<ShoppingItem>>()
            .executeInBackground { AppDatabase.getInstance().itemDao().getAllShopping() }
            .onDone { shoppingItemList.value = it }
            .start()
    }

    fun updateShoppingItem(shoppingItem: ShoppingItem) {
        CoroutineBackgroundTask<Unit>()
            .executeInBackground {
                AppDatabase.getInstance().itemDao().setShoppingItemChecked(
                    shoppingItem.uid, shoppingItem.checked
                )
            }.start()
    }

    fun deleteAllCheckedItems(callback: () -> Unit) {
        CoroutineBackgroundTask<Unit>()
            .executeInBackground {
                AppDatabase.getInstance().itemDao().deleteAllCheckedShoppingItems()
            }.onDone { callback() }
            .start()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}