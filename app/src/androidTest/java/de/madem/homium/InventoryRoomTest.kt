package de.madem.homium


import androidx.test.platform.app.InstrumentationRegistry
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.InventoryDao
import de.madem.homium.databases.ItemDao
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.ShoppingItem
import de.madem.homium.models.Units
import de.madem.homium.utilities.quantitycalculation.UnitConverter
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class InventoryRoomTest {

    companion object {
        private lateinit var dao: InventoryDao

        @BeforeClass
        @JvmStatic
        fun setupClass() {
            dao = AppDatabase.getInstance(
                InstrumentationRegistry.getInstrumentation().targetContext
            ).inventoryDao()
        }
    }


    @Before
    fun setup() {
        dao.clearInventory()
    }

    @Test
    fun testUpdateConvertedUnit(){
        val inventoryItem = InventoryItem(
            "Testitem",
            15,Units.KILOGRAM.getString(InstrumentationRegistry.getInstrumentation().targetContext),
            "Kühlschrank")

        dao.insertInventoryItems(inventoryItem)

        val iId = dao.getUIdsByNameUnit(inventoryItem.name,inventoryItem.unit).first()
        val converter = UnitConverter()

        val convItem = converter.convertUnitOf(inventoryItem,Units.GRAM)

        dao.updateQuantityOf(iId,convItem.count,convItem.unit)

        val resultFromDB = dao.fetchInventoryItemById(iId)

        Assert.assertEquals(Units.GRAM.getString(),resultFromDB.unit)
        Assert.assertEquals(15000,resultFromDB.count)
    }





}