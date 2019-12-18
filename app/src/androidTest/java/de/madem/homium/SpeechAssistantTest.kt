package de.madem.homium

import androidx.test.platform.app.InstrumentationRegistry
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.ItemDao
import de.madem.homium.models.ShoppingItem
import de.madem.homium.speech.SpeechAssistent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

class SpeechAssistantTest {

    companion object {
        private lateinit var shoppingDao: ItemDao
        private lateinit var speechAssistant: SpeechAssistent

        @BeforeClass
        @JvmStatic
        fun setupClass() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext

            shoppingDao = AppDatabase.getInstance(context).itemDao()
            speechAssistant = SpeechAssistent(context)
        }
    }

    @Before
    fun setup() {
        shoppingDao.deleteAllShopping()
    }

    @Test
    fun testInsert1() = runBlocking{
        val testString = "Setze ein Apfel auf die Einkaufsliste".toLowerCase()
        val targetShoppingItem = ShoppingItem("Apfel", 1, "Stück")

        testShoppingItemInsert(testString, targetShoppingItem)
    }

    @Test
    fun testInsert2() = runBlocking{
        val testString = "Setze einen Apfel auf die Einkaufsliste".toLowerCase()
        val targetShoppingItem = ShoppingItem("Apfel", 1, "Stück")

        testShoppingItemInsert(testString, targetShoppingItem)
    }

    @Test
    fun multiInsert() = runBlocking {
        val testString = "Setze auf die Einkaufsliste Bananen, Äpfel und Bier".toLowerCase()
        //add items with default values
        val targetShoppingItems = listOf<ShoppingItem>(ShoppingItem("Banane", 1, "Stück"),
            ShoppingItem("Apfel",1, "Stück"), ShoppingItem("Bier", 500, "Milliliter"))

        testMultiShoppingItemInsert(testString, targetShoppingItems)
    }

    @Test
    fun testInsertWithoutQuantity() = runBlocking{
        val testString = "Setze Banane auf die Einkaufsliste".toLowerCase()
        val targetShoppingItem = ShoppingItem("Banane", 1, "Stück")

        testShoppingItemInsert(testString, targetShoppingItem)
    }

    @Test
    fun deleteShoppingList() = runBlocking {
        val testString = "bereinige die Einkaufsliste".toLowerCase()

        testClearShoppingList(testString)
    }


    private suspend fun testShoppingItemInsert(testString: String, targetShoppingItem: ShoppingItem) {
        println("Testing if '$testString' adds shopping item '$targetShoppingItem' to database")

        //execute test command
        speechAssistant.executeCommand(testString)

        //wait 1 second to complete async execution
        delay(1000)

        val shoppingItems = shoppingDao.getAllShopping()

        if (shoppingItems.isEmpty()) {
            Assert.fail("Nothing inserted!")
        }

        val lastInsertedShoppingItem = shoppingItems.last()

        Assert.assertTrue(lastInsertedShoppingItem.contentEquals(targetShoppingItem))
    }

    private suspend fun testMultiShoppingItemInsert(testString: String, targetShoppingItems: List<ShoppingItem>){
        println("Testing if '$testString' adds shopping items '$targetShoppingItems' to database")

        speechAssistant.executeCommand(testString)

        delay(1000)

        val shoppingItems = shoppingDao.getAllShopping()

        if(shoppingItems.size != targetShoppingItems.size){
            Assert.fail("Not all shopping items inserted!")
        }

        Assert.assertTrue((shoppingItems.equals(targetShoppingItems)))
    }

    private suspend fun testClearShoppingList(testString: String){
        shoppingDao.insertShopping(ShoppingItem("Bier", 500, "Milliiter"), ShoppingItem("Apfel", 1, "Stück"),
            ShoppingItem("Kiwi", 3, "Stück")
        )

        speechAssistant.executeCommand(testString)

        delay(1000)

        if (shoppingDao.getAllShopping().isEmpty()){
            Assert.assertTrue(shoppingDao.getAllShopping().isEmpty())
        } else{
            Assert.fail("Shoppinglist is not emtpy, items in Shoppinglist: ${shoppingDao.getAllShopping().toString()}")
        }
    }

}