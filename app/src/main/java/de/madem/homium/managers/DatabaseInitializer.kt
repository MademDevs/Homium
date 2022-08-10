package de.madem.homium.managers

import android.content.Context
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Product
import de.madem.homium.models.Units
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import javax.inject.Inject

//TODO Use this class in Room-Callback
class DatabaseInitializer @Inject constructor(
    private val context: Context,
    database: AppDatabase,
    private val doneCallback: () -> Unit
) {

    private val dao = database.shoppingDao()
    private val backgroundTask =
        CoroutineBackgroundTask<Unit>()

    init {
        with(backgroundTask) {
            executeInBackground {
                deleteAllProducts()
                loadProductsFromFile()
            }
            onDone { doneCallback.invoke() }
            start()
        }
    }

    private fun deleteAllProducts() {
        dao.deleteAllProduct()
    }

    private fun loadProductsFromFile() {
        val fileReader = context.assets.open("productsCSV.csv").bufferedReader()
        var line = fileReader.readLine()
        while(line != null) {
            val splitted = line.split(";")
            val name = splitted[0]
            val plural = splitted[1]
            val unit = Units.unitOf(splitted[2], context) ?: Units.default
            val amount = splitted[3]
            dao.insertProduct(Product(name, plural, unit, amount))
            line = fileReader.readLine()
        }
        println("PRODUCTSIZE: ${dao.productSize()}")
    }
}