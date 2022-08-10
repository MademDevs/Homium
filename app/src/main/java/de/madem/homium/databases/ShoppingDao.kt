package de.madem.homium.databases

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.madem.homium.models.Product
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import kotlinx.coroutines.flow.Flow


@Dao
interface ShoppingDao {

    @Deprecated("Use this function in ProductDao")
    @Query("SELECT * FROM product")
    fun getAllProduct(): List<Product>

    @Query("SELECT * FROM shoppingItem")
    fun getAllShopping(): List<ShoppingItem>

    @Query("SELECT * FROM shoppingItem WHERE checked = 1")
    fun getAllCheckedShoppingItem(): List<ShoppingItem>

    @Query("SELECT * FROM shoppingItem ORDER BY uid DESC")
    fun getAllShoppingReversedOrder(): List<ShoppingItem>

    @Query("SELECT name FROM shoppingitem;")
    fun getAllShoppingNames(): List<String>

    @Insert
    fun insertProduct(vararg item: Product)

    @Insert
    fun insertShopping(vararg item: ShoppingItem)

    @Query("DELETE FROM product")
    fun deleteAllProduct()

    @Query("DELETE FROM shoppingItem")
    fun deleteAllShopping()

    //Search for Items -> if partial String (not full name), has to be %String%

    @Query("DELETE FROM product WHERE name LIKE :deleteItem")
    fun deleteProduct(deleteItem: String)

    @Query("DELETE FROM shoppingItem WHERE name LIKE :deleteItem")
    fun deleteShoppingByName(deleteItem: String)

    @Query("DELETE FROM shoppingItem WHERE name LIKE :itemName AND unit LIKE :itemUnit AND count = :itemCount")
    fun deleteShoppingByNameCountUnit(itemName: String, itemCount : Int, itemUnit: Units)

    @Deprecated("Use this function in ProductDao")
    @Query("SELECT * FROM product WHERE name LIKE :getItem")
    fun getProductsByName(getItem: String): List<Product>

    @Query("SELECT * FROM product WHERE name LIKE :name OR pluralName LIKE :name")
    fun getProductsByNameOrPlural(name: String) : List<Product>

    @Query("SELECT COUNT(*) FROM product")
    fun productSize() : Int

    @Deprecated("Use getShoppingItemById instead")
    @Query("SELECT * FROM shoppingItem WHERE uid = :id")
    fun getShoppingItemByIdSync(id: Int) : ShoppingItem

    @Query("SELECT * FROM shoppingItem WHERE uid = :id")
    fun getShoppingItemById(id: Int) : Flow<ShoppingItem?>

    @Query("UPDATE shoppingItem SET name = :name, count = :count, unit = :unit WHERE uid = :id")
    fun updateShoppingItemById(id: Int, name: String, count: Int, unit: String)

    @Query("DELETE FROM shoppingItem WHERE uid = :id")
    fun deleteShoppingItemById(id: Int)

    @Query("DELETE FROM shoppingItem WHERE name = :name AND count = :count")
    fun deleteShoppingItemByNameCount(name : String,count: Int)

    @Query("UPDATE shoppingItem SET checked = :checked WHERE uid = :id")
    fun setShoppingItemChecked(id: Int, checked: Boolean = true)

    @Query("DELETE FROM shoppingItem WHERE checked = 1")
    fun deleteAllCheckedShoppingItems()

    @Query("SELECT COUNT(uid) FROM shoppingItem;")
    fun shoppingListSize() : Int
}