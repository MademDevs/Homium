package de.madem.homium.ui.activities.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.InventoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestViewModel @Inject constructor(
    database: AppDatabase
) : ViewModel(){
    private val inventoryDao = database.inventoryDao()

    fun clearInventory() {
        viewModelScope.launch(Dispatchers.IO) {
            inventoryDao.clearInventory()
        }
    }

    fun insertInventoryItems(items: List<InventoryItem>) {
        viewModelScope.launch(Dispatchers.IO) {
            inventoryDao.insertInventoryItems(*items.toTypedArray())
        }
    }
}