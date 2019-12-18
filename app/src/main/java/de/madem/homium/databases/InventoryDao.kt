package de.madem.homium.databases

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.madem.homium.models.InventoryItem

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventoryItem")
    fun fetchAllInventoryItems(): List<InventoryItem>

    @Query("SELECT * FROM inventoryItem WHERE uid = :id")
    fun fetchInventoryItemById(id: Int): InventoryItem

    @Query("DELETE FROM inventoryItem")
    fun clearInventory()

    @Query("DELETE FROM inventoryItem WHERE uid LIKE :id")
    fun deleteInventoryItemById(id: Int)

    @Insert
    fun insertInventoryItems(vararg item: InventoryItem)

    @Query("""
        UPDATE inventoryItem 
        SET name = :name, count = :count, unit = :unit, location = :location 
        WHERE uid = :id
        """)
    fun updateInventoryItem(id: Int, name: String, count: Int, unit: String, location: String)

    @Query("SELECT COUNT(uid) FROM inventoryItem;")
    fun inventorySize() : Int

}