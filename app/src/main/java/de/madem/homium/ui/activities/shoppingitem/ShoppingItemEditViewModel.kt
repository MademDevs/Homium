package de.madem.homium.ui.activities.shoppingitem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.madem.homium.R
import de.madem.homium.constants.INTENT_DATA_TRANSFER_EDIT_SHOPPING_ITEM_ID
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.models.dataset
import de.madem.homium.repositories.ShoppingRepository
import de.madem.homium.utilities.extensions.notNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO Implement logic for handling process death with savedstatehandle
//TODO maybe handle errors
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
    private val _counterState : MutableStateFlow<ShoppingCounterState> = MutableStateFlow(
        ShoppingCounterState.InRange(0, _selectedUnit.value.dataset)
    )

    private var lastInRangeCounterStateIndex: Int? = null
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

    val counterState: Flow<ShoppingCounterState> = _counterState
    //endregion

    //region functions
    fun setEditItemName(newTitle: String) {
        if (_editItemName.value != newTitle) {
            viewModelScope.launch { _editItemName.emit(newTitle) }
        }
    }

    private fun setSelectedUnit(newUnit: Units) {
        val oldUnit = _selectedUnit.value
        if(oldUnit == newUnit) {
            return
        }

        viewModelScope.launch {
            _selectedUnit.emit(newUnit)
            adjustCounterStateOnUnitChange(oldUnit,newUnit)
        }
    }

    fun setSelectedUnitByIndex(index: Int) {
        if(index in units.indices) {
            setSelectedUnit(units[index])
        }
    }

    fun setCounterStateInRangeSelectedIndex(newIndex: Int) {
        viewModelScope.launch {
            when(val currentCounterState = _counterState.value) {
                is ShoppingCounterState.InRange -> {
                    //only set new index if its in range of the current dataset
                    if(newIndex in currentCounterState.dataset.indices) {
                        setCounterState(currentCounterState.copy(selectedIndex = newIndex))
                    }
                }
                is ShoppingCounterState.Custom -> {
                    //change type and selected index
                    //get dataset from currently selected unit
                    setCounterState(ShoppingCounterState.InRange(newIndex, _selectedUnit.value.dataset))
                }
            }
        }
    }

    fun setCounterStateToInRangeType() {
        viewModelScope.launch {
            val currentDataSet = _selectedUnit.value.dataset
            val currentCounterState = _counterState.value
            if(currentCounterState is ShoppingCounterState.Custom) {
                val currentValIndex = currentDataSet.indexOf(currentCounterState.value)
                val newStateIndex = if(currentValIndex in currentDataSet.indices)
                    currentValIndex
                else
                    0
                setCounterState(ShoppingCounterState.InRange(newStateIndex, currentDataSet))
            }
        }
    }

    fun setCounterStateCustomWithValue(value: String) {
        viewModelScope.launch {
            when(val currentCounterState = _counterState.value) {
                is ShoppingCounterState.InRange -> {
                    // Switch State to Custom State
                    setCounterState(ShoppingCounterState.Custom(value))
                }
                is ShoppingCounterState.Custom -> {
                    if(currentCounterState.value != value) {
                        setCounterState(ShoppingCounterState.Custom(value))
                    }
                }
            }
        }
    }

    fun setCounterStateToCustomType() {
        viewModelScope.launch {
            val currentCounterState = _counterState.value
            if(currentCounterState is ShoppingCounterState.InRange) {
                // Switch State to Custom State and apply value from currently selected index
                val value = currentCounterState.dataset[currentCounterState.selectedIndex]
                setCounterState(ShoppingCounterState.Custom(value))
            }
        }
    }

    /**
     * This function adjusts the counterState if it's InRange, so that Unit-Dataset change according
     * to the new selected Unit. The Unit-Dataset is only changed, when the [Units.isSmallUnit]-
     * Result of both [oldUnit] and [newUnit] differ from each other, which is why this Function
     * uses XOR to check this Condition.
     * */
    private suspend fun adjustCounterStateOnUnitChange(oldUnit: Units, newUnit: Units) {
        val currentCounterState = _counterState.value
        if(currentCounterState is ShoppingCounterState.InRange) {
            if(oldUnit.isSmallUnit().xor(newUnit.isSmallUnit())) {
                setCounterState(currentCounterState.copy(dataset = newUnit.dataset))
            }
        }
    }

    private suspend fun setCounterState(state: ShoppingCounterState) {
        val currentState = _counterState.value
        if(currentState is ShoppingCounterState.InRange) {
            lastInRangeCounterStateIndex = currentState.selectedIndex
        }
        _counterState.emit(state)
    }
    //endregion
}

sealed class ShoppingCounterState {
    data class InRange(val selectedIndex: Int, val dataset: Array<String>) : ShoppingCounterState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is InRange) return false
            if (selectedIndex != other.selectedIndex) return false
            if (!dataset.contentEquals(other.dataset)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = selectedIndex
            result = 31 * result + dataset.contentHashCode()
            return result
        }

    }
    data class Custom(val value: String) : ShoppingCounterState()
}