package de.madem.homium.repositories

import de.madem.homium.models.ShoppingItem
import de.madem.homium.utilities.AppResult
import kotlinx.coroutines.flow.Flow

//TODO Add Documentation
interface ShoppingRepository {
    //TODO
    fun getShoppingItemById(id: Int) : Flow<AppResult<ShoppingItem>>
}