package de.madem.homium.databases

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.madem.homium.models.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Deprecated(message = "fetchAllInventoryItems with flow should be used")
    @Query("SELECT * FROM inventoryItem")
    fun fetchAllInventoryItemsOld(): List<InventoryItem>

    @Query("SELECT * FROM inventoryItem")
    fun fetchAllInventoryItems(): Flow<List<InventoryItem>>

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

    @Query("UPDATE inventoryItem SET count=:count, unit=:unit WHERE uid=:id")
    fun updateQuantityOf(id: Int, count: Int, unit: String)

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

    @Query("SELECT SUM(count) FROM inventoryItem WHERE name LIKE :name AND unit LIKE :unit")
    fun sumOfQuantityByNameUnit(name: String,unit: String): Int

    @Query("SELECT uid FROM inventoryItem WHERE name LIKE :name AND unit LIKE :unit")
    fun getUIdsByNameUnit(name: String,unit: String): List<Int>

    @Query("SELECT * FROM inventoryItem WHERE name LIKE :name AND unit NOT IN (:forbiddenUnits)")
    fun getInventoryItemWithNameWithoutForbiddenUnits(name : String, forbiddenUnits: List<String>) : List<InventoryItem>
}