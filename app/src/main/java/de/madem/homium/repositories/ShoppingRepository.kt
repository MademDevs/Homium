package de.madem.homium.repositories

import de.madem.homium.models.ShoppingItem
import de.madem.homium.utilities.AppResult
import kotlinx.coroutines.flow.Flow

//TODO Add Documentation
interface ShoppingRepository {
    //TODO Documentation
    fun getShoppingItemById(id: Int) : Flow<AppResult<ShoppingItem>>
    suspend fun deleteShoppingItemById(id: Int) : AppResult<Unit>
}