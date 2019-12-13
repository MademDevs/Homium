package de.madem.homium.databases

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.madem.homium.models.InventoryItem

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventoryItem")
    fun fetchAllInventoryItems(): List<InventoryItem>

    @Query("DELETE FROM inventoryItem")
    fun clearInventory()

    @Query("DELETE FROM inventoryItem WHERE name LIKE :deleteItem")
    fun deleteInventoryItem(deleteItem: String)

    @Insert
    fun insertInventoryItems(vararg item: InventoryItem)

}