package de.madem.homium.ui.fragments.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.InventoryItem
import de.madem.homium.utilities.InventoryItemAmountClassifier
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class InventoryViewModel : ViewModel() {

    //private properties
    private val dao = AppDatabase.getInstance().inventoryDao()

    //public properties
    private val _inventoryItems by lazy {
        MutableLiveData<List<InventoryItem>>().apply { value = listOf() }
    }
    val inventoryItems: LiveData<List<InventoryItem>> = _inventoryItems

    //public methods
    fun reloadInventoryItems() {
        viewModelScope.launch(IO) {

            val items = dao.fetchAllInventoryItems()
                .sortedWith(compareBy(
                    { InventoryItemAmountClassifier.byInventoryItem(it).order }, { it.name }
                ))

            _inventoryItems.postValue(items)
        }
    }

    fun deleteInventoryItems(inventoryItems: Collection<InventoryItem>, onDone: () -> Unit) {
        viewModelScope.launch(IO) {
            inventoryItems.forEach { dao.deleteInventoryItemById(it.uid) }

            viewModelScope.launch(Main) { onDone.invoke() }
        }
    }


}