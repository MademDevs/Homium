package de.madem.homium.databases

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Product
import de.madem.homium.models.ShoppingItem


@Dao
interface ItemDao {

    /*function:
    @Query/Insert/Delete for type of SQL-Query
    (SQL query itself)
    fun functionName(): returnType
    examples:
    @Delete
    fun delete(item: ShoppingItem)
    @Query("SELECT * FROM shoppingitem WHERE uid IN (:shoppingItemIds)")
    fun loadAllByIds(shoppingItemIds: IntArray): List<ShoppingItem>
    @Query("SELECT * FROM shoppingItem WHERE name LIKE :name")
    fun findByName(name: String): ShoppingItem
     */

    @Query("SELECT * FROM product")
    fun getAllProduct(): List<Product>

    @Query("SELECT * FROM shoppingItem ORDER BY uid DESC")
    fun getAllShopping(): List<ShoppingItem>

    @Query("SELECT * FROM inventoryItem")
    fun getAllInventory(): List<InventoryItem>

    @Insert
    fun insertProduct(vararg item: Product)

    @Insert
    fun insertShopping(vararg item: ShoppingItem)

    @Insert
    fun insertInventory(vararg item: InventoryItem)

    @Query("DELETE FROM product")
    fun deleteAllProduct()

    @Query("DELETE FROM shoppingItem")
    fun deleteAllShopping()

    @Query("DELETE FROM inventoryItem")
    fun deleteAllInventory()


    //Search for Items -> if partial String (not full name), has to be %String%

    @Query("DELETE FROM product WHERE name LIKE :deleteItem")
    fun deleteProduct(deleteItem: String)

    @Query("DELETE FROM shoppingItem WHERE name LIKE :deleteItem")
    fun deleteShopping(deleteItem: String)

    @Query("DELETE FROM inventoryItem WHERE name LIKE :deleteItem")
    fun deleteInventory(deleteItem: String)

    //return List -> iterate and adapt view -> auto-completion
    @Query("SELECT * FROM product WHERE name LIKE :getItem")
    fun getProductsByName(getItem: String): List<Product>

    @Query("SELECT COUNT(*) FROM product")
    fun productSize() : Int

    @Query("SELECT * FROM shoppingItem WHERE uid = :id")
    fun getShoppingItemById(id: Int) : ShoppingItem

    @Query("UPDATE shoppingItem SET name = :name, count = :count, unit = :unit WHERE uid = :id")
    fun updateShoppingItemById(id: Int, name: String, count: Int, unit: String)

    @Query("DELETE FROM shoppingItem WHERE uid = :id")
    fun deleteShoppingItemById(id: Int)

    @Query("UPDATE shoppingItem SET checked = :checked WHERE uid = :id")
    fun setShoppingItemChecked(id: Int, checked: Boolean = true)

    @Query("DELETE FROM shoppingItem WHERE checked = 1")
    fun deleteAllCheckedShoppingItems()

}