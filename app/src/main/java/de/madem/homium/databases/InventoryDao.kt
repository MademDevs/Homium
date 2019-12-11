package de.madem.homium.databases

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.madem.homium.models.InventoryItem

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventoryItem")
    fun getAllInventory(): List<InventoryItem>

    @Query("DELETE FROM inventoryItem")
    fun deleteAllInventory()

    @Query("DELETE FROM inventoryItem WHERE name LIKE :deleteItem")
    fun deleteInventory(deleteItem: String)

    @Insert
    fun insertInventory(vararg item: InventoryItem)

}