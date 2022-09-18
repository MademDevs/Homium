package de.madem.homium.repositories

import de.madem.homium.errors.businesslogicerrors.ShoppingItemNotFoundException
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.utilities.AppResult
import kotlinx.coroutines.flow.Flow

/**
 * This interface defines all functions for manipulating data related to Shopping-Feature
 */
interface ShoppingRepository {
    /**
     * @return A [Flow] of [AppResult] containing a single [ShoppingItem], whose uid matches [id].
     * If no Item could be found, the Flow should emit an [AppResult.Error] with an
     * [ShoppingItemNotFoundException]
     */
    fun getShoppingItemById(id: Int) : Flow<AppResult<ShoppingItem>>

    /** @return A [Flow] of [AppResult] containing a List of all existing ShoppingItems*/
    fun getAllShoppingItems() : Flow<AppResult<List<ShoppingItem>>>

    /**
     * This function deletes a certain [ShoppingItem] by it's uid [id].
     * @return An [AppResult] to indicate the Operation-Status
     * */
    suspend fun deleteShoppingItemById(id: Int) : AppResult<Unit>

    /**
     * This function deletes all [ShoppingItem]s that whose [ShoppingItem.checked]-State is set to
     * true An [AppResult] to indicate the Operation-Status
     * @return
     * */
    suspend fun deleteAllCheckedShoppingItems() : AppResult<Unit>

    /**
     * This function updates a certain [ShoppingItem] by it's uid [id].
     * @param id The uid of the [ShoppingItem] to be updated
     * @param name The new name of the [ShoppingItem]
     * @param count The new count of the [ShoppingItem]
     * @param unit The new unit of the [ShoppingItem]
     * @param isChecked The new isChecked-Value of the [ShoppingItem]
     * @return An [AppResult] to indicate the Operation-Status.
     * */
    suspend fun updateShoppingItemById(id: Int, name: String, count: Int, unit: Units, isChecked: Boolean) : AppResult<Unit>

    /**
     * This function inserts a new [ShoppingItem]
     * @return An [AppResult] to indicate the Operation-Status.
     * */
    suspend fun insertShoppingItem(item: ShoppingItem) : AppResult<Unit>
}