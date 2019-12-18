package de.madem.homium.ui.fragments.inventory

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.madem.homium.application.HomiumApplication
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Units
import de.madem.homium.utilities.CoroutineBackgroundTask
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class InventoryViewModel : ViewModel() {

    var inventoryItems = MutableLiveData<List<InventoryItem>>().apply { value = listOf() }
    val context = HomiumApplication.appContext!!
    val dao = AppDatabase.getInstance().inventoryDao()

    fun reloadInventoryItems() {
        GlobalScope.launch(IO) {
            val items = dao.fetchAllInventoryItems()
            inventoryItems.postValue(items)
        }
    }


}