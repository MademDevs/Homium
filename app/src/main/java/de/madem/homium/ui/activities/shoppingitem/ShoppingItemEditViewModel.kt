package de.madem.homium.ui.activities.shoppingitem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.madem.homium.R
import de.madem.homium.constants.INTENT_DATA_TRANSFER_EDIT_SHOPPING_ITEM_ID
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.repositories.ShoppingRepository
import de.madem.homium.utilities.extensions.notNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO Implement logic for handling process death with savedstatehandle
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingItemEditViewModel @Inject constructor(
    shoppingRepository: ShoppingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    //region private Properties
    private val itemIdFlow: Flow<Int?> = flowOf(
        savedStateHandle.get<Int>(INTENT_DATA_TRANSFER_EDIT_SHOPPING_ITEM_ID)
    )

    private val existingShoppingItem: StateFlow<ShoppingItem?> = itemIdFlow.flatMapLatest { id ->
        if (id == null) flowOf(null) else shoppingRepository.getShoppingItemById(id)
    }
        .map { it?.data }
        .onEach { item ->
            item.notNull { shoppingItem ->
                //name
                setEditItemName(shoppingItem.name)
                //unit
                setSelectedUnit(shoppingItem.unit)

            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    private val _editItemName = MutableStateFlow("")
    private val _selectedUnit : MutableStateFlow<Units> = MutableStateFlow(Units.default)
    //endregion

    //region exposed data for UI
    val units : Array<Units>
        get() = Units.values()

    val actionTitleResId: Flow<Int> = itemIdFlow.map {
        if (it == null)
            R.string.screentitle_edit_shopppingitem_add
        else
            R.string.screentitle_edit_shoppingitem_edit
    }
    val editItemName: Flow<String> = _editItemName
    val showDeleteButton: Flow<Boolean> = existingShoppingItem.map { it != null }
    val selectedUnitIndex : Flow<Int> = _selectedUnit.map { units.indexOf(it) }
    //endregion

    //region functions
    fun setEditItemName(newTitle: String) {
        if (_editItemName.value != newTitle) {
            viewModelScope.launch { _editItemName.emit(newTitle) }
        }
    }

    private fun setSelectedUnit(unit: Units) {
        if(_selectedUnit.value == unit) {
            return
        }

        viewModelScope.launch { _selectedUnit.emit(unit) }
    }

    fun setSelectedUnitByIndex(index: Int) {
        if(index in units.indices) {
            setSelectedUnit(units[index])
        }
    }
    //endregion
}