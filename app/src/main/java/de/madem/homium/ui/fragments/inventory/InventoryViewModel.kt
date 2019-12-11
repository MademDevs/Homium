package de.madem.homium.ui.fragments.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.madem.homium.application.HomiumApplication
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Units

class InventoryViewModel() : ViewModel() {

    var inventoryItems = MutableLiveData<List<InventoryItem>>().apply { value = listOf() }
    val context = HomiumApplication.appContext!!

    fun setDummyData() {
        val list = mutableListOf<InventoryItem>().apply {
            add(InventoryItem("Apfel", 1, Units.ITEM.getString(context), "Kühlschrank"))
            add(InventoryItem("Milch", 1, Units.LITRE.getString(context), "Kühlschrank"))
        }
        inventoryItems.value = list
    }

    private val _text = MutableLiveData<String>().apply {
        value = "Das Inventar ist bald verfügbar ;)"
    }
    val text: LiveData<String> = _text
}