package de.madem.homium.managers

import android.content.Context
import de.madem.homium.databases.AppDatabase
import de.madem.homium.models.Product
import de.madem.homium.utilities.backgroundtasks.CoroutineBackgroundTask
import javax.inject.Inject

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
        var fileReader = context.assets.open("productsCSV.csv").bufferedReader()
        var line = fileReader.readLine()
        while(line != null) {
            val splitted = line.split(";")
            dao.insertProduct(Product(splitted[0],splitted[1], splitted[2], splitted[3]))
            line = fileReader.readLine()
        }
        println("PRODUCTSIZE: ${dao.productSize()}")
    }
}