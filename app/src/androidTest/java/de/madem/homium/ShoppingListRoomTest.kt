package de.madem.homium

import androidx.test.platform.app.InstrumentationRegistry
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.ItemDao
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import org.junit.*

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

        Assert.assertEquals(0, items.size)
    }

    @Test
    fun insertAndSelct() {
        val item = ShoppingItem("Bananen", 5, Units.STUECK.name)
        dao.insertShopping(item)

        Assert.assertEquals(1, dao.getAllShopping().size)
    }

    @Test
    fun insertWithSameUid() {
        val item1 = ShoppingItem("Bananen", 5, Units.STUECK.name)
        val item2 = ShoppingItem("Birnen", 4, Units.STUECK.name)
        dao.insertShopping(item1)
        dao.insertShopping(item2)

        val items = dao.getAllShopping()
        println(items)

        Assert.assertEquals(2, items.size)

    }

}