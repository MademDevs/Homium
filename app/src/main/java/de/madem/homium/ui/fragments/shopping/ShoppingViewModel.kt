package de.madem.homium.ui.fragments.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.madem.homium.R
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.ShoppingItem
import de.madem.homium.repositories.ShoppingRepository
import de.madem.homium.repositories.extensions.updateIsChecked
import de.madem.homium.utilities.AppResult
import de.madem.homium.utilities.backgroundtasks.debounce
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

//TODO Fix sorting of ShoppingList
@HiltViewModel
class ShoppingViewModel @Inject constructor(
    database: AppDatabase,
    private val shoppingRepository: ShoppingRepository
) : ViewModel() {

    private val shoppingItemDao = database.shoppingDao()

    private val updateShoppingItemCheckedDebounced =
        debounce<Pair<ShoppingItem, Boolean>>(50UL) { (item, isChecked) ->
            shoppingRepository.updateIsChecked(item, isChecked)
        }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: Flow<Boolean> = _isRefreshing

    private val toastNotificationChannel = Channel<Int>()
    val toastNotifications = toastNotificationChannel.receiveAsFlow()

    val shoppingItems = shoppingRepository
        .getAllShoppingItems()
        .map { it.data ?: emptyList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2000L),
            initialValue = emptyList()
        )

    //TODO This is only a emergency-solution: Change Code, so that Refreshing-State is only changed
    // by ViewModel
    fun setRefreshing(isRefreshing: Boolean) {
        _isRefreshing.update { isRefreshing }
    }

    fun setShoppingItemCheckState(shoppingItem: ShoppingItem, isChecked: Boolean) {
        viewModelScope.launch {
            updateShoppingItemCheckedDebounced.startDebounced(Pair(shoppingItem,isChecked))
        }
    }

    //should run in GlobalScope because it coroutine should not be canceled, if fragment ends
    fun deleteAllCheckedItems() {
        viewModelScope.launch {
            shoppingRepository.deleteAllCheckedShoppingItems()
            toastNotificationChannel.send(R.string.notification_remove_bought_shoppingitems)
            setRefreshing(false)
        }
    }

    fun deleteShoppingItem(item: ShoppingItem) : Flow<Boolean> = flow {
        emit(shoppingRepository.deleteShoppingItemById(item.uid))
    }.mapNotNull { result ->
        when(result) {
            is AppResult.Success -> true
            is AppResult.Error -> false
            else -> null
        }
    }

    suspend fun getAllCheckedShoppingItems(): List<ShoppingItem> = coroutineScope {
        val itemsDeferred = async(Dispatchers.IO) { shoppingItemDao.getAllCheckedShoppingItem() }
        return@coroutineScope itemsDeferred.await()
    }
}