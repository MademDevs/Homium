package de.madem.homium.ui.fragments.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.InventoryDao
import de.madem.homium.models.InventoryItem
import de.madem.homium.repositories.InventoryRepository
import de.madem.homium.utilities.InventoryItemAmountClassifier
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val repository: InventoryRepository
) : ViewModel() {

    //public properties
    val inventoryItems: Flow<List<InventoryItem>> = repository.getAllInventoryItems()
        .map { list ->
            list.sortedWith(
                compareBy(
                    { InventoryItemAmountClassifier.byInventoryItem(it).order }, { it.name }
                )
            )
        }

    //public methods
    fun deleteInventoryItems(inventoryItems: Collection<InventoryItem>, onDone: () -> Unit) {
        viewModelScope.launch(IO) {
            inventoryItems.forEach { repository.deleteInventoryItemById(it.uid) }
            viewModelScope.launch(Main) { onDone.invoke() }
        }
    }


}