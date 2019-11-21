package de.madem.homium

import androidx.test.platform.app.InstrumentationRegistry
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.ItemDao
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class ShoppingListRoomTest {

    companion object {
        private lateinit var dao: ItemDao

        @BeforeClass
        @JvmStatic
        fun setupClass() {
            dao = AppDatabase.getInstance(
                InstrumentationRegistry.getInstrumentation().targetContext
            ).itemDao()
        }
    }


    @Before
    fun setup() {
        dao.deleteAllShopping()
    }


    @Test
    fun simpleSelectAll() {
        val items = dao.getAllShopping()

        println(items)

        Assert.assertTrue(items.isEmpty())
    }

    @Test
    fun insertAndSelect() {
        val item = ShoppingItem("Bananen", 5, Units.GRAM.name)
        dao.insertShopping(item)

        Assert.assertEquals(1, dao.getAllShopping().size)
    }

    @Test
    fun insertWithSameUid() {
        val item1 = ShoppingItem("Bananen", 5, Units.GRAM.name)
        val item2 = ShoppingItem("Birnen", 4, Units.GRAM.name)
        dao.insertShopping(item1)
        dao.insertShopping(item2)

        val items = dao.getAllShopping()
        println(items)

        Assert.assertEquals(2, items.size)

    }

    @Test
    fun getNotExistingShoppingElement() {
        val item = dao.getShoppingItemById(-1)

        Assert.assertEquals(null, item) //not existing item is null
    }

    @Test
    fun testAutoUpdateList() {

        val list = dao.getAllShopping()
        val initSize = list.size

        dao.insertShopping(ShoppingItem("Birnen", 4, Units.GRAM.name))

        Assert.assertEquals(initSize, list.size) //list sadly don't updates on item insert
    }
}