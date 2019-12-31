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

    @Query("DELETE FROM inventoryItem WHERE name LIKE :name")
    fun deleteInventoryItemByName(name: String)

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

    @Query("SELECT name FROM inventoryItem;")
    fun getAllInventoryItemNames(): List<String>

    @Query("SELECT location FROM inventoryItem;")
    fun getAllInventoryLocations(): List<String>

    @Query("DELETE FROM inventoryItem WHERE name LIKE :name AND count = :count AND unit LIKE :unit")
    fun deleteInventoryByNameCountUnit(name: String, count: Int, unit : String)

    @Query("DELETE FROM inventoryItem WHERE name LIKE :name AND location LIKE :location")
    fun deleteInventoryItemByNameLocation(name : String, location: String)

    @Query("DELETE FROM inventoryItem WHERE name LIKE :name AND count = :count")
    fun deleteInventoryByNameCount(name: String, count: Int)

    @Query("DELETE FROM inventoryItem WHERE name LIKE :name AND count = :count AND location LIKE :location")
    fun deleteInventoryByNameCountLocation(name: String, count: Int, location: String)

    @Query("""DELETE FROM inventoryItem WHERE name LIKE :name AND count = :count 
            AND unit LIKE :unit AND location LIKE :location""")
    fun deleteInventory(name : String, count: Int, unit: String, location: String)

    @Query("DELETE FROM inventoryItem WHERE location LIKE :location")
    fun clearLocation(location: String)

}