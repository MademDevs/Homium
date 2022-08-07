package de.madem.homium.databases

import de.madem.homium.models.InventoryItem
import de.madem.homium.repositories.InventoryRepository
import kotlinx.coroutines.flow.Flow

class RoomInventoryRepository(
    private val dao: InventoryDao
): InventoryRepository {

    override fun getAllInventoryItems(): Flow<List<InventoryItem>> {
        return dao.fetchAllInventoryItems()
    }

    override suspend fun deleteInventoryItemById(id: Int) {
        dao.deleteInventoryItemById(id)
    }



}