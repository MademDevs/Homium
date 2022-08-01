package de.madem.homium.repositories

import de.madem.homium.models.InventoryItem
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {

    fun getAllInventoryItems(): Flow<List<InventoryItem>>

    //TODO: Add return type
    fun deleteInventoryItemById(id: Int)

}