package de.madem.homium.ui.activities.shoppingitem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.madem.homium.R
import de.madem.homium.constants.INTENT_DATA_TRANSFER_EDIT_SHOPPING_ITEM_ID
import de.madem.homium.models.ShoppingItem
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
            item.notNull { editItemTitle.emit(it.name) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    private val editItemTitle = MutableStateFlow("")
    //endregion

    //region exposed data for UI
    val actionTitleResId: Flow<Int> = itemIdFlow.map {
        if (it == null)
            R.string.screentitle_edit_shopppingitem_add
        else
            R.string.screentitle_edit_shoppingitem_edit
    }
    val displayedEditItemTitle: Flow<String> = editItemTitle
    val showDeleteButton: Flow<Boolean> = existingShoppingItem.map { it != null }
    //endregion

    //region functions
    fun setEditItemTitle(newTitle: String) {
        viewModelScope.launch {
            if (editItemTitle.value != newTitle) {
                editItemTitle.emit(newTitle)
            }
        }
    }
    //endregion
}